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
package net.kuujo.vertigo.eventbus.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.eventbus.AdaptiveEventBus;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Adaptive event bus wrapper implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class WrappedAdaptiveEventBus implements AdaptiveEventBus {
  private static final long DEFAULT_REPLY_TIME = 5000;
  private static final long CALCULATE_INTERVAL = 1000;
  private static final int WINDOW_SIZE = 5;
  private final Vertx vertx;
  private final EventBus eventBus;
  private float defaultTimeout;
  private final Map<String, List<List<Long>>> replyTimes = new HashMap<>();
  private final Map<String, Long> replyTimeouts = new HashMap<>();
  private long timerID;

  private Handler<Long> timer = new Handler<Long>() {
    @Override
    public void handle(Long timerID) {
      for (Map.Entry<String, List<List<Long>>> entry : replyTimes.entrySet()) {
        String address = entry.getKey();
        List<List<Long>> allTimes = entry.getValue();
        if (allTimes.size() > WINDOW_SIZE) {
          allTimes.remove(0);
        }
        int count = 0;
        long total = 0;
        for (List<Long> times : allTimes) {
          for (long time : times) {
            count++;
            total += time;
          }
        }
        long average = Math.round(total / count);
        replyTimeouts.put(address, average);
        allTimes.add(new ArrayList<Long>());
      }
    }
  };

  public WrappedAdaptiveEventBus(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    timerID = vertx.setTimer(CALCULATE_INTERVAL, timer);
  }

  /**
   * Gets an average weighted reply time.
   */
  private long getReplyTime(String address, float weight) {
    Long average = replyTimeouts.get(address);
    return average != null ? Math.max(Math.round(average * weight), 500) : DEFAULT_REPLY_TIME;
  }

  /**
   * Adds a reply time for future calculation.
   */
  private void addReplyTime(String address, long time) {
    List<List<Long>> allTimes = replyTimes.get(address);
    if (allTimes == null) {
      allTimes = new ArrayList<>();
      replyTimes.put(address, allTimes);
    }
    if (allTimes.isEmpty()) allTimes.add(new ArrayList<Long>());
    allTimes.get(allTimes.size()-1).add(time);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> doneHandler) {
    if (timerID > 0) {
      vertx.cancelTimer(timerID);
      timerID = 0;
    }
  }

  @Override
  public long getDefaultReplyTimeout() {
    return eventBus.getDefaultReplyTimeout();
  }

  @Override
  public AdaptiveEventBus send(String address, Object message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public AdaptiveEventBus send(String address, Object message, final Handler<Message> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<Object>>>() {
        @Override
        public void handle(AsyncResult<Message<Object>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Object message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Object message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, JsonObject message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, JsonObject message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, JsonObject message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, JsonObject message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, JsonArray message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, JsonArray message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, JsonArray message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, JsonArray message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Buffer message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Buffer message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Buffer message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Buffer message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, byte[] message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, byte[] message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, byte[] message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, byte[] message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, String message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, String message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, String message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, String message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Integer message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Integer message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Integer message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Integer message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Long message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Long message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Long message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Long message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Float message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Float message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Float message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Float message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Double message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Double message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Double message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Double message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Boolean message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Boolean message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Boolean message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Boolean message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Short message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Short message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Short message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Short message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Character message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Character message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Character message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Character message) {
    eventBus.send(address, message);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus send(String address, Byte message, final Handler<Message<T>> replyHandler) {
    if (defaultTimeout > 0) {
      sendWithAdaptiveTimeout(address, message, defaultTimeout, new Handler<AsyncResult<Message<T>>>() {
        @Override
        public void handle(AsyncResult<Message<T>> result) {
          replyHandler.handle(result.result());
        }
      });
    } else {
      eventBus.send(address, message, replyHandler);
    }
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithTimeout(String address, Byte message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler) {
    eventBus.sendWithTimeout(address, message, timeout, replyHandler);
    return this;
  }

  @Override
  public <T> AdaptiveEventBus sendWithAdaptiveTimeout(final String address, Byte message, float timeout, final Handler<AsyncResult<Message<T>>> replyHandler) {
    final long startTime = System.currentTimeMillis();
    eventBus.sendWithTimeout(address, message, getReplyTime(address, timeout), new Handler<AsyncResult<Message<T>>>() {
      @Override
      public void handle(AsyncResult<Message<T>> result) {
        if (result.succeeded()) addReplyTime(address, System.currentTimeMillis()-startTime);
        replyHandler.handle(result);
      }
    });
    return this;
  }

  @Override
  public AdaptiveEventBus send(String address, Byte message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Object message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, JsonObject message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, JsonArray message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Buffer message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, byte[] message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, String message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Integer message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Long message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Float message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Double message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Boolean message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Short message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Character message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  public AdaptiveEventBus publish(String address, Byte message) {
    eventBus.publish(address, message);
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public AdaptiveEventBus unregisterHandler(String address, Handler<? extends Message> handler, Handler<AsyncResult<Void>> resultHandler) {
    eventBus.unregisterHandler(address, handler, resultHandler);
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public AdaptiveEventBus unregisterHandler(String address, Handler<? extends Message> handler) {
    eventBus.unregisterHandler(address, handler);
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public AdaptiveEventBus registerHandler(String address, Handler<? extends Message> handler, Handler<AsyncResult<Void>> resultHandler) {
    eventBus.registerHandler(address, handler, resultHandler);
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public AdaptiveEventBus registerHandler(String address, Handler<? extends Message> handler) {
    eventBus.registerHandler(address, handler);
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public AdaptiveEventBus registerLocalHandler(String address, Handler<? extends Message> handler) {
    eventBus.registerLocalHandler(address, handler);
    return this;
  }

  @Override
  public AdaptiveEventBus setDefaultReplyTimeout(long timeoutMs) {
    eventBus.setDefaultReplyTimeout(timeoutMs);
    return this;
  }

  @Override
  public AdaptiveEventBus setDefaultAdaptiveTimeout(float timeout) {
    defaultTimeout = timeout;
    return this;
  }

  @Override
  public float getDefaultAdaptiveTimeout() {
    return defaultTimeout;
  }

}
