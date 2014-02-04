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

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.java.FeederVerticle;
import net.kuujo.vertigo.java.WorkerVerticle;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.testtools.TestAckingFeeder;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestFailingExecutor;
import net.kuujo.vertigo.testtools.TestFailingFeeder;
import net.kuujo.vertigo.testtools.TestFailingWorker;
import net.kuujo.vertigo.testtools.TestPeriodicFeeder;
import net.kuujo.vertigo.testtools.TestResultCheckingExecutor;
import net.kuujo.vertigo.testtools.TestTimingOutExecutor;
import net.kuujo.vertigo.testtools.TestTimingOutFeeder;
import net.kuujo.vertigo.testtools.TestTimingOutWorker;
import net.kuujo.vertigo.worker.Worker;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.testtools.TestVerticle;

/**
 * A network deploy test.
 *
 * @author Jordan Halterman
 */
public class NetworkTest extends TestVerticle {

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

  public static class StreamFeeder extends FeederVerticle {
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
  public void testAckingExecutor() {
    Network network = new Network("test");
    JsonObject data = new JsonObject().putString("body", "Hello world!");
    network.addExecutorVerticle("executor", TestResultCheckingExecutor.class.getName(), new JsonObject().putObject("input", data).putObject("output", data)).addInput("worker");
    network.addWorkerVerticle("worker", TestAckingWorker.class.getName()).addInput("executor");
    deployNetwork(network);
  }

  @Test
  public void testFailingExecutor() {
    Network network = new Network("test");
    JsonObject data = new JsonObject().putString("body", "Hello world!");
    network.addExecutorVerticle("executor", TestFailingExecutor.class.getName(), new JsonObject().putObject("input", data).putObject("output", data)).addInput("worker");
    network.addWorkerVerticle("worker", TestFailingWorker.class.getName()).addInput("executor");
    deployNetwork(network);
  }

  @Test
  public void testTimingOutExecutor() {
    Network network = new Network("test");
    network.setMessageTimeout(1000);
    JsonObject data = new JsonObject().putString("body", "Hello world!");
    network.addExecutorVerticle("executor", TestTimingOutExecutor.class.getName(), new JsonObject().putObject("input", data).putObject("output", data)).addInput("worker");
    network.addWorkerVerticle("worker", TestTimingOutWorker.class.getName()).addInput("executor");
    deployNetwork(network);
  }

  public static class NestedWorker extends WorkerVerticle {
    @Override
    protected void handleMessage(JsonMessage message, Worker worker) {
      testComplete();
    }
  }

  @Test
  public void testNestedNetwork() {
    Network network1 = new Network("network1");
    network1.addFeederVerticle("network1.feeder1", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network1.addWorkerVerticle("network1.worker1", TestAckingWorker.class.getName(), 2).addInput("network1.feeder1").randomGrouping();
    network1.addWorkerVerticle("network1.worker2", TestAckingWorker.class.getName(), 2).addInput("network1.worker1").roundGrouping();

    final Cluster cluster = new LocalCluster(vertx, container);
    cluster.deployNetwork(network1, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          assertTrue(result.cause().getMessage(), result.succeeded());
        }
        else {
          assertTrue(result.succeeded());

          Network network2 = new Network("network2");
          network2.addWorkerVerticle("network2.worker1", TestAckingWorker.class.getName(), 2).addInput("network1.worker2").randomGrouping();
          network2.addWorkerVerticle("network2.worker2", NestedWorker.class.getName(), 2).addInput("network2.worker1").roundGrouping();
          deployNetwork(network2);
        }
      }
    });
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
