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
 * Active network tests.
 *
 * @author Jordan Halterman
 */
public class ActiveNetworkTest extends TestVerticle {

  public static class TestActiveSender extends ComponentVerticle {
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

  public static class TestActiveReceiver extends ComponentVerticle {
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
  public void testActiveAddComponent() {
    // Deploy a randomly named cluster.
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());

        // Create a randomly named partial network. This network will have only
        // a single component and a connection that doesn't go anywhere.
        NetworkConfig network = vertigo.createNetwork();
        network.addVerticle("sender", TestActiveSender.class.getName());
        network.createConnection("sender", "out", "receiver", "in");

        // Deploy the partial network. Even though the network has a connection that
        // doesn't actually go anywhere, the "sender" component will still thing the
        // connection exists, and it will try to open the connection until a component
        // that can listen on the connection joins the network.
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
              // Add a "receiver" verticle to the network. The "receiver" verticle should
              // automatically be connected to the existing connection once it's deployed.
              ActiveNetwork network = result.result();
              network.addVerticle("receiver", TestActiveReceiver.class.getName(), new Handler<AsyncResult<ActiveNetwork>>() {
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
  public void testActiveRemoveComponent() {
    // Deploy a randomly named cluster.
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());

        // Create a randomly named network. This network will be a complete
        // network that we'll later alter once it's deployed.
        NetworkConfig network = vertigo.createNetwork();
        network.addVerticle("sender", TestSimpleSender.class.getName());
        network.addVerticle("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
              // Remote a verticle from the network through the ActiveNetwork.
              ActiveNetwork network = result.result();
              network.removeVerticle("receiver", new Handler<AsyncResult<ActiveNetwork>>() {
                @Override
                public void handle(AsyncResult<ActiveNetwork> result) {
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
  public void testActiveCreateConnection() {
    // Deploy a randomly named cluster.
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());

        // Create a randomly named partial network. This is a network that has
        // two components but no connections.
        NetworkConfig network = vertigo.createNetwork();
        network.addVerticle("sender", TestActiveSender.class.getName());
        network.addVerticle("receiver", TestActiveReceiver.class.getName());

        // When the network is deployed, the components will be deployed as normal,
        // but they won't be able to communicate with each other since there's no
        // connection on the network. Sending messages to the "sender"s "out" port
        // will simply result in the messages disappearing.
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
              // Now create a connection on the network. Both the "sender" and the
              // "receiver" component will be automatically updated with the new
              // connection, and messages send to the "sender"s "out" port will
              // be sent to the "receiver"s "in" port.
              ActiveNetwork network = result.result();
              network.createConnection("sender", "out", "receiver", "in", new Handler<AsyncResult<ActiveNetwork>>() {
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
  public void testActiveDestroyConnection() {
    // Deploy a randomly named cluster.
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());

        // Create a randomly named complete network.
        NetworkConfig network = vertigo.createNetwork();
        network.addVerticle("sender", TestSimpleSender.class.getName());
        network.addVerticle("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
              // Destroy the connection between the "sender" and "receiver". Once
              // the connection is destroyed the sender will no longer be able to
              // send messages to the receiver and messages sent on its "out" port
              // will simply be discarded.
              ActiveNetwork network = result.result();
              network.destroyConnection("sender", "out", "receiver", "in", new Handler<AsyncResult<ActiveNetwork>>() {
                @Override
                public void handle(AsyncResult<ActiveNetwork> result) {
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
