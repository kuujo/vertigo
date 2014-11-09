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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.VertigoMessage;
import net.kuujo.vertigo.io.connection.InputConnection;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.impl.VertigoMessageImpl;
import net.kuujo.vertigo.util.Closeable;
import net.kuujo.vertigo.util.Openable;

/**
 * Input connection implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputConnectionImpl<T> implements InputConnection<T>, Openable<InputConnection<T>>, Closeable<InputConnection<T>> {
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
  private static final long BATCH_SIZE = 1000;
  private static final long MAX_BATCH_TIME = 100;
  private final Logger log;
  private final Vertx vertx;
  private final EventBus eventBus;
  private final InputConnectionContext context;
  private final String inAddress;
  private final String outAddress;
  private MessageConsumer<T> consumer;
  private Handler<VertigoMessage<T>> messageHandler;
  private long lastReceived;
  private long lastFeedbackTime;
  private long feedbackTimerID;
  private boolean open;
  private boolean connected;
  private boolean paused;

  private final Handler<Long> internalTimer = new Handler<Long>() {
    @Override
    public void handle(Long timerID) {
      // Ensure that feedback messages are sent at least every second or so.
      // This will ensure that feedback is still provided when output connections
      // are full, otherwise the feedback will never be triggered.
      long currentTime = System.currentTimeMillis();
      if (currentTime - lastFeedbackTime > 1000) {
        ack();
      }
    }
  };

  private final Handler<Message<T>> internalMessageHandler = new Handler<Message<T>>() {
    @Override
    public void handle(Message<T> message) {
      if (open && !paused) {
        String action = message.headers().get(ACTION_HEADER);
        switch (action) {
          case MESSAGE_ACTION:
            if (checkIndex(Long.valueOf(message.headers().get(INDEX_HEADER)))) {
              doMessage(message);
            }
            break;
          case CONNECT_ACTION:
            doConnect(message);
            break;
          case DISCONNECT_ACTION:
            doDisconnect(message);
            break;
        }
      }
    }
  };

  public InputConnectionImpl(Vertx vertx, InputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.inAddress = String.format("%s.in", context.address());
    this.outAddress = String.format("%s.out", context.address());
    this.log = LoggerFactory.getLogger(String.format("%s-%s", InputConnectionImpl.class.getName(), context.address()));
  }

  @Override
  public String address() {
    return context.address();
  }

  public InputConnectionContext context() {
    return context;
  }

  @Override
  public InputConnection<T> open() {
    return open(null);
  }

  @Override
  public InputConnection<T> open(final Handler<AsyncResult<Void>> doneHandler) {
    if (consumer == null) {
      consumer = eventBus.consumer(inAddress);
      consumer.handler(internalMessageHandler);
      consumer.completionHandler((result) -> {
        if (result.succeeded()) {
          log.info(String.format("%s - Opened connection to %s", InputConnectionImpl.this, context.source()));
          if (feedbackTimerID == 0) {
            log.debug(String.format("%s - Starting periodic ack timer with max batch interval: %d", InputConnectionImpl.this, MAX_BATCH_TIME));
            feedbackTimerID = vertx.setPeriodic(MAX_BATCH_TIME, internalTimer);
          }
          open = true;
        } else {
          log.warn(String.format("%s - Failed to open connection to %s", InputConnectionImpl.this, context.source()));
        }
        doneHandler.handle(result);
      });
    }
    return this;
  }

  /**
   * Checks that the given index is valid.
   */
  private boolean checkIndex(long index) {
    // Ensure that the given ID is a monotonically increasing ID.
    // If the ID is less than the last received ID then reset the
    // last received ID since the connection must have been reset.
    if (lastReceived == 0 || index == lastReceived + 1 || index < lastReceived) {
      lastReceived = index;
      // If the ID reaches the end of the current batch then tell the data
      // source that it's okay to remove all previous messages.
      if (lastReceived % BATCH_SIZE == 0) {
        ack();
      }
      return true;
    } else {
      fail();
    }
    return false;
  }

  /**
   * Sends an ack message for the current received count.
   */
  private void ack() {
    // Send a message to the other side of the connection indicating the
    // last message that we received in order. This will allow it to
    // purge messages we've already received from its queue.
    if (open && connected) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Acking messages up to: %d", this, lastReceived));
      }
      eventBus.send(outAddress, null, new DeliveryOptions().addHeader(ACTION_HEADER, ACK_ACTION).addHeader(INDEX_HEADER, String
        .valueOf(lastReceived)));
      lastFeedbackTime = System.currentTimeMillis();
    }
  }

  /**
   * Sends a fail message for the current received count.
   */
  private void fail() {
    // Send a "fail" message indicating the last message we received in order.
    // This will cause the other side of the connection to resend messages
    // in order from that point on.
    if (open && connected) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Received a message out of order: %d", this, lastReceived));
      }
      eventBus.send(outAddress, null, new DeliveryOptions().addHeader(ACTION_HEADER, FAIL_ACTION).addHeader(INDEX_HEADER, String
        .valueOf(lastReceived)));
      lastFeedbackTime = System.currentTimeMillis();
    }
  }

  @Override
  public InputConnection<T> pause() {
    if (!paused) {
      paused = true;
      if (open && connected) {
        log.debug(String.format("%s - Pausing connection: %s", this, context.source()));
        eventBus.send(outAddress, null, new DeliveryOptions().addHeader(ACTION_HEADER, PAUSE_ACTION).addHeader(INDEX_HEADER, String
          .valueOf(lastReceived)));
      }
    }
    return this;
  }

  @Override
  public InputConnection<T> resume() {
    if (paused) {
      paused = false;
      if (open && connected) {
        log.debug(String.format("%s - Resuming connection: %s", this, context.source()));
        eventBus.send(outAddress, null, new DeliveryOptions().addHeader(ACTION_HEADER, RESUME_ACTION).addHeader(INDEX_HEADER, String
          .valueOf(lastReceived)));
      }
    }
    return this;
  }

  @Override
  public InputConnection<T> messageHandler(Handler<VertigoMessage<T>> handler) {
    if (open && handler == null) {
      throw new IllegalStateException("cannot set null handler on locked connection");
    }
    this.messageHandler = handler;
    return this;
  }

  /**
   * Handles receiving a message.
   */
  @SuppressWarnings("unchecked")
  private void doMessage(final Message<T> message) {
    if (messageHandler != null) {
      String id = message.headers().get(ID_HEADER);
      long index = Long.valueOf(message.headers().get(INDEX_HEADER));
      VertigoMessage<T> vertigoMessage = new VertigoMessageImpl<T>(id, index, context.port().name(), message.body(), message.headers());

      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Received: Message[id=%s, value=%s]", this, id, message));
      }
      messageHandler.handle(vertigoMessage);
    }
  }

  /**
   * Handles connect.
   */
  private void doConnect(final Message<T> message) {
    if (open) {
      if (!connected) {
        connected = true;
      }
      message.reply(true);
      log.debug(String.format("%s - Accepted connect request from %s", this, context.source()));
    } else {
      message.reply(false);
      log.debug(String.format("%s - Rejected connect request from %s, connection not open", this, context.source()));
    }
  }

  /**
   * Handles disconnect.
   */
  private void doDisconnect(final Message<T> message) {
    if (open) {
      if (connected) {
        connected = false;
      }
      message.reply(true);
      log.debug(String.format("%s - Accepted disconnect request from %s", this, context.source()));
    } else {
      message.reply(false);
      log.debug(String.format("%s - Rejected connect request from %s, connection not open", this, context.source()));
    }
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    if (consumer != null) {
      consumer.unregister((result) -> {
        if (feedbackTimerID > 0) {
          log.debug(String.format("%s - Stopping periodic ack timer", InputConnectionImpl.this));
          vertx.cancelTimer(feedbackTimerID);
          feedbackTimerID = 0;
        }
        open = false;
        log.info(String.format("%s - Closed connection from %s", InputConnectionImpl.this, context.source()));
        doneHandler.handle(result);
      });
    } else {
      doneHandler.handle(Future.completedFuture());
    }
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
