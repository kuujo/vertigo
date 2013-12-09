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
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.testtools.TestAckingFeeder;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestFailingExecutor;
import net.kuujo.vertigo.testtools.TestFailingFeeder;
import net.kuujo.vertigo.testtools.TestFailingWorker;
import net.kuujo.vertigo.testtools.TestResultCheckingExecutor;
import net.kuujo.vertigo.testtools.TestTimingOutExecutor;
import net.kuujo.vertigo.testtools.TestTimingOutFeeder;
import net.kuujo.vertigo.testtools.TestTimingOutWorker;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;

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
    network.addFeeder("feeder", TestAckingFeeder.class.getName(), new JsonObject().putString("body", "Hello world"));
    network.addWorker("worker", TestAckingWorker.class.getName()).addInput("feeder");
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

  @Test
  public void testFailingFeeder() {
    Network network = new Network("test");
    network.addFeeder("feeder", TestFailingFeeder.class.getName(), new JsonObject().putString("body", "Hello world"));
    network.addWorker("worker", TestFailingWorker.class.getName()).addInput("feeder");
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

  @Test
  public void testTimingOutFeeder() {
    Network network = new Network("test");
    network.setAckTimeout(1000);
    network.addFeeder("feeder", TestTimingOutFeeder.class.getName(), new JsonObject().putString("body", "Hello world"));
    network.addWorker("worker", TestTimingOutWorker.class.getName()).addInput("feeder");
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

  @Test
  public void testAckingExecutor() {
    Network network = new Network("test");
    JsonObject data = new JsonObject().putString("body", "Hello world!");
    network.addExecutor("executor", TestResultCheckingExecutor.class.getName(), new JsonObject().putObject("input", data).putObject("output", data)).addInput("worker");
    network.addWorker("worker", TestAckingWorker.class.getName()).addInput("executor");
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

  @Test
  public void testFailingExecutor() {
    Network network = new Network("test");
    JsonObject data = new JsonObject().putString("body", "Hello world!");
    network.addExecutor("executor", TestFailingExecutor.class.getName(), new JsonObject().putObject("input", data).putObject("output", data)).addInput("worker");
    network.addWorker("worker", TestFailingWorker.class.getName()).addInput("executor");
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

  @Test
  public void testTimingOutExecutor() {
    Network network = new Network("test");
    network.setAckTimeout(1000);
    JsonObject data = new JsonObject().putString("body", "Hello world!");
    network.addExecutor("executor", TestTimingOutExecutor.class.getName(), new JsonObject().putObject("input", data).putObject("output", data)).addInput("worker");
    network.addWorker("worker", TestTimingOutWorker.class.getName()).addInput("executor");
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
