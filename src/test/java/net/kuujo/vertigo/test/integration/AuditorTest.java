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

import net.kuujo.vertigo.auditor.Auditor;
import net.kuujo.vertigo.messaging.DefaultJsonMessage;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.testtools.TestVerticle;

/**
 * A network deploy test.
 *
 * @author Jordan Halterman
 */
public class AuditorTest extends TestVerticle {

  private void deployAuditor(String address, String broadcast, Handler<AsyncResult<Void>> doneHandler) {
    deployAuditor(address, broadcast, true, 30000, 0, doneHandler);
  }

  private void deployAuditor(String address, String broadcast, long expire, Handler<AsyncResult<Void>> doneHandler) {
    deployAuditor(address, broadcast, true, expire, 0, doneHandler);
  }

  private void deployAuditor(String address, String broadcast, boolean enabled, long expire, long delay, Handler<AsyncResult<Void>> doneHandler) {
    JsonObject config = new JsonObject()
      .putString(Auditor.ADDRESS, address)
      .putString(Auditor.BROADCAST, broadcast)
      .putBoolean(Auditor.ENABLED, enabled)
      .putNumber(Auditor.EXPIRE, expire)
      .putNumber(Auditor.DELAY, delay);

    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    container.deployVerticle(Auditor.class.getName(), config, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) future.setFailure(result.cause()); else future.setResult(null);
      }
    });
  }

  private Handler<Message<JsonObject>> ackHandler(final String id) {
    return new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        assertNotNull(body);
        String action = body.getString("action");
        assertEquals("ack", action);
        String returnId = body.getString("id");
        assertEquals(id, returnId);
        testComplete();
      }
    };
  }

  private Handler<Message<JsonObject>> failHandler(final String id) {
    return new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        assertNotNull(body);
        String action = body.getString("action");
        assertEquals("fail", action);
        String returnId = body.getString("id");
        assertEquals(id, returnId);
        testComplete();
      }
    };
  }

  @Test
  public void testAck() {
    final String auditor = "test";
    final JsonMessage source = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), auditor);

    vertx.eventBus().registerHandler("broadcast", ackHandler(source.id()), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployAuditor(auditor, "broadcast", new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            final EventBus eventBus = vertx.eventBus();
            final JsonMessage test1 = source.createChild();
            final JsonMessage test2 = source.createChild();
            final JsonMessage test3 = test1.createChild();
            final JsonMessage test4 = test1.createChild();

            run(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "create").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test1.id()).putString("parent", test1.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test2.id()).putString("parent", test2.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test3.id()).putString("parent", test3.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test4.id()).putString("parent", test4.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test1.id()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test2.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test3.id()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test4.id()));
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testChildFail() {
    final String auditor = "test";
    final JsonMessage source = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), auditor);

    vertx.eventBus().registerHandler("broadcast", failHandler(source.id()), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployAuditor(auditor, "broadcast", new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            final EventBus eventBus = vertx.eventBus();
            run(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "create").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fail").putString("id", source.id()));
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testDescendantFail() {
    final String auditor = "test";
    final JsonMessage source = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), auditor);

    vertx.eventBus().registerHandler("broadcast", failHandler(source.id()), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployAuditor(auditor, "broadcast", new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            final EventBus eventBus = vertx.eventBus();
            final JsonMessage test1 = source.createChild();
            final JsonMessage test2 = source.createChild();
            final JsonMessage test3 = test1.createChild();
            final JsonMessage test4 = test1.createChild();

            run(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "create").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test1.id()).putString("parent", test1.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test2.id()).putString("parent", test2.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test3.id()).putString("parent", test3.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test4.id()).putString("parent", test4.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test1.id()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test2.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test3.id()));
                eventBus.send(auditor, new JsonObject().putString("action", "fail").putString("id", test4.id()));
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testExpire() {
    final String auditor = "test";
    final JsonMessage source = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), auditor);

    vertx.eventBus().registerHandler("broadcast", failHandler(source.id()), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployAuditor(auditor, "broadcast", 1, new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            final EventBus eventBus = vertx.eventBus();
            final JsonMessage test1 = source.createChild();
            final JsonMessage test2 = source.createChild();
            run(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "create").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test1.id()).putString("parent", test1.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test2.id()).putString("parent", test2.parent()));
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testDelayedForkAck() {
    final String auditor = "test";
    final JsonMessage source = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), auditor);

    vertx.eventBus().registerHandler("broadcast", ackHandler(source.id()), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployAuditor(auditor, "broadcast", true, 30000, 1000, new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            final EventBus eventBus = vertx.eventBus();
            final JsonMessage test1 = source.createChild();
            final JsonMessage test2 = source.createChild();
            final JsonMessage test3 = test1.createChild();
            final JsonMessage test4 = test1.createChild();

            run(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "create").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test1.id()).putString("parent", test1.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test2.id()).putString("parent", test2.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test1.id()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test2.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test3.id()).putString("parent", test3.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test4.id()).putString("parent", test4.parent()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test3.id()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test4.id()));
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testDelayedForkFail() {
    final String auditor = "test";
    final JsonMessage source = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), auditor);

    vertx.eventBus().registerHandler("broadcast", failHandler(source.id()), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        deployAuditor(auditor, "broadcast", true, 30000, 1000, new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            final EventBus eventBus = vertx.eventBus();
            final JsonMessage test1 = source.createChild();
            final JsonMessage test2 = source.createChild();
            final JsonMessage test3 = test1.createChild();
            final JsonMessage test4 = test1.createChild();

            run(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "create").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test1.id()).putString("parent", test1.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test2.id()).putString("parent", test2.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", source.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test1.id()));
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test2.id()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test3.id()).putString("parent", test3.parent()));
                eventBus.send(auditor, new JsonObject().putString("action", "fork").putString("id", test4.id()).putString("parent", test4.parent()));
              }
            })
            .then(new VoidHandler() {
              @Override
              protected void handle() {
                eventBus.send(auditor, new JsonObject().putString("action", "ack").putString("id", test3.id()));
                eventBus.send(auditor, new JsonObject().putString("action", "fail").putString("id", test4.id()));
              }
            });
          }
        });
      }
    });
  }

  /**
   * Runs a near-immediate event.
   */
  private Deferred run(Handler<Void> handler) {
    return new Deferred(1, handler, vertx).start();
  }

  /**
   * A defer helper for simulating events over time.
   */
  private static class Deferred {
    public static final long DEFAULT_DELAY = 100;
    private final long delay;
    private final Handler<Void> handler;
    private final Vertx vertx;
    private Deferred after;

    public Deferred(long delay, Handler<Void> handler, Vertx vertx) {
      this.delay = delay;
      this.handler = handler;
      this.vertx = vertx;
    }

    /**
     * Starts the deferred event, setting a Vert.x timer.
     */
    public Deferred start() {
      vertx.setTimer(delay, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          handler.handle(null);
          if (after != null) {
            after.start();
          }
        }
      });
      return this;
    }

    /**
     * Adds a deferred event after this event.
     */
    public Deferred then(Handler<Void> handler) {
      return then(DEFAULT_DELAY, handler);
    }

    /**
     * Adds a deferred event to be triggered after this event.
     */
    public Deferred then(long delay, Handler<Void> handler) {
      Deferred deferred = new Deferred(delay, handler, vertx);
      this.after = deferred;
      return deferred;
    }
  }

}
