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

import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.impl.DefaultCluster;
import net.kuujo.vertigo.test.PlatformInfo;
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
@PlatformInfo(cluster=true, mods="src/test/resources/server-mods")
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
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();
        cluster.deployVerticle(TestVerticle2.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNotNull(result.result());
          }
        });
      }
    });
  }

  @Test
  public void testUndeployVerticle() {
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();
        cluster.deployVerticle(TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNotNull(result.result());
            cluster.undeployVerticle(result.result(), new Handler<AsyncResult<Void>>() {
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
  public void testDeployWorkerVerticle() {
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();
        cluster.deployWorkerVerticle(TestVerticle2.class.getName(), new JsonObject().putString("foo", "bar"), 1, false, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNotNull(result.result());
          }
        });
      }
    });
  }

  @Test
  public void testUndeployWorkerVerticle() {
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();
        cluster.deployWorkerVerticle(TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, false, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertNotNull(result.result());
            cluster.undeployVerticle(result.result(), new Handler<AsyncResult<Void>>() {
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
  public void testInstallDeployModule() {
    System.setProperty("vertx.mods", "src/test/resources/server-mods");
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        System.setProperty("vertx.mods", "src/test/resources/test-mods");
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        cluster.deployModule("net.kuujo~test-mod-1~1.0", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.failed());
            cluster.installModule("net.kuujo~test-mod-1~1.0", new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                cluster.deployModule("net.kuujo~test-mod-1~1.0", new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    assertTrue(result.succeeded());
                    assertNotNull(result.result());
                    vertx.fileSystem().deleteSync("src/test/resources/server-mods", true);
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
