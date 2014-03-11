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
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.java.BasicFeeder;
import net.kuujo.vertigo.java.BasicWorker;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.worker.Worker;

import org.junit.Test;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

/**
 * A network cluster test.
 *
 * @author Jordan Halterman
 */
public class ClusterTest extends TestVerticle {

  @Test
  public void testLocalDeploy() {
    Network network = new Network("test");
    network.setNumAuditors(2);
    network.addFeeder("test.feeder", TestFeeder.class.getName());
    network.addWorker("test.worker1", TestWorker.class.getName(), 2).addInput("test.feeder", "stream1");
    network.addWorker("test.worker2", TestWorker.class.getName(), 2).addInput("test.feeder", "stream2");

    Cluster cluster = new LocalCluster(this);
    cluster.deployNetwork(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        assertTrue(result.succeeded());
        testComplete();
      }
    });
  }

  @Test
  public void testLocalShutdown() {
    Network network = new Network("test");
    network.setNumAuditors(2);
    network.addFeeder("test.feeder", TestFeeder.class.getName());
    network.addWorker("test.worker1", TestWorker.class.getName(), 2).addInput("test.feeder", "stream1");
    network.addWorker("test.worker2", TestWorker.class.getName(), 2).addInput("test.feeder", "stream2");

    final Cluster cluster = new LocalCluster(this);
    cluster.deployNetwork(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        assertTrue(result.succeeded());
        final NetworkContext context = result.result();
        vertx.setTimer(2000, new Handler<Long>() {
          @Override
          public void handle(Long timerID) {
            cluster.shutdownNetwork(context, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  public static class TestFeeder extends BasicFeeder {
    
  }

  public static class TestWorker extends BasicWorker {
    @Override
    protected void handleMessage(JsonMessage message, Worker worker) {
      worker.ack(message);
    }
  }

}
