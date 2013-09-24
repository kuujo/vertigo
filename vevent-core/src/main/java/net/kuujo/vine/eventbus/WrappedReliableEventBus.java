/*
* Copyright 2013 the original author or authors.
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
package net.kuujo.vine.eventbus;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A wrapper around the eventbus that provides reliability features.
 *
 * @author Jordan Halterman
 */
public class WrappedReliableEventBus implements ReliableEventBus {
 
  private EventBus eventBus;
 
  private Vertx vertx;
 
  private static final long DEFAULT_TIMEOUT = 5000;

  public WrappedReliableEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }
 
  public WrappedReliableEventBus(EventBus eventBus, Vertx vertx) {
    this.eventBus = eventBus;
    this.vertx = vertx;
  }
 
  public ReliableEventBus setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }
 
  public Vertx getVertx() {
    return vertx;
  }

  /**
   * An interface for eventbus callers.
   */
  private interface Caller<T> {
    public void call(Handler<T> replyHandler);
  }

  /**
   * A reliable eventbus call.
   */
  private class ReliableCall<T> {
    private Caller<T> caller;
    private int sendAttempts;
    private long timeout;
    private boolean retry;
    private int attempts;

    private ReliableCall(Caller<T> caller) {
      this.caller = caller;
    }

    public ReliableCall<T> timeout(long timeout) {
      this.timeout = timeout;
      return this;
    }

    public ReliableCall<T> retry(boolean retry) {
      this.retry = retry;
      return this;
    }
  
    public ReliableCall<T> attempts(int attempts) {
      this.attempts = attempts;
      return this;
    }

    /**
     * Executes the call.
     */
    public void execute(AsyncResultHandler<T> handler) {
      final Future<T> future = new DefaultFutureResult<T>();
      future.setHandler(handler);

      TimerHandler<T> timerHandler = new TimerHandler<T>(future);

      final long timeoutID = vertx.setTimer(timeout, timerHandler);

      Handler<T> sendHandler = new Handler<T>() {
        @Override
        public void handle(T message) {
          vertx.cancelTimer(timeoutID);
          future.setResult(message);
        }
      };

      timerHandler.setReplyHandler(sendHandler);
      caller.call(sendHandler);
      sendAttempts++;
    }

    /**
     * A timer handler that performs recursive calls the call handler.
     */
    private class TimerHandler<E> implements Handler<Long> {
      private Future<E> future;
      private Handler<T> replyHandler;

      public void setReplyHandler(Handler<T> replyHandler) {
        this.replyHandler = replyHandler;
      }

      public TimerHandler(Future<E> future) {
        this.future = future;
      }

      @Override
      public void handle(Long event) {
        if (retry) {
          if (sendAttempts < attempts) {
            caller.call(replyHandler);
            sendAttempts++;
          }
          else {
            future.setFailure(new TimeoutException("Send timed out."));
          }
        }
        else {
          future.setFailure(new TimeoutException("Send timed out."));
        }
      }
      
    }
  }
 
  @Override
  public EventBus send(String address, Object message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public EventBus send(String address, Object message, Handler<Message> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public EventBus send(String address, Object message, AsyncResultHandler<Message> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public EventBus send(String address, Object message, long timeout, AsyncResultHandler<Message> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public EventBus send(String address, Object message, long timeout, boolean retry, AsyncResultHandler<Message> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public EventBus send(final String address, final Object message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message> replyHandler) {
    new ReliableCall<Message>(new Caller<Message>() {
      @Override
      public void call(Handler<Message> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }

  @Override
  public EventBus send(String address, JsonObject message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, JsonObject message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, JsonObject message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, JsonObject message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, JsonObject message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final JsonObject message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, JsonArray message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, JsonArray message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, JsonArray message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, JsonArray message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, JsonArray message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final JsonArray message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, Buffer message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, Buffer message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Buffer message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Buffer message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Buffer message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Buffer message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, byte[] message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, byte[] message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, byte[] message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, byte[] message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, byte[] message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final byte[] message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, String message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, String message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, String message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, String message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, String message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final String message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }

  @Override
  public EventBus send(String address, Integer message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Integer message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Integer message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Integer message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Integer message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Integer message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }

  @Override
  public EventBus send(String address, Long message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Long message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Long message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Long message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Long message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Long message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, Float message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, Float message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Float message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Float message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Float message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Float message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, Double message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, Double message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Double message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Double message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Double message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Double message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, Boolean message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, Boolean message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Boolean message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Boolean message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Boolean message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Boolean message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, Short message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, Short message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Short message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Short message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Short message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Short message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, Character message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, Character message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Character message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Character message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Character message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Character message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus send(String address, Byte message) {
    eventBus.send(address, message);
    return this;
  }
 
  @Override
  public <T> EventBus send(String address, Byte message, Handler<Message<T>> replyHandler) {
    eventBus.send(address, message, replyHandler);
    return this;
  }

  @Override
  public <T> EventBus send(String address, Byte message, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, DEFAULT_TIMEOUT, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Byte message, long timeout, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, false, 0, replyHandler);
  }

  @Override
  public <T> EventBus send(String address, Byte message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler) {
    return send(address, message, timeout, retry, 1, replyHandler);
  }

  @Override
  public <T> EventBus send(final String address, final Byte message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler) {
    new ReliableCall<Message<T>>(new Caller<Message<T>>() {
      @Override
      public void call(Handler<Message<T>> replyHandler) {
        send(address, message, replyHandler);
      }
    }).timeout(timeout).retry(retry).attempts(attempts).execute(replyHandler);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Object message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, JsonObject message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, JsonArray message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Buffer message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, byte[] message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, String message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Integer message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Long message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Float message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Double message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Boolean message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Short message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Character message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus publish(String address, Byte message) {
    eventBus.publish(address, message);
    return this;
  }
 
  @Override
  public EventBus unregisterHandler(String address, Handler<? extends Message> handler,
      Handler<AsyncResult<Void>> resultHandler) {
    eventBus.unregisterHandler(address, handler, resultHandler);
    return this;
  }
 
  @Override
  public EventBus unregisterHandler(String address, Handler<? extends Message> handler) {
    eventBus.unregisterHandler(address, handler);
    return this;
  }
 
  @Override
  public EventBus registerHandler(String address, Handler<? extends Message> handler,
      Handler<AsyncResult<Void>> resultHandler) {
    eventBus.registerHandler(address, handler, resultHandler);
    return this;
  }
 
  @Override
  public EventBus registerHandler(String address, Handler<? extends Message> handler) {
    eventBus.registerHandler(address, handler);
    return this;
  }
 
  @Override
  public EventBus registerLocalHandler(String address, Handler<? extends Message> handler) {
    eventBus.registerLocalHandler(address, handler);
    return this;
  }
 
  @Override
  public void close(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.close(doneHandler);
  }

}
