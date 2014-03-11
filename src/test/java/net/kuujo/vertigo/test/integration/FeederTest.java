/*
 * Copyright 2013 the original author or authors.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.java.BasicFeeder;
import net.kuujo.vertigo.java.BasicWorker;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.testtools.TestAckingFeeder;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestFailingFeeder;
import net.kuujo.vertigo.testtools.TestFailingWorker;
import net.kuujo.vertigo.testtools.TestTimingOutFeeder;
import net.kuujo.vertigo.testtools.TestTimingOutWorker;
import net.kuujo.vertigo.worker.Worker;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.testtools.TestVerticle;

/**
 * A network deploy test.
 *
 * @author Jordan Halterman
 */
public class FeederTest extends TestVerticle {

  @Test
  public void testAckingFeeder() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestAckingFeeder.class.getName(), new JsonObject().putString("body", "Hello world"));
    network.addWorkerVerticle("worker", TestAckingWorker.class.getName()).addInput("feeder");
    deployNetwork(network);
  }

  @Test
  public void testFailingFeeder() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestFailingFeeder.class.getName(), new JsonObject().putString("body", "Hello world"));
    network.addWorkerVerticle("worker", TestFailingWorker.class.getName()).addInput("feeder");
    deployNetwork(network);
  }

  @Test
  public void testTimingOutFeeder() {
    Network network = new Network("test");
    network.setMessageTimeout(1000);
    network.addFeederVerticle("feeder", TestTimingOutFeeder.class.getName(), new JsonObject().putString("body", "Hello world"));
    network.addWorkerVerticle("worker", TestTimingOutWorker.class.getName()).addInput("feeder");
    deployNetwork(network);
  }

  public static class StreamFeeder extends BasicFeeder {
    @Override
    public void start(Feeder feeder) {
      feeder.emit("stream", new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<MessageId>>() {
        @Override
        public void handle(AsyncResult<MessageId> result) {
          assertTrue(result.succeeded());
          testComplete();
        }
      });
    }
  }

  @Test
  public void testStreamFeeder() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", StreamFeeder.class.getName());
    network.addWorkerVerticle("worker", TestAckingWorker.class.getName()).addInput("feeder", "stream");
    deployNetwork(network);
  }

  @Test
  public void testOneToManyFeeder() {
    final Set<String> inputs = new HashSet<>(Arrays.asList(new String[]{"test.worker-1", "test.worker-2", "test.worker-3", "test.worker-4"}));
    final Set<String> alive = new HashSet<>();
    final Network network = new Network("test.network");
    network.addFeeder("test.feeder", TestOneToManyFeeder.class.getName());
    network.addWorker("test.worker", TestOneToManyWorker.class.getName(), 4).addInput("test.feeder");
    vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        alive.add(message.body());
        if (alive.size() == inputs.size()) {
          testComplete();
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployNetwork(network);
      }
    });
  }

  public static class TestOneToManyFeeder extends BasicFeeder {
    @Override
    public void start(final Feeder feeder) {
      feeder.emit(new JsonObject().putString("body", "Hello world!"));
      feeder.emit(new JsonObject().putString("body", "Hello world!"));
      feeder.emit(new JsonObject().putString("body", "Hello world!"));
      feeder.emit(new JsonObject().putString("body", "Hello world!"));
    }
  }

  public static class TestOneToManyWorker extends BasicWorker {
    @Override
    protected void handleMessage(JsonMessage message, Worker worker) {
      vertx.eventBus().send("test", context.address());
    }
  }

  @Test
  public void testManyToManyFeeder() {
    final Set<String> inputs = new HashSet<>(Arrays.asList(new String[]{"test.worker-1", "test.worker-2", "test.worker-3", "test.worker-4"}));
    final Set<String> alive = new HashSet<>();
    final Network network = new Network("test.network");
    network.addFeeder("test.feeder", TestManyToManyFeeder.class.getName(), 4);
    network.addWorker("test.worker", TestManyToManyWorker.class.getName(), 4).addInput("test.feeder");
    vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        alive.add(message.body());
        if (alive.size() == inputs.size()) {
          testComplete();
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployNetwork(network);
      }
    });
  }

  public static class TestManyToManyFeeder extends BasicFeeder {
    @Override
    public void start(final Feeder feeder) {
      feeder.emit(new JsonObject().putString("address", context.address()));
      feeder.emit(new JsonObject().putString("address", context.address()));
      feeder.emit(new JsonObject().putString("address", context.address()));
      feeder.emit(new JsonObject().putString("address", context.address()));
    }
  }

  public static class TestManyToManyWorker extends BasicWorker {
    private Set<String> received = new HashSet<>();
    @Override
    protected void handleMessage(JsonMessage message, Worker worker) {
      received.add(message.body().getString("address"));
      if (received.size() == 4) {
        vertx.eventBus().send("test", context.address());
      }
      worker.ack(message);
    }
  }

  @Test
  public void testManyToOneFeeder() {
    final Network network = new Network("test.network");
    network.addFeeder("test.feeder", TestManyToOneFeeder.class.getName(), 4);
    network.addWorker("test.worker", TestManyToOneWorker.class.getName(), 4).addInput("test.feeder");
    vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        testComplete();
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployNetwork(network);
      }
    });
  }

  public static class TestManyToOneFeeder extends BasicFeeder {
    @Override
    public void start(final Feeder feeder) {
      feeder.emit(new JsonObject().putString("address", context.address()));
      feeder.emit(new JsonObject().putString("address", context.address()));
      feeder.emit(new JsonObject().putString("address", context.address()));
      feeder.emit(new JsonObject().putString("address", context.address()));
    }
  }

  public static class TestManyToOneWorker extends BasicWorker {
    private Set<String> received = new HashSet<>();
    @Override
    protected void handleMessage(JsonMessage message, Worker worker) {
      received.add(message.body().getString("address"));
      if (received.size() == 4) {
        vertx.eventBus().send("test", context.address());
      }
      worker.ack(message);
    }
  }

  private void deployNetwork(Network network) {
    Cluster cluster = new LocalCluster(vertx, container);
    cluster.deployNetwork(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          assertTrue(result.cause().getMessage(), result.succeeded());
        }
        else {
          assertTrue(result.succeeded());
        }
      }
    });
  }

}