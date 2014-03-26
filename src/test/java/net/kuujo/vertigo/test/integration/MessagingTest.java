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
import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.impl.ContextBuilder;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.input.impl.DefaultInputCollector;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.auditor.Acker;
import net.kuujo.vertigo.network.auditor.AuditorVerticle;
import net.kuujo.vertigo.network.auditor.impl.DefaultAcker;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.output.impl.DefaultOutputCollector;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

/**
 * A messaging test.
 *
 * @author Jordan Halterman
 */
public class MessagingTest extends TestVerticle {

  private void deployAuditor(String address, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    JsonObject config = new JsonObject()
      .putString("address", address)
      .putNumber("timeout", timeout);

    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    container.deployVerticle(AuditorVerticle.class.getName(), config, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) future.setFailure(result.cause()); else future.setResult(null);
      }
    });
  }

  @Test
  public void testAck() {
    final VertigoCluster cluster = new LocalCluster(vertx, container);
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", "feeder.py", 2);
    network.addVerticle("worker", "worker.py", 2);
    network.createConnection("feeder", "worker");
    NetworkContext context = ContextBuilder.buildContext(network, cluster);
    final Acker acker1 = new DefaultAcker(vertx.eventBus(), context.auditors());
    final OutputCollector output = new DefaultOutputCollector(vertx, context.component("feeder").instance(1).output(), acker1);
    final Acker acker2 = new DefaultAcker(vertx.eventBus(), context.auditors());
    final InputCollector input = new DefaultInputCollector(vertx, context.component("worker").instance(1).input(), acker2);

    deployAuditor(context.auditors().iterator().next(), 30000, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        acker1.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            acker2.start(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                input.stream("default").messageHandler(new Handler<JsonMessage>() {
                  @Override
                  public void handle(JsonMessage message) {
                    input.stream("default").ack(message);
                  }
                });
                input.open(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue(result.succeeded());
                    output.addHook(new OutputHook() {
                      @Override
                      public void handleEmit(String messageId) {
                        
                      }
                      @Override
                      public void handleAcked(String messageId) {
                        testComplete();
                      }
                      @Override
                      public void handleFailed(String messageId) {
                        
                      }
                      @Override
                      public void handleTimeout(String messageId) {
                        
                      }
                    });
                    output.open(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        assertTrue(result.succeeded());
                        output.stream("default").emit(new JsonObject().putString("foo", "bar"));
                        output.stream("default").emit(new JsonObject().putString("foo", "bar"));
                      }
                    });
                  }
                });
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testFail() {
    final VertigoCluster cluster = new LocalCluster(vertx, container);
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", "feeder.py", 2);
    network.addVerticle("worker", "worker.py", 2);
    network.createConnection("feeder", "worker");
    NetworkContext context = ContextBuilder.buildContext(network, cluster);
    final Acker acker1 = new DefaultAcker(vertx.eventBus(), context.auditors());
    final OutputCollector output = new DefaultOutputCollector(vertx, context.component("feeder").instance(1).output(), acker1);
    final Acker acker2 = new DefaultAcker(vertx.eventBus(), context.auditors());
    final InputCollector input = new DefaultInputCollector(vertx, context.component("worker").instance(1).input(), acker2);

    deployAuditor(context.auditors().iterator().next(), 30000, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        acker1.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            acker2.start(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                input.stream("default").messageHandler(new Handler<JsonMessage>() {
                  @Override
                  public void handle(JsonMessage message) {
                    input.stream("default").fail(message);
                  }
                });
                input.open(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue(result.succeeded());
                    output.addHook(new OutputHook() {
                      @Override
                      public void handleEmit(String messageId) {
                        
                      }
                      @Override
                      public void handleAcked(String messageId) {
                        
                      }
                      @Override
                      public void handleFailed(String messageId) {
                        testComplete();
                      }
                      @Override
                      public void handleTimeout(String messageId) {
                        
                      }
                    });
                    output.open(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        assertTrue(result.succeeded());
                        output.stream("default").emit(new JsonObject().putString("foo", "bar"));
                        output.stream("default").emit(new JsonObject().putString("foo", "bar"));
                      }
                    });
                  }
                });
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testTimeout() {
    final VertigoCluster cluster = new LocalCluster(vertx, container);
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", "feeder.py", 2);
    network.addVerticle("worker", "worker.py", 2);
    network.createConnection("feeder", "worker");
    NetworkContext context = ContextBuilder.buildContext(network, cluster);
    final Acker acker1 = new DefaultAcker(vertx.eventBus(), context.auditors());
    final OutputCollector output = new DefaultOutputCollector(vertx, context.component("feeder").instance(1).output(), acker1);
    final Acker acker2 = new DefaultAcker(vertx.eventBus(), context.auditors());
    final InputCollector input = new DefaultInputCollector(vertx, context.component("worker").instance(1).input(), acker2);

    deployAuditor(context.auditors().iterator().next(), 1000, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        acker1.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            acker2.start(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                input.stream("default").messageHandler(new Handler<JsonMessage>() {
                  @Override
                  public void handle(JsonMessage message) {
                  }
                });
                input.open(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue(result.succeeded());
                    output.addHook(new OutputHook() {
                      @Override
                      public void handleEmit(String messageId) {
                        
                      }
                      @Override
                      public void handleAcked(String messageId) {
                        
                      }
                      @Override
                      public void handleFailed(String messageId) {
                        
                      }
                      @Override
                      public void handleTimeout(String messageId) {
                        testComplete();
                      }
                    });
                    output.open(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        assertTrue(result.succeeded());
                        output.stream("default").emit(new JsonObject().putString("foo", "bar"));
                        output.stream("default").emit(new JsonObject().putString("foo", "bar"));
                      }
                    });
                  }
                });
              }
            });
          }
        });
      }
    });
  }

}
