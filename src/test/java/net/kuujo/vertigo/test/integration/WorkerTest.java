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
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.feeder.Feeder;
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
public class WorkerTest extends TestVerticle {

  public static class TestFeeder extends BasicFeeder {
    @Override
    public void start(final Feeder feeder) {
      feeder.emit(new JsonObject().putString("body", "Hello world!"));
    }
  }

  @Test
  public void testOneToManyWorker() {
    final Network network = new Network("test.network");
    network.addFeeder("test.feeder", TestFeeder.class.getName());
    network.addWorker("test.worker1", TestOneToManyWorker1.class.getName()).addInput("test.feeder").allGrouping();
    network.addWorker("test.worker2", TestOneToManyWorker2.class.getName(), 4).addInput("test.worker1");
    deployNetwork(network);
  }

  public static class TestOneToManyWorker1 extends BasicWorker {
    private final Set<String> inputs = new HashSet<>(Arrays.asList(new String[]{"test.worker2-1", "test.worker2-2", "test.worker2-3", "test.worker2-4"}));
    private final Set<String> received = new HashSet<>();
    @Override
    protected void handleMessage(final JsonMessage message, final Worker worker) {
      vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
        @Override
        public void handle(Message<String> message) {
          received.add(message.body());
          if (received.size() == inputs.size()) {
            testComplete();
          }
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          assertTrue(result.succeeded());
          worker.emit(new JsonObject().putString("body", "Hello world!"), message);
          worker.emit(new JsonObject().putString("body", "Hello world!"), message);
          worker.emit(new JsonObject().putString("body", "Hello world!"), message);
          worker.emit(new JsonObject().putString("body", "Hello world!"), message);
          worker.ack(message);
        }
      });
    }
  }

  public static class TestOneToManyWorker2 extends BasicWorker {
    @Override
    protected void handleMessage(JsonMessage message, Worker worker) {
      vertx.eventBus().send("test", context.address());
      worker.ack(message);
    }
  }

  @Test
  public void testManyToManyWorker() {
    final Network network = new Network("test.network");
    network.addFeeder("test.feeder", TestFeeder.class.getName());
    network.addWorker("test.worker1", TestManyToManyWorker1.class.getName(), 4).addInput("test.feeder").allGrouping();
    network.addWorker("test.worker2", TestManyToManyWorker2.class.getName(), 4).addInput("test.worker1");
    deployNetwork(network);
  }

  public static class TestManyToManyWorker1 extends BasicWorker {
    private final Set<String> inputs = new HashSet<>(Arrays.asList(new String[]{"test.worker2-1", "test.worker2-2", "test.worker2-3", "test.worker2-4"}));
    private final Set<String> received = new HashSet<>();
    @Override
    protected void handleMessage(final JsonMessage message, final Worker worker) {
      vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
        @Override
        public void handle(Message<String> message) {
          received.add(message.body());
          if (received.size() == inputs.size()) {
            testComplete();
          }
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          assertTrue(result.succeeded());
          worker.emit(new JsonObject().putString("address", context.address()), message);
          worker.emit(new JsonObject().putString("address", context.address()), message);
          worker.emit(new JsonObject().putString("address", context.address()), message);
          worker.emit(new JsonObject().putString("address", context.address()), message);
          worker.ack(message);
        }
      });
    }
  }

  public static class TestManyToManyWorker2 extends BasicWorker {
    private Set<String> received = new HashSet<>();
    @Override
    protected void handleMessage(JsonMessage message, Worker worker) {
      received.add(message.body().getString("address"));
      if (received.size() == 4) {
        vertx.eventBus().publish("test", context.address());
      }
      worker.ack(message);
    }
  }

  @Test
  public void testManyToOneWorker() {
    final Network network = new Network("test.network");
    network.addFeeder("test.feeder", TestFeeder.class.getName());
    network.addWorker("test.worker1", TestManyToOneWorker1.class.getName(), 4).addInput("test.feeder").allGrouping();
    network.addWorker("test.worker2", TestManyToOneWorker2.class.getName()).addInput("test.worker1");
    deployNetwork(network);
  }

  public static class TestManyToOneWorker1 extends BasicWorker {
    @Override
    protected void handleMessage(final JsonMessage message, final Worker worker) {
      vertx.eventBus().registerHandler("test", new Handler<Message<String>>() {
        @Override
        public void handle(Message<String> message) {
          testComplete();
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          assertTrue(result.succeeded());
          worker.emit(new JsonObject().putString("address", context.address()), message);
          worker.emit(new JsonObject().putString("address", context.address()), message);
          worker.emit(new JsonObject().putString("address", context.address()), message);
          worker.emit(new JsonObject().putString("address", context.address()), message);
          worker.ack(message);
        }
      });
    }
  }

  public static class TestManyToOneWorker2 extends BasicWorker {
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