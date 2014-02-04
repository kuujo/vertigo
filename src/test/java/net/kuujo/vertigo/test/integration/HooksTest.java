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
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestFailingWorker;
import net.kuujo.vertigo.testtools.TestPeriodicFeeder;
import net.kuujo.vertigo.testtools.TestTimingOutWorker;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.testtools.TestVerticle;

/**
 * A component hooks test.
 *
 * @author Jordan Halterman
 */
public class HooksTest extends TestVerticle {

  /**
   * A test component hook.
   */
  public static class TestComponentHook implements ComponentHook {
    private String hook;
    public TestComponentHook() {
    }
    public TestComponentHook(String hook) {
      this.hook = hook;
    }
    @Override
    public void handleStart(net.kuujo.vertigo.component.Component<?> subject) {
      if (hook.equals("start")) {
        testComplete();
      }
    }
    @Override
    public void handleStop(net.kuujo.vertigo.component.Component<?> subject) {
      if (hook.equals("stop")) {
        testComplete();
      }
    }
    @Override
    public void handleReceive(MessageId id) {
      if (hook.equals("received")) {
        testComplete();
      }
    }
    @Override
    public void handleAck(MessageId id) {
      if (hook.equals("ack")) {
        testComplete();
      }
    }
    @Override
    public void handleFail(MessageId id) {
      if (hook.equals("fail")) {
        testComplete();
      }
    }
    @Override
    public void handleEmit(MessageId id) {
      if (hook.equals("emit")) {
        testComplete();
      }
    }
    @Override
    public void handleAcked(MessageId id) {
      if (hook.equals("acked")) {
        testComplete();
      }
    }
    @Override
    public void handleFailed(MessageId id) {
      if (hook.equals("failed")) {
        testComplete();
      }
    }
    @Override
    public void handleTimeout(MessageId id) {
      if (hook.equals("timeout")) {
        testComplete();
      }
    }
  }

  @Test
  public void testComponentStartHook() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addWorkerVerticle("worker", TestAckingWorker.class.getName(), 2).addHook(new TestComponentHook("start")).addInput("feeder");
    deploy(network);
  }

  @Test
  public void testComponentEmitHook() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("emit"));
    network.addWorkerVerticle("worker", TestAckingWorker.class.getName(), 2).addInput("feeder");
    deploy(network);
  }

  @Test
  public void testComponentReceivedHook() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addWorkerVerticle("worker", TestAckingWorker.class.getName(), 2).addHook(new TestComponentHook("received")).addInput("feeder");
    deploy(network);
  }

  @Test
  public void testComponentAckHook() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addWorkerVerticle("worker", TestAckingWorker.class.getName(), 2).addHook(new TestComponentHook("ack")).addInput("feeder");
    deploy(network);
  }

  @Test
  public void testComponentFailHook() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addWorkerVerticle("worker", TestFailingWorker.class.getName(), 2).addHook(new TestComponentHook("fail")).addInput("feeder");
    deploy(network);
  }

  @Test
  public void testComponentAckedHook() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("acked"));
    network.addWorkerVerticle("worker", TestAckingWorker.class.getName(), 2).addInput("feeder");
    deploy(network);
  }

  @Test
  public void testComponentFailedHook() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("failed"));
    network.addWorkerVerticle("worker", TestFailingWorker.class.getName(), 2).addInput("feeder");
    deploy(network);
  }

  @Test
  public void testComponentTimeoutHook() {
    Network network = new Network("test");
    network.setMessageTimeout(1000);
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("timeout"));
    network.addWorkerVerticle("worker", TestTimingOutWorker.class.getName(), 2).addInput("feeder");
    deploy(network);
  }

  private void deploy(Network network) {
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
