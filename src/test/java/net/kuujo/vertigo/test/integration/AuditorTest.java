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

import java.util.ArrayList;
import java.util.List;

import net.kuujo.vertigo.acker.Acker;
import net.kuujo.vertigo.acker.DefaultAcker;
import net.kuujo.vertigo.auditor.AuditorVerticle;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.impl.JsonMessageBuilder;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.testtools.TestVerticle;

/**
 * A network auditor test.
 *
 * @author Jordan Halterman
 */
public class AuditorTest extends TestVerticle {

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

  private Handler<MessageId> ackHandler(final MessageId id) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        assertEquals(id.correlationId(), messageId.correlationId());
        testComplete();
      }
    };
  }

  private Handler<MessageId> failHandler(final MessageId id) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        assertEquals(id.correlationId(), messageId.correlationId());
        testComplete();
      }
    };
  }

  private Handler<MessageId> timeoutHandler(final MessageId id) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        assertEquals(id.correlationId(), messageId.correlationId());
        testComplete();
      }
    };
  }

  @Test
  public void testAck() {
    final String auditor = "auditor";
    deployAuditor(auditor, 30000, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        final Acker acker = new DefaultAcker("test", vertx.eventBus());
        acker.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            JsonMessageBuilder builder = new JsonMessageBuilder("test");
            JsonMessage message = builder.createNew(auditor).toMessage();
            MessageId source = message.messageId();
            acker.ackHandler(ackHandler(source));
            List<MessageId> children = new ArrayList<MessageId>();
            for (int i = 0; i < 5; i++) {
              children.add(builder.createChild(message).toMessage().messageId());
            }
            acker.fork(source, children);
            acker.create(source);

            for (MessageId child : children) {
              acker.ack(child);
            }
          }
        });
      }
    });
  }

  @Test
  public void testFail() {
    final String auditor = "auditor";
    deployAuditor(auditor, 30000, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        final Acker acker = new DefaultAcker("test", vertx.eventBus());
        acker.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());

            JsonMessageBuilder builder = new JsonMessageBuilder("test");
            JsonMessage message = builder.createNew(auditor).toMessage();
            MessageId source = message.messageId();
            acker.failHandler(failHandler(source));
            List<MessageId> children = new ArrayList<MessageId>();
            for (int i = 0; i < 5; i++) {
              children.add(builder.createChild(message).toMessage().messageId());
            }
            acker.fork(source, children);
            acker.create(source);
            acker.ack(children.get(0));
            acker.ack(children.get(1));
            acker.fail(children.get(2));
            acker.ack(children.get(3));
            acker.ack(children.get(4));
          }
        });
      }
    });
  }

  @Test
  public void testTimeout() {
    final String auditor = "auditor";
    deployAuditor(auditor, 500, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        final Acker acker = new DefaultAcker("test", vertx.eventBus());
        acker.start(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            JsonMessageBuilder builder = new JsonMessageBuilder("test");
            JsonMessage message = builder.createNew(auditor).toMessage();
            MessageId source = message.messageId();
            acker.timeoutHandler(timeoutHandler(source));
            List<MessageId> children = new ArrayList<MessageId>();
            for (int i = 0; i < 5; i++) {
              children.add(builder.createChild(message).toMessage().messageId());
            }
            acker.fork(source, children);
            acker.create(source);
          }
        });
      }
    });
  }

}
