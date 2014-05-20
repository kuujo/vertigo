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

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.UUID;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.io.Feeder;
import net.kuujo.vertigo.io.FileReceiver;
import net.kuujo.vertigo.io.FileSender;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.io.selector.RoundRobinSelector;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

/**
 * I/O utilities tests.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class IoUtilitiesTest extends TestVerticle {

  public static class TestFeederSender extends ComponentVerticle {
    @Override
    public void start() {
      Feeder<OutputPort> feeder = new Feeder<>(output.port("out"));
      feeder.feedHandler(new Handler<OutputPort>() {
        @Override
        public void handle(OutputPort port) {
          port.send("Hello world!");
        }
      }).start();
    }
  }

  public static class TestFeederReceiver extends ComponentVerticle {
    private int count;
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String message) {
          assertEquals("Hello world!", message);
          count++;
          if (count == 10) {
            testComplete();
          }
        }
      });
    }
  }

  @Test
  public void testPortFeeder() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork("test");
        network.addVerticle("sender", TestFeederSender.class.getName());
        network.addVerticle("receiver", TestFeederReceiver.class.getName(), 4);
        network.createConnection("sender", "out", "receiver", "in").setSelector(new RoundRobinSelector());
        Cluster cluster = result.result();
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
    });
  }

  public static class TestFileSender extends ComponentVerticle {
    @Override
    public void start() {
      FileSender sender = new FileSender(output.port("out"));
      sender.sendFile("src/test/resources/test.txt", new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          assertTrue(result.succeeded());
        }
      });
    }
  }

  public static class TestFileReceiver extends ComponentVerticle {
    @Override
    public void start() {
      FileReceiver receiver = new FileReceiver(input.port("in"));
      receiver.fileHandler(new Handler<String>() {
        @Override
        public void handle(String filePath) {
          assertTrue(vertx.fileSystem().existsSync(filePath));
          vertx.fileSystem().deleteSync(filePath);
          testComplete();
        }
      });
    }
  }

  @Test
  public void testSendFile() {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = vertigo.createNetwork("test");
        network.addVerticle("sender", TestFileSender.class.getName());
        network.addVerticle("receiver", TestFileReceiver.class.getName(), 4);
        network.createConnection("sender", "out", "receiver", "in").setSelector(new RoundRobinSelector());
        Cluster cluster = result.result();
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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
    });
  }

}
