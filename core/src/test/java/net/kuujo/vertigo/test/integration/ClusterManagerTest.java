/*
 * Copyright 2013-2014 the original author or authors.
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
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.Collection;
import java.util.UUID;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.test.VertigoTestVerticle;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A remote cluster test.
 *
 * @author Jordan Halterman
 */
public class ClusterManagerTest extends VertigoTestVerticle {

  @Test
  public void testDeploy() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork("test-local-deploy");
        network.addVerticle("feeder", TestFeeder.class.getName());
        network.addVerticle("worker1", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream1", "worker", "stream1");
        network.createConnection("feeder", "stream2", "worker", "stream2");

        final ClusterManager cluster = result.result();
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            assertTrue(result.succeeded());
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testShutdown() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork("test-local-shutdown");
        network.addVerticle("feeder", TestFeeder.class.getName());
        network.addVerticle("worker1", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream1", "worker", "stream1");
        network.addVerticle("worker2", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream2", "worker", "stream2");

        final ClusterManager cluster = result.result();
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            assertTrue(result.succeeded());
            vertx.setTimer(2000, new Handler<Long>() {
              @Override
              public void handle(Long timerID) {
                cluster.undeployNetwork("test-local-shutdown", new Handler<AsyncResult<Void>>() {
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
    });
  }

  @Test
  public void testGetNetwork() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork("test-get-network-1");
        network.addVerticle("feeder", TestFeeder.class.getName());
        network.addVerticle("worker", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream", "worker", "stream");

        final ClusterManager cluster = result.result();
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            assertTrue(result.succeeded());
            NetworkConfig network = vertigo.createNetwork("test-get-network-2");
            network.addVerticle("feeder", TestFeeder.class.getName());
            network.addVerticle("worker", TestWorker.class.getName(), 2);
            network.createConnection("feeder", "stream", "worker", "stream");
            cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
              @Override
              public void handle(AsyncResult<ActiveNetwork> result) {
                assertTrue(result.succeeded());
                cluster.getNetwork("test-get-network-1", new Handler<AsyncResult<ActiveNetwork>>() {
                  @Override
                  public void handle(AsyncResult<ActiveNetwork> result) {
                    assertTrue(result.succeeded());
                    assertEquals("test-get-network-1", result.result().getConfig().getName());
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
  public void testGetNetworks() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork("test-get-networks-1");
        network.addVerticle("feeder", TestFeeder.class.getName());
        network.addVerticle("worker", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream", "worker", "stream");

        final ClusterManager cluster = result.result();
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            assertTrue(result.succeeded());
            NetworkConfig network = vertigo.createNetwork("test-get-networks-2");
            network.addVerticle("feeder", TestFeeder.class.getName());
            network.addVerticle("worker", TestWorker.class.getName(), 2);
            network.createConnection("feeder", "stream", "worker", "stream");
            cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
              @Override
              public void handle(AsyncResult<ActiveNetwork> result) {
                assertTrue(result.succeeded());
                cluster.getNetworks(new Handler<AsyncResult<Collection<ActiveNetwork>>>() {
                  @Override
                  public void handle(AsyncResult<Collection<ActiveNetwork>> result) {
                    assertTrue(result.succeeded());
                    assertEquals(2, result.result().size());
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
  public void testDeployWithBrokenConnection() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork("test-get-networks-1");
        network.addVerticle("feeder", TestFeeder.class.getName());
        network.addVerticle("worker", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream", "worker", "stream");
        network.createConnection("worker", "out", "nowhere", "in");

        ClusterManager cluster = result.result();
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            assertTrue(result.succeeded());
            testComplete();
          }
        });
      }
    });
  }

  public static class TestFeeder extends ComponentVerticle {
  }

  public static class TestWorker extends ComponentVerticle {
  }

}
