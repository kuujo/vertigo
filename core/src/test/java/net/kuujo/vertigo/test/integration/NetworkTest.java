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
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.hook.EventBusHook;
import net.kuujo.vertigo.hook.EventBusHookListener;
import net.kuujo.vertigo.hook.IOHook;
import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.io.batch.InputBatch;
import net.kuujo.vertigo.io.batch.OutputBatch;
import net.kuujo.vertigo.io.group.InputGroup;
import net.kuujo.vertigo.io.group.OutputGroup;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.test.VertigoTestVerticle;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

/**
 * A network messaging test.
 *
 * @author Jordan Halterman
 */
public class NetworkTest extends VertigoTestVerticle {

  public static class TestOneToNoneSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").send("Hello world!");
      vertx.setTimer(100, new Handler<Long>() {
        @Override
        public void handle(Long event) {
          testComplete();
        }
      });
    }
  }

  @Test
  public void testOneToNone() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestSender.class.getName());
        network.addVerticle("receiver", TestReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").send("Hello world!");
    }
  }

  public static class TestReceiver extends ComponentVerticle {
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
  public void testOneToOne() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestSender.class.getName());
        network.addVerticle("receiver", TestReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestOneToManySender extends ComponentVerticle {
    private final int count = 4;
    private final Set<String> received = new HashSet<>();

    @Override
    public void start() {
      vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
        @Override
        public void handle(Message<String> message) {
          received.add(message.body());
          if (received.size() == count) {
            testComplete();
          }
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          assertTrue(result.succeeded());
          for (int i = 0; i < count; i++) {
            output.port("out").send("Hello world!");
          }
        }
      });
    }
  }

  public static class TestOneToManyReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          assertEquals("Hello world!", message);
          vertx.eventBus().send("test", context.address());
        }
      });
    }
  }

  @Test
  public void testOneToMany() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestOneToManySender.class.getName());
        network.addVerticle("receiver", TestOneToManyReceiver.class.getName(), 4);
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestManyToOneSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").send(context.address());
    }
  }

  public static class TestManyToOneReceiver extends ComponentVerticle {
    private final int count = 4;
    private final Set<String> received = new HashSet<>();
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          received.add(message);
          if (received.size() == count) {
            testComplete();
          }
        }
      });
    }
  }

  @Test
  public void testManyToOne() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestManyToOneSender.class.getName(), 4);
        network.addVerticle("receiver", TestManyToOneReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestManyToManySender extends ComponentVerticle {
    private final int count = 4;
    private final Set<String> received = new HashSet<>();

    @Override
    public void start(final Future<Void> future) {
      vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
        @Override
        public void handle(Message<String> message) {
          received.add(message.body());
          if (received.size() == count) {
            testComplete();
          }
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            future.setFailure(result.cause());
          } else {
            TestManyToManySender.super.start(future);
          }
        }
      });
    }

    @Override
    public void start() {
      for (int i = 0; i < count; i++) {
        output.port("out").send(context.address());
        output.port("out").send(context.address());
        output.port("out").send(context.address());
        output.port("out").send(context.address());
      }
    }
  }

  public static class TestManyToManyReceiver extends ComponentVerticle {
    private final int count = 4;
    private final Set<String> received = new HashSet<>();
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          received.add(message);
          if (received.size() == count) {
            vertx.eventBus().publish("test", context.address());
          }
        }
      });
    }
  }

  @Test
  public void testManyToMany() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestManyToManySender.class.getName(), 4);
        network.addVerticle("receiver", TestManyToManyReceiver.class.getName(), 4);
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestCircularSender extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          assertEquals("Hello world!", message);
          testComplete();
        }
      });
      output.port("out").send("Hello world!");
    }
  }

  public static class TestCircularReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          assertEquals("Hello world!", message);
          output.port("out").send(message);
        }
      });
    }
  }

  @Test
  public void testCircular() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestCircularSender.class.getName());
        network.addVerticle("receiver", TestCircularReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        network.createConnection("receiver", "out", "sender", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestManySender extends ComponentVerticle {
    private int count;
    private final int total = 1000000;
    @Override
    public void start() {
      doSend();
    }
    private void doSend() {
      while (!output.port("out").sendQueueFull() && count < total) {
        output.port("out").send("Hello world!");
        count++;
      }
      output.port("out").drainHandler(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          doSend();
        }
      });
    }
  }

  public static class TestManyReceiver extends ComponentVerticle {
    private int received;
    private final int total = 1000000;
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          assertEquals("Hello world!", message);
          received++;
          if (received == total) {
            testComplete();
          }
        }
      });
    }
  }

  @Test
  public void testMany() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestManySender.class.getName());
        network.addVerticle("receiver", TestManyReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestPauseResumeSender extends ComponentVerticle {
    @Override
    public void start() {
      vertx.eventBus().registerHandler("test", new Handler<Message<Void>>() {
        @Override
        public void handle(Message<Void> message) {
          output.port("out").send(3);
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          output.port("out").send(1);
          output.port("out").send(2);
        }
      });
    }
  }

  public static class TestPauseResumeReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<Integer>() {
        @Override
        public void handle(Integer message) {
          if (message == 1) {
            input.port("in").pause();
            vertx.setTimer(100, new Handler<Long>() {
              @Override
              public void handle(Long timerID) {
                input.port("in").resume();
                vertx.eventBus().send("test", "foo");
              }
            });
          } else if (message == 2) {
            testComplete();
          }
        }
      });
    }
  }

  @Test
  public void testPauseResume() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestPauseResumeSender.class.getName());
        network.addVerticle("receiver", TestPauseResumeReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestOneToNoneBatchSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").batch(new Handler<OutputBatch>() {
        @Override
        public void handle(OutputBatch batch) {
          batch.send("foo").send("bar").send("baz").end();
          testComplete();
        }
      });
    }
  }

  @Test
  public void testOneToNoneBatch() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestOneToNoneBatchSender.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestBasicBatchSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").batch(new Handler<OutputBatch>() {
        @Override
        public void handle(OutputBatch batch) {
          batch.send("Hello world!");
          batch.send("Hello world!");
          batch.send("Hello world!");
          batch.end();
        }
      });
    }
  }

  public static class TestBasicBatchReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").batchHandler(new Handler<InputBatch>() {
        @Override
        public void handle(InputBatch batch) {
          final List<String> messages = new ArrayList<>();
          batch.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          batch.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testBasicBatch() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestBasicBatchSender.class.getName());
        network.addVerticle("receiver", TestBasicBatchReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestOneToManyBatchSender extends ComponentVerticle {
    private final int count = 4;
    private final Set<String> received = new HashSet<>();

    @Override
    public void start() {
      vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
        @Override
        public void handle(Message<String> message) {
          received.add(message.body());
          if (received.size() == count) {
            testComplete();
          }
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          assertTrue(result.succeeded());
          output.port("out").batch(new Handler<OutputBatch>() {
            @Override
            public void handle(OutputBatch batch) {
              batch.send("Hello world!");
              batch.send("Hello world!");
              batch.send("Hello world!");
              batch.send("Hello world!");
              batch.end();
            }
          });
        }
      });
    }
  }

  public static class TestOneToManyBatchReceiver extends ComponentVerticle {
    private boolean sent;
    @Override
    public void start() {
      input.port("in").batchHandler(new Handler<InputBatch>() {
        @Override
        public void handle(InputBatch batch) {
          batch.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              if (!sent) {
                assertEquals("Hello world!", message);
                vertx.eventBus().send("test", context.address());
                sent = true;
              }
            }
          });
        }
      });
    }
  }

  @Test
  public void testOneToManyBatch() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestOneToManyBatchSender.class.getName());
        network.addVerticle("receiver", TestOneToManyBatchReceiver.class.getName(), 4);
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestBatchesSender extends ComponentVerticle {
    @Override
    public void start() {
      vertx.setPeriodic(100, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          output.port("out").batch(new Handler<OutputBatch>() {
            @Override
            public void handle(OutputBatch batch) {
              batch.send("foo").send("bar").send("baz").end();
            }
          });
        }
      });
    }
  }

  public static class TestBatchesReceiver extends ComponentVerticle {
    private int received;

    @Override
    public void start() {
      input.port("in").batchHandler(new Handler<InputBatch>() {
        @Override
        public void handle(final InputBatch batch) {
          final Set<String> messages = new HashSet<>();
          batch.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              messages.add(message);
            }
          });
          batch.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
              assertEquals(3, messages.size());
              received++;
              if (received == 5) {
                testComplete();
              }
            }
          });
        }
      });
    }
  }

  @Test
  public void testBatches() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestBatchesSender.class.getName());
        network.addVerticle("receiver", TestBatchesReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestBatchForwardSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").batch(new Handler<OutputBatch>() {
        @Override
        public void handle(OutputBatch batch) {
          batch.send("Hello world!");
          batch.send("Hello world!");
          batch.send("Hello world!");
          batch.end();
        }
      });
    }
  }

  public static class TestBatchForwarder extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").batchHandler(new Handler<InputBatch>() {
        @Override
        public void handle(final InputBatch inbatch) {
          output.port("out").batch(inbatch.id(), new Handler<OutputBatch>() {
            @Override
            public void handle(final OutputBatch outbatch) {
              inbatch.messageHandler(new Handler<String>() {
                @Override
                public void handle(String message) {
                  outbatch.send(message);
                }
              });
              inbatch.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                  outbatch.end();
                }
              });
            }
          });
        }
      });
    }
  }

  public static class TestBatchForwardReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").batchHandler(new Handler<InputBatch>() {
        @Override
        public void handle(InputBatch batch) {
          final List<String> messages = new ArrayList<>();
          batch.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          batch.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testBatchForward() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestBatchForwardSender.class.getName());
        network.addVerticle("forwarder", TestBatchForwarder.class.getName());
        network.addVerticle("receiver", TestBatchForwardReceiver.class.getName());
        network.createConnection("sender", "out", "forwarder", "in");
        network.createConnection("forwarder", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestOneToManyGroupWithinBatchSender extends ComponentVerticle {
    private final int count = 4;
    private final Set<String> received = new HashSet<>();

    @Override
    public void start() {
      vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
        @Override
        public void handle(Message<String> message) {
          received.add(message.body());
          if (received.size() == count) {
            testComplete();
          }
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          assertTrue(result.succeeded());
          output.port("out").batch(new Handler<OutputBatch>() {
            @Override
            public void handle(OutputBatch batch) {
              batch.group("foo", new Handler<OutputGroup>() {
                @Override
                public void handle(OutputGroup group) {
                  group.send("Hello world!").end();
                }
              });
              batch.group("foo", new Handler<OutputGroup>() {
                @Override
                public void handle(OutputGroup group) {
                  group.send("Hello world!").end();
                }
              });
              batch.group("foo", new Handler<OutputGroup>() {
                @Override
                public void handle(OutputGroup group) {
                  group.send("Hello world!").end();
                }
              });
              batch.group("foo", new Handler<OutputGroup>() {
                @Override
                public void handle(OutputGroup group) {
                  group.send("Hello world!").end();
                }
              });
              batch.end();
            }
          });
        }
      });
    }
  }

  public static class TestOneToManyGroupWithinBatchReceiver extends ComponentVerticle {
    private boolean sent;
    @Override
    public void start() {
      input.port("in").batchHandler(new Handler<InputBatch>() {
        @Override
        public void handle(InputBatch batch) {
          batch.groupHandler("foo", new Handler<InputGroup>() {
            @Override
            public void handle(InputGroup group) {
              group.messageHandler(new Handler<String>() {
                @Override
                public void handle(String message) {
                  if (!sent) {
                    assertEquals("Hello world!", message);
                    vertx.eventBus().send("test", context.address());
                    sent = true;
                  }
                }
              });
            }
          });
        }
      });
    }
  }

  @Test
  public void testOneToManyGroupWithinBatch() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestOneToManyGroupWithinBatchSender.class.getName());
        network.addVerticle("receiver", TestOneToManyGroupWithinBatchReceiver.class.getName(), 4);
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestOneToNoneGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("test", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("foo").send("bar").send("baz").end();
          testComplete();
        }
      });
    }
  }

  @Test
  public void testOneToNoneGroup() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestOneToNoneGroupSender.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestBasicGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("Hello world!");
          group.send("Hello world!");
          group.send("Hello world!");
          group.end();
        }
      });
    }
  }

  public static class TestBasicGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testBasicGroup() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestBasicGroupSender.class.getName());
        network.addVerticle("receiver", TestBasicGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestUnnamedGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group(new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("Hello world!");
          group.send("Hello world!");
          group.send("Hello world!");
          group.end();
        }
      });
    }
  }

  public static class TestUnnamedGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler(new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testUnnamedGroup() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestUnnamedGroupSender.class.getName());
        network.addVerticle("receiver", TestUnnamedGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestNamedToUnnamedGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("Hello world!");
          group.send("Hello world!");
          group.send("Hello world!");
          group.end();
        }
      });
    }
  }

  public static class TestNamedToUnnamedGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler(new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testNamedToUnnamedGroup() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestNamedToUnnamedGroupSender.class.getName());
        network.addVerticle("receiver", TestNamedToUnnamedGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestNamedToNamedGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("Hello world!");
          group.send("Hello world!");
          group.send("Hello world!");
          group.end();
        }
      });
    }
  }

  public static class TestNamedToNamedGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler(new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          fail(); // The "foo" group handler should be called, not the unnamed handler.
        }
      });
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testNamedToNamedGroup() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestNamedToNamedGroupSender.class.getName());
        network.addVerticle("receiver", TestNamedToNamedGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestAsyncGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(final OutputGroup group) {
          vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              group.send("Hello world!");
              vertx.runOnContext(new Handler<Void>() {
                @Override
                public void handle(Void _) {
                  group.send("Hello world!");
                  vertx.runOnContext(new Handler<Void>() {
                    @Override
                    public void handle(Void _) {
                      group.send("Hello world!");
                      vertx.runOnContext(new Handler<Void>() {
                        @Override
                        public void handle(Void _) {
                          group.end();
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
  }

  public static class TestAsyncGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testAsyncGroup() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestAsyncGroupSender.class.getName());
        network.addVerticle("receiver", TestAsyncGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestNestedGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("Hello world!");
          group.send("Hello world!");
          group.send("Hello world!");
          group.group("bar", new Handler<OutputGroup>() {
            @Override
            public void handle(OutputGroup group) {
              group.send("bar");
              group.send("bar");
              group.send("bar");
              group.end();
            }
          });
          group.group("bar", new Handler<OutputGroup>() {
            @Override
            public void handle(OutputGroup group) {
              group.send("bar");
              group.send("bar");
              group.send("bar");
              group.end();
            }
          });
          group.end();
        }
      });
    }
  }

  public static class TestNestedGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          group.groupHandler("bar", new Handler<InputGroup>() {
            @Override
            public void handle(InputGroup group) {
              group.messageHandler(new Handler<String>() {
                @Override
                public void handle(String message) {
                  assertEquals("bar", message);
                  messages.add(message);
                }
              });
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(9, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testNestedGroups() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestNestedGroupSender.class.getName());
        network.addVerticle("receiver", TestNestedGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestNestedAsyncGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(final OutputGroup group) {
          vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              group.send("Hello world!");
              group.send("Hello world!");
              group.send("Hello world!");
              group.group("bar", new Handler<OutputGroup>() {
                @Override
                public void handle(final OutputGroup group) {
                  vertx.runOnContext(new Handler<Void>() {
                    @Override
                    public void handle(Void _) {
                      group.send("bar");
                      group.send("bar");
                      group.send("bar");
                      vertx.runOnContext(new Handler<Void>() {
                        @Override
                        public void handle(Void _) {
                          group.end();
                        }
                      });
                    }
                  });
                }
              });
              group.group("bar", new Handler<OutputGroup>() {
                @Override
                public void handle(final OutputGroup group) {
                  vertx.runOnContext(new Handler<Void>() {
                    @Override
                    public void handle(Void _) {
                      group.send("bar");
                      group.send("bar");
                      group.send("bar");
                      vertx.runOnContext(new Handler<Void>() {
                        @Override
                        public void handle(Void _) {
                          group.end();
                        }
                      });
                    }
                  });
                }
              });
              vertx.runOnContext(new Handler<Void>() {
                @Override
                public void handle(Void _) {
                  group.end();
                }
              });
            }
          });
        }
      });
    }
  }

  public static class TestNestedAsyncGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
              messages.add(message);
            }
          });
          group.groupHandler("bar", new Handler<InputGroup>() {
            @Override
            public void handle(InputGroup group) {
              group.messageHandler(new Handler<String>() {
                @Override
                public void handle(String message) {
                  assertEquals("bar", message);
                  messages.add(message);
                }
              });
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(9, messages.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testNestedAsyncGroups() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestNestedAsyncGroupSender.class.getName());
        network.addVerticle("receiver", TestNestedAsyncGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestOrderedGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send(1).send(1).send(1).send(1).send(1).end();
        }
      });
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send(2).send(2).send(2).send(2).send(2).end();
        }
      });
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send(3).send(3).send(3).send(3).send(3).end();
        }
      });
    }
  }

  public static class TestOrderedGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      final Set<Integer> numbers = new HashSet<>();
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          group.messageHandler(new Handler<Integer>() {
            @Override
            public void handle(Integer number) {
              for (Integer num : numbers) {
                if (num > number) {
                  fail();
                }
              }
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              numbers.add(numbers.size()+1);
              if (numbers.size() == 3) {
                testComplete();
              }
            }
          });
        }
      });
    }
  }

  @Test
  public void testGroupOrder() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestOrderedGroupSender.class.getName());
        network.addVerticle("receiver", TestOrderedGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestOrderedNestedGroupSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.group("bar", new Handler<OutputGroup>() {
            @Override
            public void handle(OutputGroup group) {
              group.send(1).send(1).send(1).send(1).send(1).end();
            }
          });
          group.group("bar", new Handler<OutputGroup>() {
            @Override
            public void handle(OutputGroup group) {
              group.send(2).send(2).send(2).send(2).send(2).end();
            }
          });
          group.group("bar", new Handler<OutputGroup>() {
            @Override
            public void handle(OutputGroup group) {
              group.send(3).send(3).send(3).send(3).send(3).end();
            }
          });
          group.end();
        }
      });
    }
  }

  public static class TestOrderedNestedGroupReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final Set<Integer> numbers = new HashSet<>();
          group.groupHandler("bar", new Handler<InputGroup>() {
            @Override
            public void handle(InputGroup group) {
              group.messageHandler(new Handler<Integer>() {
                @Override
                public void handle(Integer number) {
                  for (Integer num : numbers) {
                    if (num > number) {
                      fail();
                    }
                  }
                }
              });
              group.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void _) {
                  numbers.add(numbers.size()+1);
                }
              });
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, numbers.size());
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testNestedGroupsOrder() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestOrderedNestedGroupSender.class.getName());
        network.addVerticle("receiver", TestOrderedNestedGroupReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestGroupForwardSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("foo").send("bar").send("baz").end();
        }
      });
    }
  }

  public static class TestGroupForwardForwarder extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(final InputGroup ingroup) {
          output.port("out").group("foo", new Handler<OutputGroup>() {
            @Override
            public void handle(final OutputGroup outgroup) {
              ingroup.messageHandler(new Handler<String>() {
                @Override
                public void handle(String message) {
                  outgroup.send(message);
                }
              });
              ingroup.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void _) {
                  outgroup.end();
                }
              });
            }
          });
        }
      });
    }
  }

  public static class TestGroupForwardReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              messages.add(message);
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              assertTrue(messages.contains("foo"));
              assertTrue(messages.contains("bar"));
              assertTrue(messages.contains("baz"));
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testGroupForward() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestGroupForwardSender.class.getName());
        network.addVerticle("forwarder", TestGroupForwardForwarder.class.getName());
        network.addVerticle("receiver", TestGroupForwardReceiver.class.getName());
        network.createConnection("sender", "out", "forwarder", "in").roundSelect();
        network.createConnection("forwarder", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestNestedGroupForwardSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("foo", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.group("bar", new Handler<OutputGroup>() {
            @Override
            public void handle(OutputGroup group) {
              group.send("foo").send("bar").send("baz").end();
            }
          });
          group.end();
        }
      });
    }
  }

  public static class TestNestedGroupForwardForwarder extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(final InputGroup ingroup) {
          output.port("out").group("foo", new Handler<OutputGroup>() {
            @Override
            public void handle(final OutputGroup outgroup) {
              ingroup.groupHandler("bar", new Handler<InputGroup>() {
                @Override
                public void handle(final InputGroup ingroup) {
                  outgroup.group("bar", new Handler<OutputGroup>() {
                    @Override
                    public void handle(final OutputGroup outgroup) {
                      ingroup.messageHandler(new Handler<String>() {
                        @Override
                        public void handle(String message) {
                          outgroup.send(message);
                        }
                      });
                      ingroup.endHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void _) {
                          outgroup.end();
                        }
                      });
                    }
                  });
                }
              });
              ingroup.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void _) {
                  outgroup.end();
                }
              });
            }
          });
        }
      });
    }
  }

  public static class TestNestedGroupForwardReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          final List<String> messages = new ArrayList<>();
          group.groupHandler("bar", new Handler<InputGroup>() {
            @Override
            public void handle(InputGroup group) {
              group.messageHandler(new Handler<String>() {
                @Override
                public void handle(String message) {
                  messages.add(message);
                }
              });
            }
          });
          group.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void _) {
              assertEquals(3, messages.size());
              assertTrue(messages.contains("foo"));
              assertTrue(messages.contains("bar"));
              assertTrue(messages.contains("baz"));
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testNestedGroupForward() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestNestedGroupForwardSender.class.getName());
        network.addVerticle("forwarder", TestNestedGroupForwardForwarder.class.getName());
        network.addVerticle("receiver", TestNestedGroupForwardReceiver.class.getName());
        network.createConnection("sender", "out", "forwarder", "in").roundSelect();
        network.createConnection("forwarder", "out", "receiver", "in").roundSelect();
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        final ClusterManager cluster = result.result();
        NetworkConfig network = vertigo.createNetwork(name);
        network.addVerticle("sender", TestReconfigureSender.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
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
  public void testActiveAddComponent() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestReconfigureSender.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
              ActiveNetwork network = result.result();
              network.addVerticle("receiver", TestReconfigureReceiver.class.getName(), new Handler<AsyncResult<ActiveNetwork>>() {
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        final ClusterManager cluster = result.result();
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
  public void testActiveRemoveComponent() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestSimpleSender.class.getName());
        network.addVerticle("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
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
  public void testReconfigureCreateConnection() {
    final String name = UUID.randomUUID().toString();
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        final ClusterManager cluster = result.result();
        NetworkConfig network = vertigo.createNetwork(name);
        network.addVerticle("sender", TestReconfigureSender.class.getName());
        network.addVerticle("receiver", TestReconfigureReceiver.class.getName());
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
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
  public void testActiveCreateConnection() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestReconfigureSender.class.getName());
        network.addVerticle("receiver", TestReconfigureReceiver.class.getName());
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
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
  public void testReconfigureDestroyConnection() {
    final String name = UUID.randomUUID().toString();
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        final ClusterManager cluster = result.result();
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

  @Test
  public void testActiveDestroyConnection() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addVerticle("sender", TestSimpleSender.class.getName());
        network.addVerticle("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");
        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            if (result.failed()) {
              assertTrue(result.cause().getMessage(), result.succeeded());
            } else {
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").getTarget().addHook(new TestInputHook());

        ClusterManager cluster = result.result();
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").getSource().addHook(new TestOutputHook());

        ClusterManager cluster = result.result();
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").addHook(new TestIOSendHook());

        ClusterManager cluster = result.result();
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in").addHook(new TestIOReceiveHook());

        ClusterManager cluster = result.result();
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName()).addHook(new TestComponentStartHook());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");

        ClusterManager cluster = result.result();
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName()).addHook(new TestComponentSendHook());
        network.addComponent("receiver", TestSimpleReceiver.class.getName());
        network.createConnection("sender", "out", "receiver", "in");

        ClusterManager cluster = result.result();
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork(UUID.randomUUID().toString());
        network.addComponent("sender", TestSimpleSender.class.getName());
        network.addComponent("receiver", TestSimpleReceiver.class.getName()).addHook(new TestComponentReceiveHook());
        network.createConnection("sender", "out", "receiver", "in");

        ClusterManager cluster = result.result();
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        final ClusterManager cluster = result.result();
        final EventBusHookListener listener = new EventBusHookListener("test-hook", vertx.eventBus());
        listener.startHandler(new Handler<InstanceContext>() {
          @Override
          public void handle(InstanceContext context) {
            assertEquals("sender", context.component().name());
            testComplete();
          }
        });

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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        final ClusterManager cluster = result.result();
        final EventBusHookListener listener = new EventBusHookListener("test-hook", vertx.eventBus());
        listener.sendHandler(new Handler<String>() {
          @Override
          public void handle(String message) {
            assertEquals("Hello world!", message);
            testComplete();
          }
        });

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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        assertTrue(result.succeeded());
        final ClusterManager cluster = result.result();
        final EventBusHookListener listener = new EventBusHookListener("test-hook", vertx.eventBus());
        listener.receiveHandler(new Handler<String>() {
          @Override
          public void handle(String message) {
            assertEquals("Hello world!", message);
            testComplete();
          }
        });

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
