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
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestFailingWorker;
import net.kuujo.vertigo.testtools.TestPeriodicFeeder;
import net.kuujo.vertigo.testtools.TestTimingOutWorker;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
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
   * A test input hook.
   */
  public static class TestInputHook implements InputHook, Serializable {
    private String hook;
    public TestInputHook() {
    }
    public TestInputHook(String hook) {
      this.hook = hook;
    }
    @Override
    public void handleStart(InputCollector subject) {
      if (hook.equals("start")) {
        testComplete();
      }
    }
    @Override
    public void handleStop(InputCollector subject) {
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
    public JsonObject getState() {
      return new JsonObject().putString("hook", hook);
    }
    @Override
    public void setState(JsonObject state) throws SerializationException {
      this.hook = state.getString("hook");
    }
  }

  /**
   * A test output hook.
   */
  public static class TestOutputHook implements OutputHook, Serializable {
    private String hook;
    public TestOutputHook() {
    }
    public TestOutputHook(String hook) {
      this.hook = hook;
    }
    @Override
    public void handleStart(OutputCollector subject) {
      if (hook.equals("start")) {
        testComplete();
      }
    }
    @Override
    public void handleStop(OutputCollector subject) {
      if (hook.equals("stop")) {
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
    @Override
    public JsonObject getState() {
      return new JsonObject().putString("hook", hook);
    }
    @Override
    public void setState(JsonObject state) throws SerializationException {
      this.hook = state.getString("hook");
    }
  }

  /**
   * A test component hook.
   */
  public static class TestComponentHook implements ComponentHook, Serializable {
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
    @Override
    public JsonObject getState() {
      return new JsonObject().putString("hook", hook);
    }
    @Override
    public void setState(JsonObject state) throws SerializationException {
      this.hook = state.getString("hook");
    }
  }

  @Test
  public void testInputStartHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestInputHook("start"));
    deploy(network);
  }

  @Test
  public void testInputReceivedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestInputHook("received"));
    deploy(network);
  }

  @Test
  public void testInputAckHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestInputHook("ack"));
    deploy(network);
  }

  @Test
  public void testInputFailHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestFailingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestInputHook("fail"));
    deploy(network);
  }

  @Test
  public void testOutputStartHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestOutputHook("start"));
    deploy(network);
  }

  @Test
  public void testOutputEmitHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestOutputHook("emit"));
    deploy(network);
  }

  @Test
  public void testOutputAckedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestOutputHook("acked"));
    deploy(network);
  }

  @Test
  public void testOutputFailedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestFailingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestOutputHook("failed"));
    deploy(network);
  }

  @Test
  public void testOutputTimeoutHook() {
    Network network = new Network("test");
    network.setAckTimeout(1000);
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestTimingOutWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestOutputHook("timeout"));
    deploy(network);
  }

  @Test
  public void testComponentStartHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestComponentHook("start"));
    deploy(network);
  }

  @Test
  public void testComponentEmitHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestComponentHook("emit"));
    deploy(network);
  }

  @Test
  public void testComponentReceivedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestComponentHook("received"));
    deploy(network);
  }

  @Test
  public void testComponentAckHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestComponentHook("ack"));
    deploy(network);
  }

  @Test
  public void testComponentFailHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestFailingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestComponentHook("fail"));
    deploy(network);
  }

  @Test
  public void testComponentAckedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestComponentHook("acked"));
    deploy(network);
  }

  @Test
  public void testComponentFailedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestFailingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestComponentHook("failed"));
    deploy(network);
  }

  @Test
  public void testComponentTimeoutHook() {
    Network network = new Network("test");
    network.setAckTimeout(1000);
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestTimingOutWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestComponentHook("timeout"));
    deploy(network);
  }

  private void deploy(Network network) {
    Cluster cluster = new LocalCluster(vertx, container);
    cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
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
