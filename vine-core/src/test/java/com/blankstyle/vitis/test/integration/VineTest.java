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
package com.blankstyle.vitis.test.integration;

import net.kuujo.vitis.Nodes;
import net.kuujo.vitis.Vines;
import net.kuujo.vitis.context.VineContext;
import net.kuujo.vitis.definition.VineDefinition;
import net.kuujo.vitis.eventbus.seed.SeedVerticle;
import net.kuujo.vitis.eventbus.vitis.VitisVerticle;
import net.kuujo.vitis.feeder.Executor;
import net.kuujo.vitis.feeder.Feeder;
import net.kuujo.vitis.feeder.ReliableExecutor;
import net.kuujo.vitis.feeder.ReliableFeeder;
import net.kuujo.vitis.feeder.UnreliableFeeder;
import net.kuujo.vitis.grouping.FieldsGrouping;
import net.kuujo.vitis.local.LocalVitis;
import net.kuujo.vitis.remote.RemoteVitis;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A vine deploy test.
 *
 * @author Jordan Halterman
 */
public class VineTest extends TestVerticle {

  private VineDefinition createTestDefinition() {
    VineDefinition vine = Vines.createDefinition("test.vine");
    vine.setMessageTimeout(5000).setMessageExpiration(15000);
    vine.feed(Nodes.createDefinition("seedone", TestSeedOne.class.getName()).setWorkers(2))
      .to(Nodes.createDefinition("seedtwo", TestSeedTwo.class.getName()).setWorkers(2));
    return vine;
  }

