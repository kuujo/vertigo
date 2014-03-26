/*
 * Copyright 2014 the original author or authors.
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
import net.kuujo.vertigo.cluster.RemoteClusterManager;
import net.kuujo.vertigo.cluster.VertigoClusterManager;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.xync.test.integration.XyncTestVerticle;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A remote cluster test.
 *
 * @author Jordan Halterman
 */
public class RemoteClusterManagerTest extends XyncTestVerticle {

  @Test
  public void testLocalDeploy() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.setNumAuditors(2);
    network.addVerticle("feeder", TestFeeder.class.getName());
    network.addVerticle("worker1", TestWorker.class.getName(), 2);
    network.createConnection("feeder", "stream1", "worker", "stream1");
    network.addVerticle("worker2", TestWorker.class.getName(), 2);
    network.createConnection("feeder", "stream2", "worker", "stream2");

    VertigoClusterManager cluster = new RemoteClusterManager(this);
    cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
      @Override
      public void handle(AsyncResult<ActiveNetwork> result) {
        assertTrue(result.succeeded());
        testComplete();
      }
    });
  }

  @Test
  public void testLocalShutdown() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.setNumAuditors(2);
    network.addVerticle("feeder", TestFeeder.class.getName());
    network.addVerticle("worker1", TestWorker.class.getName(), 2);
    network.createConnection("feeder", "stream1", "worker", "stream1");
    network.addVerticle("worker2", TestWorker.class.getName(), 2);
    network.createConnection("feeder", "stream2", "worker", "stream2");

    final VertigoClusterManager cluster = new LocalClusterManager(this);
    cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
      @Override
      public void handle(AsyncResult<ActiveNetwork> result) {
        assertTrue(result.succeeded());
        vertx.setTimer(2000, new Handler<Long>() {
          @Override
          public void handle(Long timerID) {
            cluster.undeployNetwork("test", new Handler<AsyncResult<Void>>() {
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

  public static class TestFeeder extends ComponentVerticle {
  }

  public static class TestWorker extends ComponentVerticle {
  }

}
