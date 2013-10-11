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

import net.kuujo.vertigo.Cluster;
import net.kuujo.vertigo.LocalCluster;
import net.kuujo.vertigo.Networks;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.definition.NetworkDefinition;
import net.kuujo.vertigo.testtools.AckCheckingFeeder;
import net.kuujo.vertigo.testtools.AckingWorker;
import net.kuujo.vertigo.testtools.FailCheckingFeeder;
import net.kuujo.vertigo.testtools.FailingWorker;
import net.kuujo.vertigo.testtools.TimeoutWorker;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.testtools.TestVerticle;

/**
 * A network deploy test.
 *
 * @author Jordan Halterman
 */
public class NetworkTests extends TestVerticle {

  @Test
  public void testBasicAckFeeder() {
    NetworkDefinition network = Networks.createNetwork("test");
    network.from(AckCheckingFeeder.createDefinition(new JsonObject().putString("body", "Hello world!")))
      .to(AckingWorker.createDefinition(2));
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
    NetworkDefinition network = Networks.createNetwork("test");
    network.from(FailCheckingFeeder.createDefinition(new JsonObject().putString("body", "Hello world!")))
      .to(FailingWorker.createDefinition(2));
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
    NetworkDefinition network = Networks.createNetwork("test");
    network.from(FailCheckingFeeder.createDefinition(new JsonObject().putString("body", "Hello world!")))
      .to(TimeoutWorker.createDefinition(2));
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

}