  public void testPerformance() {
    VineDefinition vine = createTestDefinition();
    LocalVitis root = new LocalVitis(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
      @Override
      public void handle(AsyncResult<VineContext> result) {
        assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());

        assertNotNull(result.result());
        Feeder feeder = new ReliableFeeder(result.result(), vertx);
        new FeederTester(feeder).start();
      }
    });
  }

  private class FeederTester {
    private Feeder feeder;
    private long count;
    private long total = 10000;
    private long startTime;
    private long endTime;
    public FeederTester(Feeder feeder) {
      this.feeder = feeder;
    }
    public void start() {
      for (int i = 0; i < total; i++) {
        feeder.feed(new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.succeeded()) {
              count++;
              if (count == total / 2) {
                startTime = System.currentTimeMillis();
              }
              else if (count >= total) {
                endTime = System.currentTimeMillis();
                System.out.println(String.format("Processed %d messages in %d milliseconds.", total / 2, endTime - startTime));
                testComplete();
              }
            }
          }
        });
      }
    }
  }

  @Test
  public void testLocalDeploy() {
    VineDefinition vine = createTestDefinition();
    LocalVitis root = new LocalVitis(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
      @Override
      public void handle(AsyncResult<VineContext> result) {
        assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());
        assertNotNull(result.result());
        testComplete();
      }
    });
  }

  @Test
  public void testLocalFeed() {
    VineDefinition vine = createTestDefinition();
    LocalVitis root = new LocalVitis(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
      @Override
      public void handle(AsyncResult<VineContext> result) {
        assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());

        assertNotNull(result.result());
        Feeder feeder = new UnreliableFeeder(result.result(), vertx);

        feeder.feed(new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue("Failed to process message. " + result.cause(), result.succeeded());
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testLocalExecute() {
    VineDefinition vine = createTestDefinition();
    LocalVitis root = new LocalVitis(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
      @Override
      public void handle(AsyncResult<VineContext> result) {
        assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());

        assertNotNull(result.result());
        Executor executor = new ReliableExecutor(result.result(), vertx);

        executor.execute(new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<JsonObject>>() {
          @Override
          public void handle(AsyncResult<JsonObject> result) {
            assertTrue("Failed to process message. " + result.cause(), result.succeeded());
            assertEquals("Hello world again again!", result.result().getString("body"));
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testLocalDeployTimeout() {
    VineDefinition vine = createTestDefinition();
    LocalVitis root = new LocalVitis(vertx, container);
    root.deploy(vine, 1, new Handler<AsyncResult<VineContext>>() {
      @Override
      public void handle(AsyncResult<VineContext> result) {
        assertTrue(result.failed());
        testComplete();
      }
    });
  }

  @Test
  public void testLocalShutdown() {
    final VineDefinition vine = createTestDefinition();
    final LocalVitis root = new LocalVitis(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
      @Override
      public void handle(AsyncResult<VineContext> result) {
        assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());
        assertNotNull(result.result());

        root.shutdown(result.result(), new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue("Failed to shutdown vine. " + result.cause(), result.succeeded());
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testRemoteDeploy() {
    deployRoot(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue("Failed to deploy root. " + result.cause(), result.succeeded());

        deployStem(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue("Failed to deploy stem. " + result.cause(), result.succeeded());

            VineDefinition vine = createTestDefinition();
            RemoteVitis root = new RemoteVitis("vine.root.test", vertx, container);
            root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
              @Override
              public void handle(AsyncResult<VineContext> result) {
                assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());
                assertNotNull(result.result());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testRemoteFeed() {
    deployRoot(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue("Failed to deploy root. " + result.cause(), result.succeeded());

        deployStem(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue("Failed to deploy stem. " + result.cause(), result.succeeded());

            VineDefinition vine = createTestDefinition();
            RemoteVitis root = new RemoteVitis("vine.root.test", vertx, container);
            root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
              @Override
              public void handle(AsyncResult<VineContext> result) {
                assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());

                assertNotNull(result.result());
                Feeder feeder = new UnreliableFeeder(result.result(), vertx);

                feeder.feed(new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue("Failed to process message. " + result.cause(), result.succeeded());
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
  public void testRemoteExecute() {
    deployRoot(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue("Failed to deploy root. " + result.cause(), result.succeeded());

        deployStem(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue("Failed to deploy stem. " + result.cause(), result.succeeded());

            VineDefinition vine = createTestDefinition();
            RemoteVitis root = new RemoteVitis("vine.root.test", vertx, container);
            root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
              @Override
              public void handle(AsyncResult<VineContext> result) {
                assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());

                assertNotNull(result.result());
                Executor executor = new ReliableExecutor(result.result(), vertx);

                executor.execute(new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<JsonObject>>() {
                  @Override
                  public void handle(AsyncResult<JsonObject> result) {
                    assertTrue("Failed to process message. " + result.cause(), result.succeeded());
                    assertEquals("Hello world again again!", result.result().getString("body"));
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
  public void testRemoteDeployTimeout() {
    deployRoot(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue("Failed to deploy root. " + result.cause(), result.succeeded());

        deployStem(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue("Failed to deploy stem. " + result.cause(), result.succeeded());

            VineDefinition vine = createTestDefinition();
            RemoteVitis root = new RemoteVitis("vine.root.test", vertx, container);
            root.deploy(vine, 1, new Handler<AsyncResult<VineContext>>() {
              @Override
              public void handle(AsyncResult<VineContext> result) {
                assertTrue(result.failed());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testRemoteShutdown() {
    deployRoot(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue("Failed to deploy root. " + result.cause(), result.succeeded());

        deployStem(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue("Failed to deploy stem. " + result.cause(), result.succeeded());

            final VineDefinition vine = createTestDefinition();
            final RemoteVitis root = new RemoteVitis("vine.root.test", vertx, container);
            root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
              @Override
              public void handle(AsyncResult<VineContext> result) {
                assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());
                assertNotNull(result.result());

                root.shutdown(result.result(), new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue("Failed to shutdown vine. " + result.cause(), result.succeeded());
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
  public void testFieldsDispatcher() {
    VineDefinition vine = Vines.createDefinition("test.vine");
    vine.setMessageTimeout(5000).setMessageExpiration(15000);
    vine.feed(Nodes.createDefinition("seedone", TestConsistentSeed.class.getName()).setWorkers(2).groupBy(new FieldsGrouping("body")));

    LocalVitis root = new LocalVitis(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
      @Override
      public void handle(AsyncResult<VineContext> result) {
        assertTrue("Failed to deploy vine. " + result.cause(), result.succeeded());

        assertNotNull(result.result());

        Executor executor = new ReliableExecutor(result.result(), vertx);

        Handler<AsyncResult<JsonObject>> emptyHandler = new Handler<AsyncResult<JsonObject>>() {
          @Override
          public void handle(AsyncResult<JsonObject> result) {
            // Do nothing.
          }
        };

        executor.execute(new JsonObject().putString("body", "a"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "ab"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "a"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "a"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "ab"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "ab"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "ab"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "a"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "a"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "ab"), emptyHandler);
        executor.execute(new JsonObject().putString("body", "a"), new Handler<AsyncResult<JsonObject>>() {
          @Override
          public void handle(AsyncResult<JsonObject> result) {
            testComplete();
          }
        });
      }
    });
  }

  /**
   * Deploys a test root.
   */
  private void deployRoot(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    container.deployVerticle(VitisVerticle.class.getName(), new JsonObject().putString("address", "vine.root.test"), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue("Failed to deploy root. " + result.cause(), result.succeeded());
        future.setResult(null);
      }
    });
  }

  /**
   * Deploys a test stem.
   */
  private void deployStem(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    container.deployVerticle(SeedVerticle.class.getName(),
      new JsonObject().putString("address", "vine.stem.test").putString("root", "vine.root.test"),
      new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue("Failed to deploy stem. " + result.cause(), result.succeeded());
        future.setResult(null);
      }
    });
  }

}
