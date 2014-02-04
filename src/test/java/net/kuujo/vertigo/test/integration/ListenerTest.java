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
import net.kuujo.vertigo.input.Listener;
import net.kuujo.vertigo.input.impl.DefaultListener;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestPeriodicFeeder;
import net.kuujo.vertigo.worker.Worker;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.testtools.TestVerticle;

/**
 * A listener test.
 *
 * @author Jordan Halterman
 */
public class ListenerTest extends TestVerticle {

  @Test
  public void testListener() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", TestPeriodicFeeder.class.getName(), new JsonObject().putArray("fields", new JsonArray().add("body")));
    network.addWorkerVerticle("worker1", TestAckingWorker.class.getName(), 2).addInput("feeder");
    network.addWorkerVerticle("worker2", TestAckingWorker.class.getName(), 2).addInput("feeder");
    Component<Worker> worker3 = network.addWorker("worker3", TestAckingWorker.class.getName(), 2);
    worker3.addInput("worker1");
    worker3.addInput("worker2");
    network.addWorkerVerticle("worker4", TestAckingWorker.class.getName(), 2).addInput("worker3").randomGrouping();

    Cluster cluster = new LocalCluster(vertx, container);
    cluster.deployNetwork(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          assertTrue(result.cause().getMessage(), result.succeeded());
        }
        else {
          assertTrue(result.succeeded());
          Listener listener = new DefaultListener("worker3", vertx);
          listener.messageHandler(new Handler<JsonMessage>() {
            @Override
            public void handle(JsonMessage message) {
              assertEquals("feeder", message.source());
              testComplete();
            }
          }).start();
        }
      }
    });
  }

}
