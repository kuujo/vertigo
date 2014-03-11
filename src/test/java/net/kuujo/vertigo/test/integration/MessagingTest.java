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

import net.kuujo.vertigo.acker.Acker;
import net.kuujo.vertigo.acker.DefaultAcker;
import net.kuujo.vertigo.auditor.AuditorVerticle;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.impl.ContextBuilder;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.input.impl.DefaultInputCollector;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.output.impl.DefaultOutputCollector;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

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

  private final Handler<MessageId> emptyHandler = new Handler<MessageId>() {
    public void handle(MessageId messageId) {
    }
  };

  private final Handler<MessageId> completeHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      testComplete();
    }
  };

  @Test
  public void testAck() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", "feeder.py", 2);
    network.addWorkerVerticle("worker", "worker.py", 2).addInput("feeder");
    NetworkContext context = ContextBuilder.buildContext(network);
    final Acker acker1 = new DefaultAcker(context.component("feeder").instance(1).address(), vertx.eventBus());
    final OutputCollector output = new DefaultOutputCollector(vertx, context.component("feeder").instance(1).output(), acker1);
    final Acker acker2 = new DefaultAcker(context.component("worker").instance(1).address(), vertx.eventBus());
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
                input.messageHandler(new Handler<JsonMessage>() {
                  @Override
                  public void handle(JsonMessage message) {
                    input.ack(message);
                  }
                });
                input.start(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue(result.succeeded());
                    output.ackHandler(completeHandler);
                    output.failHandler(emptyHandler);
                    output.timeoutHandler(emptyHandler);
                    output.start(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        assertTrue(result.succeeded());
                        output.emit(new JsonObject().putString("foo", "bar"));
                        output.emit(new JsonObject().putString("foo", "bar"));
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
    Network network = new Network("test");
    network.addFeederVerticle("feeder", "feeder.py", 2);
    network.addWorkerVerticle("worker", "worker.py", 2).addInput("feeder");
    NetworkContext context = ContextBuilder.buildContext(network);
    final Acker acker1 = new DefaultAcker(context.component("feeder").instance(1).address(), vertx.eventBus());
    final OutputCollector output = new DefaultOutputCollector(vertx, context.component("feeder").instance(1).output(), acker1);
    final Acker acker2 = new DefaultAcker(context.component("worker").instance(1).address(), vertx.eventBus());
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
                input.messageHandler(new Handler<JsonMessage>() {
                  @Override
                  public void handle(JsonMessage message) {
                    input.fail(message);
                  }
                });
                input.start(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue(result.succeeded());
                    output.ackHandler(emptyHandler);
                    output.failHandler(completeHandler);
                    output.timeoutHandler(emptyHandler);
                    output.start(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        assertTrue(result.succeeded());
                        output.emit(new JsonObject().putString("foo", "bar"));
                        output.emit(new JsonObject().putString("foo", "bar"));
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
    Network network = new Network("test");
    network.addFeederVerticle("feeder", "feeder.py", 2);
    network.addWorkerVerticle("worker", "worker.py", 2).addInput("feeder");
    NetworkContext context = ContextBuilder.buildContext(network);
    final Acker acker1 = new DefaultAcker(context.component("feeder").instance(1).address(), vertx.eventBus());
    final OutputCollector output = new DefaultOutputCollector(vertx, context.component("feeder").instance(1).output(), acker1);
    final Acker acker2 = new DefaultAcker(context.component("worker").instance(1).address(), vertx.eventBus());
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
                input.messageHandler(new Handler<JsonMessage>() {
                  @Override
                  public void handle(JsonMessage message) {
                  }
                });
                input.start(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue(result.succeeded());
                    output.ackHandler(emptyHandler);
                    output.failHandler(emptyHandler);
                    output.timeoutHandler(completeHandler);
                    output.start(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        assertTrue(result.succeeded());
                        output.emit(new JsonObject().putString("foo", "bar"));
                        output.emit(new JsonObject().putString("foo", "bar"));
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
