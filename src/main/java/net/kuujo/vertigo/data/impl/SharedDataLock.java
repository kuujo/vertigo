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
package net.kuujo.vertigo.data.impl;

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.LocalType;
import net.kuujo.vertigo.data.AsyncLock;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * Shared data based lock implementation.
 *
 * @author Jordan Halterman
 */
@LocalType
@ClusterType
public class SharedDataLock implements AsyncLock {
  private static final String LOCK_MAP_NAME = "__LOCK__";
  private final String name;
  private final Vertx vertx;
  private final ConcurrentSharedMap<String, Long> map;

  @Factory
  public static SharedDataLock factory(String name, Vertx vertx) {
    return new SharedDataLock(name, vertx);
  }

  private SharedDataLock(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.map = vertx.sharedData().getMap(LOCK_MAP_NAME);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void lock(final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        if (checkLock()) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        } else {
          vertx.setPeriodic(10, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
              if (checkLock()) {
                map.put(name, null);
                vertx.cancelTimer(timerID);
                new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
              }
            }
          });
        }
      }
    });
  }

  @Override
  public void tryLock(final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(checkLock()).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void tryLock(final long timeout, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        final long startTime = System.currentTimeMillis();
        if (checkLock()) {
          new DefaultFutureResult<Boolean>(true).setHandler(resultHandler);
        } else {
          vertx.setPeriodic(10, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
              if (System.currentTimeMillis() - startTime > timeout) {
                vertx.cancelTimer(timerID);
                new DefaultFutureResult<Boolean>(false).setHandler(resultHandler);
              } else if (checkLock()) {
                vertx.cancelTimer(timerID);
                new DefaultFutureResult<Boolean>(true).setHandler(resultHandler);
              }
            }
          });
        }
      }
    });
  }

  @Override
  public void unlock() {
    unlock(null);
  }

  @Override
  public void unlock(final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        synchronized (map) {
          if (map.containsKey(name)) {
            map.remove(name);
          }
        }
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

  private boolean checkLock() {
    synchronized (map) {
      if (!map.containsKey(name)) {
        map.put(name, null);
        return true;
      }
    }
    return false;
  }

}
