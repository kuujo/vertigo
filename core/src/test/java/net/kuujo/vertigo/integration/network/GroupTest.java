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
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.io.batch.InputBatch;
import net.kuujo.vertigo.io.batch.OutputBatch;
import net.kuujo.vertigo.io.group.InputGroup;
import net.kuujo.vertigo.io.group.OutputGroup;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

/**
 * A network messaging test.
 *
 * @author Jordan Halterman
 */
public class GroupTest extends TestVerticle {

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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
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

  public static class TestGroupStartArgsSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("test", new JsonObject().putString("foo", "bar"), new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("Hello world!").end();
        }
      });
    }
  }

  public static class TestGroupStartArgsReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("test", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          group.startHandler(new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject args) {
              assertEquals("bar", args.getString("foo"));
              testComplete();
            }
          });
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
            }
          });
        }
      });
    }
  }

  @Test
  public void testGroupStartArgs() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
        network.addVerticle("sender", TestGroupStartArgsSender.class.getName());
        network.addVerticle("receiver", TestGroupStartArgsReceiver.class.getName());
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

  public static class TestGroupEndArgsSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").group("test", new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          group.send("Hello world!").end(new JsonObject().putString("foo", "bar"));
        }
      });
    }
  }

  public static class TestGroupEndArgsReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("test", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              assertEquals("Hello world!", message);
            }
          });
          group.endHandler(new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject args) {
              assertEquals("bar", args.getString("foo"));
              testComplete();
            }
          });
        }
      });
    }
  }

  @Test
  public void testGroupEndArgs() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork();
        network.addVerticle("sender", TestGroupEndArgsSender.class.getName());
        network.addVerticle("receiver", TestGroupEndArgsReceiver.class.getName());
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

}
