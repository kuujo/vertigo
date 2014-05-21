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
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.hook.EventBusHook;
import net.kuujo.vertigo.hook.EventBusHookListener;
import net.kuujo.vertigo.hook.IOHook;
import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

/**
 * Network and IO hook tests.
 *
 * @author Jordan Halterman
 */
public class HookTest extends TestVerticle {

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

  public static class TestInputHook implements InputHook {
    @Override
    public void handleReceive(Object message) {
      assertEquals("Hello world!", message);
      testComplete();
    }
  }

  @Test
  public void testInputHook() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").getTarget().addHook(new TestInputHook());

        Cluster cluster = result.result();
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
    });
  }

  public static class TestOutputHook implements OutputHook {
    @Override
    public void handleSend(Object message) {
      assertEquals("Hello world!", message);
      testComplete();
    }
  }

  @Test
  public void testOutputHook() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").getSource().addHook(new TestOutputHook());

        Cluster cluster = result.result();
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
    });
  }

  public static class TestIOSendHook implements IOHook {
    @Override
    public void handleReceive(Object message) {
      
    }
    @Override
    public void handleSend(Object message) {
      assertEquals("Hello world!", message);
      testComplete();
    }
  }

  @Test
  public void testIOSendHook() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());

        // The IO hook is added directly to a connection between two components.
        network.createConnection("sender", "out", "receiver", "in").addHook(new TestIOSendHook());

        Cluster cluster = result.result();
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
    });
  }

  public static class TestIOReceiveHook implements IOHook {
    @Override
    public void handleReceive(Object message) {
      
    }
    @Override
    public void handleSend(Object message) {
      assertEquals("Hello world!", message);
      testComplete();
    }
  }

  @Test
  public void testIOReceiveHook() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());

        // The IO hook is added directly to a connection between two components.
        network.createConnection("sender", "out", "receiver", "in").addHook(new TestIOReceiveHook());

        Cluster cluster = result.result();
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
    });
  }

  public static class TestComponentStartHook implements ComponentHook {
    @Override
    public void handleSend(Object message) {
      assertEquals("Hello world!", message);
      testComplete();
    }
    @Override
    public void handleReceive(Object message) {
      
    }
    @Override
    public void handleStart(Component component) {
      
    }
    @Override
    public void handleStop(Component component) {
      
    }
  }

  @Test
  public void testComponentStartHook() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());

        // The start hook is added directly to a component configuration.
        network.addComponent("sender", TestSimpleSender.class.getName()).addHook(new TestComponentStartHook());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");

        Cluster cluster = result.result();
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
    });
  }

  public static class TestComponentSendHook implements ComponentHook {
    @Override
    public void handleSend(Object message) {
      assertEquals("Hello world!", message);
      testComplete();
    }
    @Override
    public void handleReceive(Object message) {
      
    }
    @Override
    public void handleStart(Component component) {
      
    }
    @Override
    public void handleStop(Component component) {
      
    }
  }

  @Test
  public void testComponentSendHook() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());

        // The component hook is added directly to a component configuration. The
        // send handler will be called each time a message is sent on *any* port.
        network.addComponent("sender", TestSimpleSender.class.getName()).addHook(new TestComponentSendHook());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");

        Cluster cluster = result.result();
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
    });
  }

  public static class TestComponentReceiveHook implements ComponentHook {
    @Override
    public void handleSend(Object message) {
      
    }
    @Override
    public void handleReceive(Object message) {
      assertEquals("Hello world!", message);
      testComplete();
    }
    @Override
    public void handleStart(Component component) {
      
    }
    @Override
    public void handleStop(Component component) {
      
    }
  }

  @Test
  public void testComponentReceiveHook() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());

        // The component hook is added directly to a component configuration. The
        // receive handler will be called each time a message is received on *any* port.
        network.addComponent("receiver", TestSimpleReceiver.class.getName()).addHook(new TestComponentReceiveHook());
        network.createConnection("sender", "out", "receiver", "in");

        Cluster cluster = result.result();
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
    });
  }

  @Test
  public void testEventBusHookStart() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();

        // Use the special EventBusHookListener to listen for messages
        // from the event bus hook. This will handle parsing event bus messages.
        final EventBusHookListener listener = new EventBusHookListener("test-hook", vertx.eventBus());
        listener.startHandler(new Handler<InstanceContext>() {
          @Override
          public void handle(InstanceContext context) {
            assertEquals("sender", context.component().name());
            testComplete();
          }
        });

        // The start() method simply registers an event bus handler for the hook.
        listener.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());

            NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
            network.addComponent("sender", TestSimpleSender.class.getName()).addHook(new EventBusHook("test-hook"));
            network.addComponent("receiver", TestSimpleReceiver.class.getName());
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
        });
      }
    });
  }

  @Test
  public void testEventBusHookSend() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();

        // Use the special EventBusHookListener to listen for messages
        // from the event bus hook. This will handle parsing event bus messages.
        final EventBusHookListener listener = new EventBusHookListener("test-hook", vertx.eventBus());
        listener.sendHandler(new Handler<String>() {
          @Override
          public void handle(String message) {
            assertEquals("Hello world!", message);
            testComplete();
          }
        });

        // The start() method simply registers an event bus handler for the hook.
        listener.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());

            NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
            network.addComponent("sender", TestSimpleSender.class.getName()).addHook(new EventBusHook("test-hook"));
            network.addComponent("receiver", TestSimpleReceiver.class.getName());
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
        });
      }
    });
  }

  @Test
  public void testEventBusHookReceive() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        final Cluster cluster = result.result();

        // Use the special EventBusHookListener to listen for messages
        // from the event bus hook. This will handle parsing event bus messages.
        final EventBusHookListener listener = new EventBusHookListener("test-hook", vertx.eventBus());
        listener.receiveHandler(new Handler<String>() {
          @Override
          public void handle(String message) {
            assertEquals("Hello world!", message);
            testComplete();
          }
        });

        // The start() method simply registers an event bus handler for the hook.
        listener.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());

            NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
            network.addComponent("sender", TestSimpleSender.class.getName());
            network.addComponent("receiver", TestSimpleReceiver.class.getName()).addHook(new EventBusHook("test-hook"));
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
        });
      }
    });
  }

}
