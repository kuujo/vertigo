/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.vertigo.io.connection.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;

import java.util.TreeMap;
import java.util.UUID;

/**
 * Default output connection implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputConnectionImpl<T> implements OutputConnection<T>, Handler<Message<T>> {
  private static final String ACTION_HEADER = "action";
  private static final String PORT_HEADER = "port";
  private static final String ID_HEADER = "name";
  private static final String INDEX_HEADER = "index";
  private static final String MESSAGE_ACTION = "message";
  private static final String ACK_ACTION = "ack";
  private static final String FAIL_ACTION = "fail";
  private static final String PAUSE_ACTION = "pause";
  private static final String RESUME_ACTION = "resume";
  private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;
  private final Logger log;
  private final Vertx vertx;
  private final EventBus eventBus;
  private final OutputConnectionContext context;
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private Handler<Void> drainHandler;
  private long currentMessage = 1;
  private final TreeMap<Long, JsonObject> messages = new TreeMap<>();
  private final TreeMap<Long, Handler<AsyncResult<Void>>> ackHandlers = new TreeMap<>();
  private boolean full;
  private boolean paused;

  public OutputConnectionImpl(Vertx vertx, OutputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.log = LoggerFactory.getLogger(String.format("%s-%s", OutputConnectionImpl.class.getName(), context.port().output().component().address()));
  }

  @Override
  public void handle(Message<T> message) {
    String action = message.headers().get(ACTION_HEADER);
    Long id = Long.valueOf(message.headers().get(INDEX_HEADER));
    switch (action) {
      case ACK_ACTION:
        doAck(id);
        break;
      case FAIL_ACTION:
        doFail(id);
        break;
      case PAUSE_ACTION:
        doPause(id);
        break;
      case RESUME_ACTION:
        doResume(id);
        break;
    }
  }

  @Override
  public OutputConnection<T> setSendQueueMaxSize(int maxSize) {
    this.maxQueueSize = maxSize;
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return maxQueueSize;
  }

  @Override
  public int size() {
    return messages.size();
  }

  @Override
  public boolean sendQueueFull() {
    return paused || messages.size() >= maxQueueSize;
  }

  @Override
  public OutputConnection<T> drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  /**
   * Checks whether the connection is full.
   */
  private void checkFull() {
    if (!full && messages.size() >= maxQueueSize) {
      full = true;
      log.debug(String.format("%s - Connection to %s is full", this, context.target()));
    }
  }

  /**
   * Checks whether the connection has been drained.
   */
  private void checkDrain() {
    if (full && !paused && messages.size() < maxQueueSize / 2) {
      full = false;
      log.debug(String.format("%s - Connection to %s is drained", this, context.target()));
      if (drainHandler != null) {
        drainHandler.handle((Void) null);
      }
    }
  }

  /**
   * Handles a batch ack.
   */
  private void doAck(long id) {
    // The other side of the connection has sent a message indicating which
    // messages it has seen. We can clear any messages before the indicated ID.
    if (log.isDebugEnabled()) {
      log.debug(String.format("%s - Received ack for messages up to %d, removing all previous messages from memory", this, id));
    }
    if (messages.containsKey(id)) {
      messages.headMap(id, true).clear();
    } else {
      messages.clear();
    }
    checkDrain();
  }

  /**
   * Handles a batch fail.
   */
  private void doFail(long id) {
    if (log.isDebugEnabled()) {
      log.debug(String.format("%s - Received resend request for messages starting at %d", this, id));
    }

    // Ack all the entries before the given ID.
    doAck(id);

    // Now that all the entries before the given ID have been removed,
    // just iterate over the messages map and resend all the messages.
    for (JsonObject message : messages.values()) {
      eventBus.send(context.target().address(), message);
    }
  }

  /**
   * Handles a connection pause.
   */
  private void doPause(long id) {
    log.debug(String.format("%s - Paused connection to %s", this, context.target()));
    paused = true;
  }

  /**
   * Handles a connection resume.
   */
  private void doResume(long id) {
    if (paused) {
      log.debug(String.format("%s - Resumed connection to %s", this, context.target()));
      paused = false;
      checkDrain();
    }
  }

  /**
   * Sends a message.
   */
  private OutputConnection<T> doSend(Object message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    if (!paused) {
      // Generate a unique ID and monotonically increasing index for the message.
      String id = UUID.randomUUID().toString();
      long index = currentMessage++;

      if (ackHandler != null) {
        ackHandlers.put(index, ackHandler);
      }

      // Set up the message headers.
      DeliveryOptions options = new DeliveryOptions()
        .addHeader(PORT_HEADER, context.target().port())
        .addHeader(ID_HEADER, id)
        .addHeader(INDEX_HEADER, String.valueOf(index));
      if (headers == null) {
        headers = new CaseInsensitiveHeaders();
      }
      headers.add(ACTION_HEADER, MESSAGE_ACTION);
      headers.add(ID_HEADER, id);
      headers.add(INDEX_HEADER, String.valueOf(index));
      options.setHeaders(headers);

      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Send: Message[name=%s, message=%s]", this, id, message));
      }
      eventBus.send(context.target().address(), message, options);
      checkFull();
    }
    return this;
  }

  @Override
  public OutputConnection<T> send(T message) {
    return doSend(message, null, null);
  }

  @Override
  public OutputConnection<T> send(T message, MultiMap headers) {
    return doSend(message, headers, null);
  }

  @Override
  public OutputConnection<T> send(T message, Handler<AsyncResult<Void>> ackHandler) {
    return doSend(message, null, ackHandler);
  }

  @Override
  public OutputConnection<T> send(T message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    return doSend(message, headers, ackHandler);
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
