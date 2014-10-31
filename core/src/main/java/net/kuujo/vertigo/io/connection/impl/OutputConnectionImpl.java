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

import io.vertx.core.*;
import io.vertx.core.eventbus.*;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.connection.OutputConnectionInfo;

import java.util.TreeMap;
import java.util.UUID;

/**
 * Default output connection implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputConnectionImpl<T> implements OutputConnection<T> {
  private static final String ACTION_HEADER = "action";
  private static final String ID_HEADER = "id";
  private static final String INDEX_HEADER = "index";
  private static final String CONNECT_ACTION = "connect";
  private static final String DISCONNECT_ACTION = "disconnect";
  private static final String MESSAGE_ACTION = "message";
  private static final String ACK_ACTION = "ack";
  private static final String FAIL_ACTION = "fail";
  private static final String PAUSE_ACTION = "pause";
  private static final String RESUME_ACTION = "resume";
  private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;
  private final Logger log;
  private final Vertx vertx;
  private final EventBus eventBus;
  private final OutputConnectionInfo info;
  private final String outAddress;
  private final String inAddress;
  private MessageConsumer<Long> consumer;
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private Handler<Void> drainHandler;
  private long currentMessage = 1;
  private final TreeMap<Long, JsonObject> messages = new TreeMap<>();
  private boolean open;
  private boolean full;
  private boolean paused;

  private final Handler<Message<Long>> internalMessageHandler = new Handler<Message<Long>>() {
    @Override
    public void handle(Message<Long> message) {
      String action = message.headers().get(ACTION_HEADER);
      if (action != null) {
        switch (action) {
          case ACK_ACTION:
            doAck(message.body());
            break;
          case FAIL_ACTION:
            doFail(message.body());
            break;
          case PAUSE_ACTION:
            doPause(message.body());
            break;
          case RESUME_ACTION:
            doResume(message.body());
            break;
        }
      }
    }
  };

  public OutputConnectionImpl(Vertx vertx, OutputConnectionInfo info) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.info = info;
    this.outAddress = String.format("%s.out", info.address());
    this.inAddress = String.format("%s.in", info.address());
    this.consumer = eventBus.consumer(inAddress);
    this.log = LoggerFactory.getLogger(String.format("%s-%s", OutputConnectionImpl.class.getName(), info.target()));
  }

  @Override
  public String address() {
    return info.address();
  }

  @Override
  public OutputConnectionInfo info() {
    return info;
  }

  @Override
  public OutputConnection<T> open() {
    return open(null);
  }

  @Override
  public OutputConnection<T> open(final Handler<AsyncResult<Void>> doneHandler) {
    if (consumer == null) {
      consumer = eventBus.consumer(outAddress);
      consumer.handler(internalMessageHandler);
      consumer.completionHandler((result) -> {
        if (result.failed()) {
          doneHandler.handle(result);
        } else {
          connect(doneHandler);
        }
      });
    }
    return this;
  }

  /**
   * Connects to the other side of the connection.
   */
  private void connect(final Handler<AsyncResult<Void>> doneHandler) {
    // Recursively send "connect" messages to the other side of the connection
    // until we get a response. This gives the other side of the connection time
    // to open and ensures that the connection doesn't claim it's open until
    // the other side has registered a handler and responded at least once.
    eventBus.send(inAddress, new JsonObject(), new DeliveryOptions().setSendTimeout(1000).addHeader(ACTION_HEADER, CONNECT_ACTION), (Handler<AsyncResult<Message<Boolean>>>) (result) -> {
      if (result.failed()) {
        ReplyException failure = (ReplyException) result.cause();
        if (failure.failureType().equals(ReplyFailure.RECIPIENT_FAILURE)) {
          log.warn(String.format("%s - Failed to connect to %s", OutputConnectionImpl.this, info.target()), result.cause());
          doneHandler.handle(Future.completedFuture(failure));
        } else if (failure.failureType().equals(ReplyFailure.TIMEOUT)) {
          log.warn(String.format("%s - Connection to %s failed, retrying", OutputConnectionImpl.this, info.target()));
          connect(doneHandler);
        } else {
          log.debug(String.format("%s - Connection to %s failed, retrying", OutputConnectionImpl.this, info.target()));
          vertx.setTimer(500, (timerId) -> {
            connect(doneHandler);
          });
        }
      } else if (result.result().body()) {
        log.info(String.format("%s - Connected to %s", OutputConnectionImpl.this, info.target()));
        open = true;
        doneHandler.handle(Future.completedFuture());
      } else {
        log.debug(String.format("%s - Connection to %s failed, retrying", OutputConnectionImpl.this, info.target()));
        vertx.setTimer(500, (timerId) -> {
          connect(doneHandler);
        });
      }
    });
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

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    if (consumer != null) {
      consumer.unregister((result) -> {
        if (result.failed()) {
          doneHandler.handle(result);
        } else {
          disconnect(doneHandler);
        }
      });
    } else {
      doneHandler.handle(Future.completedFuture());
    }
  }

  /**
   * Disconnects from the other side of the connection.
   */
  private void disconnect(final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.send(inAddress, new JsonObject(), new DeliveryOptions().setSendTimeout(5000).addHeader(ACTION_HEADER, DISCONNECT_ACTION), (Handler<AsyncResult<Message<Boolean>>>) (result) -> {
      if (result.failed()) {
        ReplyException failure = (ReplyException) result.cause();
        if (failure.failureType().equals(ReplyFailure.RECIPIENT_FAILURE)) {
          log.warn(String.format("%s - Failed to disconnect from %s", OutputConnectionImpl.this, info.target()), result.cause());
          doneHandler.handle(Future.completedFuture(failure));
        } else if (failure.failureType().equals(ReplyFailure.NO_HANDLERS)) {
          log.info(String.format("%s - Disconnected from %s", OutputConnectionImpl.this, info.target()));
          doneHandler.handle(Future.completedFuture());
        } else {
          log.debug(String.format("%s - Disconnection from %s failed, retrying", OutputConnectionImpl.this, info.target()));
          disconnect(doneHandler);
        }
      } else if (result.result().body()) {
        log.info(String.format("%s - Disconnected from %s", OutputConnectionImpl.this, info.target()));
        open = false;
        doneHandler.handle(Future.completedFuture());
      } else {
        log.debug(String.format("%s - Disconnection from %s failed, retrying", OutputConnectionImpl.this, info.target()));
        vertx.setTimer(500, (timerId) -> {
          disconnect(doneHandler);
        });
      }
    });
  }

  /**
   * Checks whether the connection is open.
   */
  private void checkOpen() {
    if (!open) throw new IllegalStateException(String.format("%s - Connection to %s not open.", this, info.target()));
  }

  /**
   * Checks whether the connection is full.
   */
  private void checkFull() {
    if (!full && messages.size() >= maxQueueSize) {
      full = true;
      log.debug(String.format("%s - Connection to %s is full", this, info.target()));
    }
  }

  /**
   * Checks whether the connection has been drained.
   */
  private void checkDrain() {
    if (full && !paused && messages.size() < maxQueueSize / 2) {
      full = false;
      log.debug(String.format("%s - Connection to %s is drained", this, info.target()));
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
      eventBus.send(inAddress, message);
    }
  }

  /**
   * Handles a connection pause.
   */
  private void doPause(long id) {
    log.debug(String.format("%s - Paused connection to %s", this, info.target()));
    paused = true;
  }

  /**
   * Handles a connection resume.
   */
  private void doResume(long id) {
    if (paused) {
      log.debug(String.format("%s - Resumed connection to %s", this, info.target()));
      paused = false;
      checkDrain();
    }
  }

  /**
   * Sends a message.
   */
  private OutputConnection<T> doSend(Object value) {
    return doSend(value, null);
  }

  /**
   * Sends a message.
   */
  private OutputConnection<T> doSend(Object message, MultiMap headers) {
    checkOpen();

    if (open && !paused) {
      // Generate a unique ID and monotonically increasing index for the message.
      String id = UUID.randomUUID().toString();
      long index = currentMessage++;

      // Set up the message headers.
      DeliveryOptions options = new DeliveryOptions();
      if (headers == null) {
        headers = new CaseInsensitiveHeaders();
      }
      headers.add(ACTION_HEADER, MESSAGE_ACTION);
      headers.add(ID_HEADER, id);
      headers.add(INDEX_HEADER, String.valueOf(index));
      options.setHeaders(headers);

      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Send: Message[id=%s, message=%s]", this, id, message));
      }
      eventBus.send(inAddress, message, options);
      checkFull();
    }
    return this;
  }

  @Override
  public OutputConnection<T> send(T message) {
    return doSend(message);
  }

  @Override
  public OutputConnection<T> send(T message, MultiMap headers) {
    return doSend(message, headers);
  }

  @Override
  public String toString() {
    return info.toString();
  }

}
