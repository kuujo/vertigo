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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.io.batch.InputBatch;
import net.kuujo.vertigo.io.batch.OutputBatch;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;

/**
 * Network batch messaging tests.
 *
 * @author Jordan Halterman
 */
public class BatchTest extends TestVerticle {

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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
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

}
