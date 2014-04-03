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
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.testtools.TestAckingComponent;
import net.kuujo.vertigo.testtools.TestPeriodicFeeder;

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
    public void handleReceive(String port, String id) {
      if (hook.equals("receive")) {
        testComplete();
      }
    }
    @Override
    public void handleSend(String port, String id) {
      if (hook.equals("send")) {
        testComplete();
      }
    }
  }

  @Test
  public void testComponentStartHook() {
    NetworkConfig network = new DefaultNetworkConfig("test1");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addVerticle("worker", TestAckingComponent.class.getName(), 2).addHook(new TestComponentHook("start"));
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  @Test
  public void testComponentSendHook() {
    NetworkConfig network = new DefaultNetworkConfig("test2");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body"))).addHook(new TestComponentHook("send"));
    network.addVerticle("worker", TestAckingComponent.class.getName(), 2);
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  @Test
  public void testComponentReceiveHook() {
    NetworkConfig network = new DefaultNetworkConfig("test3");
    network.addVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addVerticle("worker", TestAckingComponent.class.getName(), 2).addHook(new TestComponentHook("receive"));
    network.createConnection("feeder", "worker");
    deploy(network);
  }

  private void deploy(NetworkConfig network) {
    Vertigo vertigo = new Vertigo(this);
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
