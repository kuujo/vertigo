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
package net.kuujo.vertigo.integration.network;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.UUID;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

/**
 * Network reconfiguration tests.
 *
 * @author Jordan Halterman
 */
public class ReconfigureTest extends TestVerticle {

  public static class TestReconfigureSender extends ComponentVerticle {
    @Override
    public void start() {
      vertx.setPeriodic(1000, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          output.port("out").send("Hello world!");
        }
      });
    }
  }

  public static class TestReconfigureReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          assertEquals("Hello world!", message);
          testComplete();
        }
      });
    }
  }

  @Test
  public void testReconfigureAddComponent() {
    final String name = UUID.randomUUID().toString();
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();

        // Deploy a network with a single component and connection. The connection
        // doesn't go anywhere, so messages sent to the sender's "out" port will
        // simply be discarded.
        NetworkConfig network = vertigo.createNetwork(name);
        network.addVerticle("sender", TestReconfigureSender.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {

              // To add a component to listen to the sender's "out" port we simply
              // add a "receiver" component to a new configuration with the same
              // name as the original network's name. This will cause Vertigo to
              // merge the new configuration with the running configuration and
              // add the new component to the running network.
              NetworkConfig network = vertigo.createNetwork(name);
              network.addVerticle("receiver", TestReconfigureReceiver.class.getName());
              cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
                @Override
                public void handle(AsyncResult<ActiveNetwork> result) {
                  if (result.failed()) {
                    assertTrue(result.cause().getMessage(), result.succeeded());
                  } else {
                    assertTrue(result.succeeded());
                  }
                }
              });
            }
          }
        });
      }
    });
  }

  @Test
  public void testReconfigureRemoveComponent() {
    final String name = UUID.randomUUID().toString();
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();
        NetworkConfig network = vertigo.createNetwork(name);
        network.addVerticle("sender", TestSimpleSender.class.getName());
        network.addVerticle("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
              NetworkConfig network = vertigo.createNetwork(name);
              network.addComponent("receiver", TestSimpleReceiver.class.getName());
              cluster.undeployNetwork(network, new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> result) {
                  if (result.failed()) {
                    assertTrue(result.cause().getMessage(), result.succeeded());
                  } else {
                    assertTrue(result.succeeded());
                    testComplete();
                  }
                }
              });
            }
          }
        });
      }
    });
  }

  @Test
  public void testReconfigureCreateConnection() {
    final String name = UUID.randomUUID().toString();
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();

        // Deploy a network with two components but no connections. The components
        // won't be able to communicate with each other, though the component code
        // doesn't need to know that
        NetworkConfig network = vertigo.createNetwork(name);
        network.addVerticle("sender", TestReconfigureSender.class.getName());
        network.addVerticle("receiver", TestReconfigureReceiver.class.getName());
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
              // Now that the two components are running, to add a connection between
              // the two components we have to create a new network configuration with
              // the same name as the original network and add the connection to it.
              // When the new configuration is deployed, Vertigo will recognize that
              // it has the same name as the running connection and merge the two
              // networks.
              NetworkConfig network = vertigo.createNetwork(name);
              network.createConnection("sender", "out", "receiver", "in");
              cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
                @Override
                public void handle(AsyncResult<ActiveNetwork> result) {
                  if (result.failed()) {
                    assertTrue(result.cause().getMessage(), result.succeeded());
                  } else {
                    assertTrue(result.succeeded());
                  }
                }
              });
            }
          }
        });
      }
    });
  }

  @Test
  public void testReconfigureDestroyConnection() {
    final String name = UUID.randomUUID().toString();
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();
        NetworkConfig network = vertigo.createNetwork(name);
        network.addVerticle("sender", TestSimpleSender.class.getName());
        network.addVerticle("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
              NetworkConfig network = vertigo.createNetwork(name);
              network.createConnection("sender", "out", "receiver", "in");
              cluster.undeployNetwork(network, new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> result) {
                  if (result.failed()) {
                    assertTrue(result.cause().getMessage(), result.succeeded());
                  } else {
                    assertTrue(result.succeeded());
                    testComplete();
                  }
                }
              });
            }
          }
        });
      }
    });
  }

  public static class TestSimpleSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").send("Hello world!");
    }
  }

  public static class TestSimpleReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          assertEquals("Hello world!", message);
        }
      });
    }
  }

}
