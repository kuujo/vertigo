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
package net.kuujo.vertigo.cluster.data.impl;

import net.kuujo.vertigo.cluster.data.AsyncCounter;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * Shared data based counter.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class SharedDataCounter implements AsyncCounter {
  private static final String COUNTER_MAP_NAME = "__COUNTERS__";
  private final String name;
  private final Vertx vertx;
  private final ConcurrentSharedMap<String, Long> map;

  public SharedDataCounter(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.map = vertx.sharedData().getMap(COUNTER_MAP_NAME);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void get(final Handler<AsyncResult<Long>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        if (map.containsKey(name)) {
          new DefaultFutureResult<Long>(map.get(name)).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Long>(0L).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void increment() {
    increment(null);
  }

  @Override
  public void increment(final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        synchronized (map) {
          long value;
          if (!map.containsKey(name)) {
            value = 1L;
          } else {
            value = map.get(name)+1;
          }
          map.put(name, value);
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void incrementAndGet(final Handler<AsyncResult<Long>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        synchronized (map) {
          long value;
          if (!map.containsKey(name)) {
            value = 1L;
          } else {
            value = map.get(name)+1;
          }
          map.put(name, value);
          new DefaultFutureResult<Long>(value).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void decrement() {
    decrement(null);
  }

  @Override
  public void decrement(final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        synchronized (map) {
          long value;
          if (!map.containsKey(name)) {
            value = 1L;
          } else {
            value = map.get(name)-1;
          }
          map.put(name, value);
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public void decrementAndGet(final Handler<AsyncResult<Long>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        synchronized (map) {
          long value;
          if (!map.containsKey(name)) {
            value = -1L;
          } else {
            value = map.get(name)-1;
          }
          map.put(name, value);
          new DefaultFutureResult<Long>(value).setHandler(doneHandler);
        }
      }
    });
  }

}
