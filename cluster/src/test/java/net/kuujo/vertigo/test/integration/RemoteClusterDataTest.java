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
import static org.vertx.testtools.VertxAssert.assertFalse;
import static org.vertx.testtools.VertxAssert.assertNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.data.AsyncCounter;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.WrappedWatchableAsyncMap;
import net.kuujo.vertigo.cluster.impl.ClusterVerticle;
import net.kuujo.vertigo.cluster.impl.DefaultCluster;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

/**
 * A remote cluster test.
 *
 * @author Jordan Halterman
 */
public class RemoteClusterDataTest extends TestVerticle {

  @Test
  public void testMapPut() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncMap<String, String> data = cluster.getMap("test-map-put");
        data.put("foo", "bar", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNull(result.result());
            data.put("foo", "baz", new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                assertTrue(result.succeeded());
                assertEquals("bar", result.result());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testMapGet() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncMap<String, String> data = cluster.getMap("test-map-get");
        data.put("foo", "bar", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNull(result.result());
            data.get("foo", new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                assertTrue(result.succeeded());
                assertEquals("bar", result.result());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testMapRemove() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncMap<String, String> data = cluster.getMap("test-map-remove");
        data.put("foo", "bar", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNull(result.result());
            data.remove("foo", new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                assertTrue(result.succeeded());
                assertEquals("bar", result.result());
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
  public void testMapContainsKey() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncMap<String, String> data = cluster.getMap("test-map-contains-key");
        data.put("foo", "bar", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNull(result.result());
            data.containsKey("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.remove("foo", new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    assertTrue(result.succeeded());
                    data.containsKey("foo", new Handler<AsyncResult<Boolean>>() {
                      @Override
                      public void handle(AsyncResult<Boolean> result) {
                        assertTrue(result.succeeded());
                        assertFalse(result.result());
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
  public void testMapSize() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncMap<String, String> data = cluster.getMap("test-map-size");
        data.put("foo", "bar", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNull(result.result());
            data.put("bar", "baz", new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                assertTrue(result.succeeded());
                data.size(new Handler<AsyncResult<Integer>>() {
                  @Override
                  public void handle(AsyncResult<Integer> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result() == 2);
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
  public void testMapClear() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncMap<String, String> data = cluster.getMap("test-map-clear");
        data.put("foo", "bar", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNull(result.result());
            data.put("bar", "baz", new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                assertTrue(result.succeeded());
                data.size(new Handler<AsyncResult<Integer>>() {
                  @Override
                  public void handle(AsyncResult<Integer> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result() == 2);
                    data.clear(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        assertTrue(result.succeeded());
                        data.size(new Handler<AsyncResult<Integer>>() {
                          @Override
                          public void handle(AsyncResult<Integer> result) {
                             assertTrue(result.succeeded());
                             assertTrue(result.result() == 0);
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
    });
  }

  @Test
  public void testMapWatchCreate() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
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
  public void testMapWatchUpdate() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
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
  public void testMapWatchDelete() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
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

  @Test
  public void testSetAdd() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncSet<String> data = cluster.getSet("test-set-add");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertFalse(result.result());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testSetContains() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncSet<String> data = cluster.getSet("test-set-contains");
        data.contains("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertFalse(result.result());
            data.add("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.contains("foo", new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result());
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
  public void testSetRemove() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncSet<String> data = cluster.getSet("test-set-remove");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.remove("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.remove("foo", new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertFalse(result.result());
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
  public void testSetSize() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncSet<String> data = cluster.getSet("test-set-size");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("bar", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.add("baz", new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result());
                    data.size(new Handler<AsyncResult<Integer>>() {
                      @Override
                      public void handle(AsyncResult<Integer> result) {
                        assertTrue(result.succeeded());
                        assertTrue(result.result() == 3);
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
  public void testSetClear() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncSet<String> data = cluster.getSet("test-set-clear");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("bar", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.add("baz", new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result());
                    data.size(new Handler<AsyncResult<Integer>>() {
                      @Override
                      public void handle(AsyncResult<Integer> result) {
                        assertTrue(result.succeeded());
                        assertTrue(result.result() == 3);
                        data.clear(new Handler<AsyncResult<Void>>() {
                          @Override
                          public void handle(AsyncResult<Void> result) {
                            assertTrue(result.succeeded());
                            data.size(new Handler<AsyncResult<Integer>>() {
                              @Override
                              public void handle(AsyncResult<Integer> result) {
                                assertTrue(result.succeeded());
                                assertTrue(result.result() == 0);
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
        });
      }
    });
  }

  @Test
  public void testSetIsEmpty() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncSet<String> data = cluster.getSet("test-set-is-empty");
        data.isEmpty(new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.isEmpty(new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertFalse(result.result());
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
  public void testListAdd() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncList<String> data = cluster.getList("test-list-add");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testListGet() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncList<String> data = cluster.getList("test-list-get");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.get(0, new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    assertTrue(result.succeeded());
                    assertEquals("foo", result.result());
                    data.get(1, new Handler<AsyncResult<String>>() {
                      @Override
                      public void handle(AsyncResult<String> result) {
                        assertTrue(result.succeeded());
                        assertEquals("foo", result.result());
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
  public void testListContains() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncList<String> data = cluster.getList("test-list-contains");
        data.contains("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertFalse(result.result());
            data.add("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.contains("foo", new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result());
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
  public void testListSize() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncList<String> data = cluster.getList("test-list-size");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("bar", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.add("baz", new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result());
                    data.size(new Handler<AsyncResult<Integer>>() {
                      @Override
                      public void handle(AsyncResult<Integer> result) {
                        assertTrue(result.succeeded());
                        assertTrue(result.result() == 3);
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
  public void testListRemoveByValue() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncList<String> data = cluster.getList("test-list-remove-by-value");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.remove("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.remove("foo", new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertFalse(result.result());
                    data.size(new Handler<AsyncResult<Integer>>() {
                      @Override
                      public void handle(AsyncResult<Integer> result) {
                        assertTrue(result.succeeded());
                        assertTrue(result.result() == 0);
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
  public void testListRemoveByIndex() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncList<String> data = cluster.getList("test-list-remove-by-index");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.remove(0, new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                assertTrue(result.succeeded());
                assertEquals("foo", result.result());
                data.size(new Handler<AsyncResult<Integer>>() {
                  @Override
                  public void handle(AsyncResult<Integer> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result() == 0);
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
  public void testListClear() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncList<String> data = cluster.getList("test-list-clear");
        data.add("foo", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("bar", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.add("baz", new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result());
                    data.size(new Handler<AsyncResult<Integer>>() {
                      @Override
                      public void handle(AsyncResult<Integer> result) {
                        assertTrue(result.succeeded());
                        assertTrue(result.result() == 3);
                        data.clear(new Handler<AsyncResult<Void>>() {
                          @Override
                          public void handle(AsyncResult<Void> result) {
                            assertTrue(result.succeeded());
                            data.size(new Handler<AsyncResult<Integer>>() {
                              @Override
                              public void handle(AsyncResult<Integer> result) {
                                assertTrue(result.succeeded());
                                assertTrue(result.result() == 0);
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
        });
      }
    });
  }

  @Test
  public void testListIsEmpty() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncList<String> data = cluster.getList("test-list-is-empty");
        data.isEmpty(new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            data.add("foo", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertTrue(result.result());
                data.isEmpty(new Handler<AsyncResult<Boolean>>() {
                  @Override
                  public void handle(AsyncResult<Boolean> result) {
                    assertTrue(result.succeeded());
                    assertFalse(result.result());
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
  public void testCounterIncrement() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncCounter data = cluster.getCounter("test-counter-increment");
        data.incrementAndGet(new Handler<AsyncResult<Long>>() {
          @Override
          public void handle(AsyncResult<Long> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result() == 1);
            data.increment(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                data.get(new Handler<AsyncResult<Long>>() {
                  @Override
                  public void handle(AsyncResult<Long> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result() == 2);
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
  public void testCounterDecrement() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterVerticle.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        final AsyncCounter data = cluster.getCounter("test-counter-decrement");
        data.decrementAndGet(new Handler<AsyncResult<Long>>() {
          @Override
          public void handle(AsyncResult<Long> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result() == -1);
            data.decrement(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                data.get(new Handler<AsyncResult<Long>>() {
                  @Override
                  public void handle(AsyncResult<Long> result) {
                    assertTrue(result.succeeded());
                    assertTrue(result.result() == -2);
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

}
