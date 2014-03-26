/*
 * Copyright 2013-2014 the original author or authors.
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

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.cluster.LocalClusterManager;
import net.kuujo.vertigo.cluster.VertigoClusterManager;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.testtools.TestAckingComponent;
import net.kuujo.vertigo.testtools.TestFailingComponent;
import net.kuujo.vertigo.testtools.TestPeriodicFeeder;
import net.kuujo.vertigo.testtools.TestTimingOutComponent;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
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
    public void handleStart(net.kuujo.vertigo.component.Component subject) {
      if (hook.equals("start")) {
        testComplete();
      }
    }
    @Override
    public void handleStop(net.kuujo.vertigo.component.Component subject) {
      if (hook.equals("stop")) {
        testComplete();
      }
    }
    @Override
    public void handleReceive(String id) {
      if (hook.equals("received")) {
        testComplete();
      }
    }
    @Override
    public void handleAck(String id) {
      if (hook.equals("ack")) {
        testComplete();
      }
    }
    @Override
    public void handleFail(String id) {
      if (hook.equals("fail")) {
        testComplete();
      }
    }
    @Override
    public void handleEmit(String id) {
      if (hook.equals("emit")) {
        testComplete();
      }
    }
    @Override
    public void handleAcked(String id) {
      if (hook.equals("acked")) {
        testComplete();
      }
    }
    @Override
    public void handleFailed(String id) {
      if (hook.equals("failed")) {
        testComplete();
      }
    }
    @Override
    public void handleTimeout(String id) {
      if (hook.equals("timeout")) {
        testComplete();
      }
    }
  }

  @Test
  public void testComponentStartHook() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addVerticle("worker", TestAckingComponent.class.getName(), 2).addHook(new TestComponentHook("start"));
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  @Test
  public void testComponentEmitHook() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("emit"));
    network.addVerticle("worker", TestAckingComponent.class.getName(), 2);
    deploy(network);
  }

  @Test
  public void testComponentReceivedHook() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addVerticle("worker", TestAckingComponent.class.getName(), 2).addHook(new TestComponentHook("received"));
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  @Test
  public void testComponentAckHook() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addVerticle("worker", TestAckingComponent.class.getName(), 2).addHook(new TestComponentHook("ack"));
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  @Test
  public void testComponentFailHook() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addVerticle("worker", TestFailingComponent.class.getName(), 2).addHook(new TestComponentHook("fail"));
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  @Test
  public void testComponentAckedHook() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("acked"));
    network.addVerticle("worker", TestAckingComponent.class.getName(), 2);
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  @Test
  public void testComponentFailedHook() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("failed"));
    network.addVerticle("worker", TestFailingComponent.class.getName(), 2);
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  @Test
  public void testComponentTimeoutHook() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.setMessageTimeout(1000);
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("timeout"));
    network.addVerticle("worker", TestTimingOutComponent.class.getName(), 2);
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  private void deploy(NetworkConfig network) {
    VertigoClusterManager cluster = new LocalClusterManager(vertx, container);
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
