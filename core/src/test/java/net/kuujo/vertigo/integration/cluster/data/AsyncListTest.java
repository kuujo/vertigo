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
package net.kuujo.vertigo.integration.cluster.data;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertFalse;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.impl.DefaultCluster;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

/**
 * Asynchronous list tests.
 *
 * @author Jordan Halterman
 */
public class AsyncListTest extends TestVerticle {

  @Test
  public void testListAdd() {
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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

}
