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
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.impl.ClusterAgent;
import net.kuujo.vertigo.cluster.impl.DefaultCluster;
import net.kuujo.vertigo.test.VertigoTestVerticle;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A remote cluster test.
 *
 * @author Jordan Halterman
 */
public class ClusterTest extends VertigoTestVerticle {

  public static class TestVerticle1 extends Verticle {
    @Override
    public void start() {
      super.start();
    }
  }

  public static class TestVerticle2 extends Verticle {
    @Override
    public void start() {
      testComplete();
    }
  }

  @Test
  public void testDeployVerticle() {
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster client = new DefaultCluster("test", vertx, container);
        client.deployVerticle("test-verticle1", TestVerticle2.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertEquals("test-verticle1", result.result());
          }
        });
      }
    });
  }

  @Test
  public void testUndeployVerticle() {
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster client = new DefaultCluster("test", vertx, container);
        client.deployVerticle("test-verticle2", TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertEquals("test-verticle2", result.result());
            client.undeployVerticle("test-verticle2", new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testVerticleIsDeployed() {
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster client = new DefaultCluster("test", vertx, container);
        client.deployVerticle("test-verticle3", TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertEquals("test-verticle3", result.result());
            client.isDeployed("test-verticle3", new Handler<AsyncResult<Boolean>>() {
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
  public void testVerticleIsNotDeployed() {
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster client = new DefaultCluster("test", vertx, container);
        client.deployVerticle("test-verticle4", TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertEquals("test-verticle4", result.result());
            client.undeployVerticle("test-verticle4", new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                client.isDeployed("test-verticle4", new Handler<AsyncResult<Boolean>>() {
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
  public void testDeployWorkerVerticle() {
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster client = new DefaultCluster("test", vertx, container);
        client.deployWorkerVerticle("test-worker1", TestVerticle2.class.getName(), new JsonObject().putString("foo", "bar"), 1, false, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertEquals("test-worker1", result.result());
          }
        });
      }
    });
  }

  @Test
  public void testUndeployWorkerVerticle() {
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        final Cluster client = new DefaultCluster("test", vertx, container);
        client.deployWorkerVerticle("test-worker2", TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, false, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertEquals("test-worker2", result.result());
            client.undeployVerticle("test-worker2", new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

}
