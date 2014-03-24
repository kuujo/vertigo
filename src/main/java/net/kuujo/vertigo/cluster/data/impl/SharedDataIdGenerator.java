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

import net.kuujo.vertigo.cluster.data.AsyncIdGenerator;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * Shared data based ID generator.
 *
 * @author Jordan Halterman
 */
public class SharedDataIdGenerator implements AsyncIdGenerator {
  private static final String LIST_MAP_NAME = "__ID__";
  private final String name;
  private final Vertx vertx;
  private final ConcurrentSharedMap<String, Long> map;

  public SharedDataIdGenerator(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.map = vertx.sharedData().getMap(LIST_MAP_NAME);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void nextId(final Handler<AsyncResult<Long>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        long value;
        if (!map.containsKey(name)) {
          map.put(name, 1L);
          value = 1L;
        }
        else {
          value = map.get(name)+1;
          map.put(name, value);
        }
        new DefaultFutureResult<Long>(value).setHandler(resultHandler);
      }
    });
  }

}
