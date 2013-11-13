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
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestFailingWorker;
import net.kuujo.vertigo.testtools.TestPeriodicFeeder;

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
   * A test hook.
   */
  public static class TestHook implements ComponentHook, Serializable {
    private String hook;
    public TestHook() {
    }
    public TestHook(String hook) {
      this.hook = hook;
    }
    @Override
    public void start(net.kuujo.vertigo.component.Component<?> subject) {
      if (hook.equals("start")) {
        testComplete();
      }
    }
    @Override
    public void stop(net.kuujo.vertigo.component.Component<?> subject) {
      if (hook.equals("stop")) {
        testComplete();
      }
    }
    @Override
    public void received(String id) {
      if (hook.equals("received")) {
        testComplete();
      }
    }
    @Override
    public void ack(String id) {
      if (hook.equals("ack")) {
        testComplete();
      }
    }
    @Override
    public void fail(String id) {
      if (hook.equals("fail")) {
        testComplete();
      }
    }
    @Override
    public void emit(String id) {
      if (hook.equals("emit")) {
        testComplete();
      }
    }
    @Override
    public void acked(String id) {
      if (hook.equals("acked")) {
        testComplete();
      }
    }
    @Override
    public void failed(String id) {
      if (hook.equals("failed")) {
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
  public void testStartHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestHook("start"));
    deploy(network);
  }

  @Test
  public void testEmitHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestHook("emit"));
    deploy(network);
  }

  @Test
  public void testReceivedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestHook("received"));
    deploy(network);
  }

  @Test
  public void testAckHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestHook("ack"));
    deploy(network);
  }

  @Test
  public void testFailHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestFailingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    worker1.addHook(new TestHook("fail"));
    deploy(network);
  }

  @Test
  public void testAckedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestHook("acked"));
    deploy(network);
  }

  @Test
  public void testFailedHook() {
    Network network = new Network("test");
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestFailingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    feeder.addHook(new TestHook("failed"));
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
