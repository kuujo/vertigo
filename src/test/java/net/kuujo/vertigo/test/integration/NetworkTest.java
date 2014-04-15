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

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.input.InputGroup;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.output.OutputGroup;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;

/**
 * A network messaging test.
 *
 * @author Jordan Halterman
 */
public class NetworkTest extends TestVerticle {

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
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("one-to-one");
    network.addVerticle("sender", TestSender.class.getName());
    network.addVerticle("receiver", TestReceiver.class.getName());
    network.createConnection("sender", "out", "receiver", "in");
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
            output.port("out").send("Hello world!");
            output.port("out").send("Hello world!");
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
          vertx.eventBus().send("test", component.context().address());
        }
      });
    }
  }

  @Test
  public void testOneToMany() {
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("one-to-many");
    network.addVerticle("sender", TestOneToManySender.class.getName());
    network.addVerticle("receiver", TestOneToManyReceiver.class.getName(), 4);
    network.createConnection("sender", "out", "receiver", "in").roundGrouping();
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

  public static class TestManyToOneSender extends ComponentVerticle {
    @Override
    public void start() {
      output.port("out").send(component.context().address());
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
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("many-to-one");
    network.addVerticle("sender", TestManyToOneSender.class.getName(), 4);
    network.addVerticle("receiver", TestManyToOneReceiver.class.getName());
    network.createConnection("sender", "out", "receiver", "in").roundGrouping();
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
        output.port("out").send(component.context().address());
        output.port("out").send(component.context().address());
        output.port("out").send(component.context().address());
        output.port("out").send(component.context().address());
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
            vertx.eventBus().publish("test", component.context().address());
          }
        }
      });
    }
  }

  @Test
  public void testManyToMany() {
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("many-to-many");
    network.addVerticle("sender", TestManyToManySender.class.getName(), 4);
    network.addVerticle("receiver", TestManyToManyReceiver.class.getName(), 4);
    network.createConnection("sender", "out", "receiver", "in").roundGrouping();
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("many");
    network.addVerticle("sender", TestManySender.class.getName());
    network.addVerticle("receiver", TestManyReceiver.class.getName());
    network.createConnection("sender", "out", "receiver", "in").roundGrouping();
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("basic-group");
    network.addVerticle("sender", TestBasicGroupSender.class.getName());
    network.addVerticle("receiver", TestBasicGroupReceiver.class.getName());
    network.createConnection("sender", "out", "receiver", "in").roundGrouping();
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("nested-group");
    network.addVerticle("sender", TestNestedGroupSender.class.getName());
    network.addVerticle("receiver", TestNestedGroupReceiver.class.getName());
    network.createConnection("sender", "out", "receiver", "in").roundGrouping();
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("group-order");
    network.addVerticle("sender", TestOrderedGroupSender.class.getName());
    network.addVerticle("receiver", TestOrderedGroupReceiver.class.getName());
    network.createConnection("sender", "out", "receiver", "in").roundGrouping();
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("nested-group-order");
    network.addVerticle("sender", TestOrderedNestedGroupSender.class.getName());
    network.addVerticle("receiver", TestOrderedNestedGroupReceiver.class.getName());
    network.createConnection("sender", "out", "receiver", "in").roundGrouping();
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
