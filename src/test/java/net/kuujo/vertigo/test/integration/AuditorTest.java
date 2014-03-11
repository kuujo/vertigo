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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.kuujo.vertigo.acker.Acker;
import net.kuujo.vertigo.acker.DefaultAcker;
import net.kuujo.vertigo.auditor.AuditorVerticle;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.impl.DefaultJsonMessage;
import net.kuujo.vertigo.message.impl.DefaultMessageId;

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
  private final Random random = new Random();

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

            JsonMessage message = createNewMessage("default", new JsonObject().putString("foo", "bar"));
            MessageId source = message.messageId();
            acker.ackHandler(ackHandler(source));
            List<MessageId> children = new ArrayList<MessageId>();
            for (int i = 0; i < 5; i++) {
              children.add(createChildMessage("default", new JsonObject().putString("foo", "bar"), message).messageId());
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
  public void testComplexAck() {
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

            JsonMessage message = createNewMessage("default", new JsonObject().putString("foo", "bar"));
            MessageId sourceId = message.messageId();
            acker.ackHandler(ackHandler(sourceId));

            List<JsonMessage> children = new ArrayList<>();
            List<MessageId> childIds = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
              JsonMessage child = createChildMessage("default", new JsonObject().putString("foo", "bar"), message);
              children.add(child);
              childIds.add(child.messageId());
            }
            acker.fork(sourceId, childIds);
            acker.create(sourceId);

            List<MessageId> descendantIds = new ArrayList<>();
            for (JsonMessage child : children) {
              JsonMessage descendant = createChildMessage("default", new JsonObject().putString("foo", "bar"), child);
              acker.fork(child.messageId(), Arrays.asList(new MessageId[]{descendant.messageId()}));
              acker.create(child.messageId());
              descendantIds.add(descendant.messageId());
              acker.ack(child.messageId());
            }

            for (MessageId id : descendantIds) {
              acker.ack(id);
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

            JsonMessage message = createNewMessage("default", new JsonObject().putString("foo", "bar"));
            MessageId source = message.messageId();
            acker.failHandler(failHandler(source));
            List<MessageId> children = new ArrayList<MessageId>();
            for (int i = 0; i < 5; i++) {
              children.add(createChildMessage("default", new JsonObject().putString("foo", "bar"), message).messageId());
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
            JsonMessage message = createNewMessage("default", new JsonObject().putString("foo", "bar"));
            MessageId source = message.messageId();
            acker.timeoutHandler(timeoutHandler(source));
            List<MessageId> children = new ArrayList<MessageId>();
            for (int i = 0; i < 5; i++) {
              children.add(createChildMessage("default", new JsonObject().putString("foo", "bar"), message).messageId());
            }
            acker.fork(source, children);
            acker.create(source);
          }
        });
      }
    });
  }

  /**
   * Creates a new message.
   */
  private JsonMessage createNewMessage(String stream, JsonObject body) {
    MessageId messageId = DefaultMessageId.Builder.newBuilder()
        .setCorrelationId(UUID.randomUUID().toString())
        .setAuditor("auditor")
        .setCode(random.nextInt())
        .setOwner("test")
        .build();
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setMessageId(messageId)
        .setBody(body)
        .setSource("test")
        .setStream(stream)
        .build();
    return message;
  }

  /**
   * Creates a child message.
   */
  private JsonMessage createChildMessage(String stream, JsonObject body, JsonMessage parent) {
    MessageId messageId = DefaultMessageId.Builder.newBuilder()
        .setAuditor(parent.messageId().auditor())
        .setCode(random.nextInt())
        .setOwner("test")
        .setParent(parent.messageId().correlationId())
        .setRoot(parent.messageId().hasRoot() ? parent.messageId().root() : parent.messageId().correlationId())
        .build();
    JsonMessage message = DefaultJsonMessage.Builder.newBuilder()
        .setMessageId(messageId)
        .setBody(body)
        .setSource("test")
        .setStream(stream)
        .build();
    return message;
  }

}
