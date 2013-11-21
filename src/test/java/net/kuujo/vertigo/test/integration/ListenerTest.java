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
import net.kuujo.vertigo.input.DefaultListener;
import net.kuujo.vertigo.input.Listener;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.testtools.TestAckingWorker;
import net.kuujo.vertigo.testtools.TestPeriodicFeeder;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

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
    final Component<?> feeder = TestPeriodicFeeder.createDefinition(new String[]{"body"});
    final Component<?> worker1 = TestAckingWorker.createDefinition(2);
    final Component<?> worker2 = TestAckingWorker.createDefinition(2);
    final Component<?> worker3 = TestAckingWorker.createDefinition(2);
    final Component<?> worker4 = TestAckingWorker.createDefinition(2);

    network.addComponent(feeder);
    network.addComponent(worker1).addInput(feeder.getAddress());
    network.addComponent(worker2).addInput(feeder.getAddress());

    worker3.addInput(worker1.getAddress());
    worker3.addInput(worker2.getAddress());
    network.addComponent(worker3);

    network.addComponent(worker4).addInput(worker3.getAddress()).groupBy(new RandomGrouping());

    Cluster cluster = new LocalCluster(vertx, container);
    cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          assertTrue(result.cause().getMessage(), result.succeeded());
        }
        else {
          assertTrue(result.succeeded());
          Listener listener = new DefaultListener(worker3.getAddress(), vertx);
          listener.messageHandler(new Handler<JsonMessage>() {
            @Override
            public void handle(JsonMessage message) {
              assertEquals(feeder.getAddress(), message.source());
              testComplete();
            }
          }).start();
        }
      }
    });
  }

}
