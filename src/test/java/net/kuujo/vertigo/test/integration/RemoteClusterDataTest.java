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
package net.kuujo.vertigo.test.integration;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.cluster.RemoteCluster;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.data.MapEvent;
import net.kuujo.vertigo.data.WatchableAsyncMap;
import net.kuujo.xync.test.integration.XyncTestVerticle;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A remote cluster test.
 *
 * @author Jordan Halterman
 */
public class RemoteClusterDataTest extends XyncTestVerticle {

  @Test
  public void testSetGetDelete() {
    final Cluster cluster = new RemoteCluster(vertx);
    final WatchableAsyncMap<String, String> data = cluster.getMap("test");
    data.put("foo", "bar", new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        data.get("foo", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertEquals("bar", result.result());
            data.remove("foo", new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                assertTrue(result.succeeded());
                data.get("foo", new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    assertTrue(result.succeeded());
                    assertNull(result.result());
                    testComplete();
                  }
                });
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testWatchCreate() {
    final Cluster cluster = new RemoteCluster(vertx);
    final WatchableAsyncMap<String, String> data = cluster.getMap("test");
    data.watch("test1", new Handler<MapEvent<String, String>>() {
      @Override
      public void handle(MapEvent<String, String> event) {
        if (event.type().equals(MapEvent.Type.CREATE)) {
          assertEquals(MapEvent.Type.CREATE, event.type());
          assertEquals("test1", event.key());
          assertEquals("Hello world 1!", event.value());
          testComplete();
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        } else {
          data.put("test1", "Hello world 1!");
        }
      }
    });
  }

  @Test
  public void testWatchUpdate() {
    final Cluster cluster = new RemoteCluster(vertx);
    final WatchableAsyncMap<String, String> data = cluster.getMap("test");
    data.watch("test2", new Handler<MapEvent<String, String>>() {
      @Override
      public void handle(MapEvent<String, String> event) {
        if (event.type().equals(MapEvent.Type.UPDATE)) {
          assertEquals(MapEvent.Type.UPDATE, event.type());
          assertEquals("test2", event.key());
          assertEquals("Hello world 2 again!", event.value());
          testComplete();
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        } else {
          data.put("test2", "Hello world 2!", new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                fail(result.cause().getMessage());
              } else {
                data.put("test2", "Hello world 2 again!");
              }
            }
          });
        }
      }
    });
  }

  @Test
  public void testWatchDelete() {
    final Cluster cluster = new RemoteCluster(vertx);
    final WatchableAsyncMap<String, String> data = cluster.getMap("test");
    data.watch("test3", new Handler<MapEvent<String, String>>() {
      @Override
      public void handle(MapEvent<String, String> event) {
        if (event.type().equals(MapEvent.Type.DELETE)) {
          assertEquals(MapEvent.Type.DELETE, event.type());
          assertEquals("test3", event.key());
          assertEquals("Hello world 3!", event.value());
          testComplete();
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        } else {
          data.put("test3", "Hello world 3!", new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                fail(result.cause().getMessage());
              }
              else {
                data.remove("test3");
              }
            }
          });
        }
      }
    });
  }

}
