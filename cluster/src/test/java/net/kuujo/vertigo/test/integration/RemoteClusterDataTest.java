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
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.ClusterAgent;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.WrappedWatchableAsyncMap;
import net.kuujo.vertigo.cluster.impl.RemoteCluster;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

/**
 * A remote cluster data test.
 *
 * @author Jordan Halterman
 */
public class RemoteClusterDataTest extends TestVerticle {

  @Test
  public void testSetGetDelete() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new RemoteCluster("test", vertx, container);
        final AsyncMap<String, String> data = cluster.getMap("test-set-get");
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
    });
  }

  @Test
  public void testWatchCreate() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new RemoteCluster("test", vertx, container);
        final WatchableAsyncMap<String, String> data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap("test-watch-create"), vertx);
        data.watch("foo", new Handler<MapEvent<String, String>>() {
          @Override
          public void handle(MapEvent<String, String> event) {
            if (event.type().equals(MapEvent.Type.CREATE)) {
              assertEquals("foo", event.key());
              assertEquals("bar", event.value());
              testComplete();
            }
          }
        }, new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            data.put("foo", "bar");
          }
        });
      }
    });
  }

  @Test
  public void testWatchUpdate() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new RemoteCluster("test", vertx, container);
        final WatchableAsyncMap<String, String> data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap("test-watch-update"), vertx);
        data.put("foo", "bar", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            data.watch("foo", new Handler<MapEvent<String, String>>() {
              @Override
              public void handle(MapEvent<String, String> event) {
                if (event.type().equals(MapEvent.Type.UPDATE)) {
                  assertEquals("foo", event.key());
                  assertEquals("bar", event.value());
                  testComplete();
                }
              }
            }, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                data.put("foo", "bar");
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testWatchDelete() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new RemoteCluster("test", vertx, container);
        final WatchableAsyncMap<String, String> data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap("test-watch-delete"), vertx);
        data.put("foo", "bar", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            data.watch("foo", new Handler<MapEvent<String, String>>() {
              @Override
              public void handle(MapEvent<String, String> event) {
                if (event.type().equals(MapEvent.Type.DELETE)) {
                  assertEquals("foo", event.key());
                  assertEquals("bar", event.value());
                  testComplete();
                }
              }
            }, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                data.remove("foo");
              }
            });
          }
        });
      }
    });
  }

}
