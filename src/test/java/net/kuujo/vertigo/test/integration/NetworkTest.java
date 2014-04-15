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
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.HashSet;
import java.util.Set;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

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

}
