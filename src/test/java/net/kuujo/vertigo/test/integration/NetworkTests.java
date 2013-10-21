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
import net.kuujo.vertigo.context_old.NetworkContext;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.testtools.TestAckingFeeder;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestFailingFeeder;
import net.kuujo.vertigo.testtools.TestFailingWorker;
import net.kuujo.vertigo.testtools.TestResultCheckingExecutor;
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
public class NetworkTests extends TestVerticle {

  @Test
  public void testBasicAckFeeder() {
    Network network = new Network("test");
    network.from(TestAckingFeeder.createDefinition(new JsonObject().putString("body", "Hello world!")))
      .to(TestAckingWorker.createDefinition(2));
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
  public void testBasicFailFeeder() {
    Network network = new Network("test");
    network.from(TestFailingFeeder.createDefinition(new JsonObject().putString("body", "Hello world!")))
      .to(TestFailingWorker.createDefinition(2));
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
    network.from(TestFailingFeeder.createDefinition(new JsonObject().putString("body", "Hello world!")))
      .to(TestTimingOutWorker.createDefinition(2));
    network.setAckExpire(1000);
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
  public void testBasicExecutor() {
    Network network = new Network("test");
    JsonObject data = new JsonObject().putString("body", "Hello world!");
    Component executor = TestResultCheckingExecutor.createDefinition(data, data);
    network.from(executor)
      .to(TestAckingWorker.createDefinition())
      .to(executor);
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
