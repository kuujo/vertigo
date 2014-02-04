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
package net.kuujo.vertigo.auditor;

import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.impl.DefaultMessageId;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Observes component message trees and manages ack/fail.
 *
 * @author Jordan Halterman
 */
public final class AuditorVerticle extends BusModBase {
  private String address;
  private static final String DEFAULT_AUDITOR = BasicAuditor.class.getName();
  private Auditor auditor;

  public static final String AUDITOR = "auditor";
  public static final String ADDRESS = "address";
  public static final String TIMEOUT = "timeout";

  @Override
  public void start(final Future<Void> future) {
    super.start();
    address = getMandatoryStringConfig(ADDRESS);
    String auditorClass = config.getString(AUDITOR, DEFAULT_AUDITOR);
    try {
      auditor = (Auditor) Class.forName(auditorClass).newInstance();
      auditor.setVertx(vertx);
      auditor.setContainer(container);
      auditor.setAcker(this);
      auditor.setTimeout(getOptionalLongConfig(TIMEOUT, 30000));
    }
    catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      logger.error(e);
    }

    eb.registerHandler(address, handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          auditor.start();
          future.setResult(null);
        }
      }
    });
  }

  private Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body != null) {
        String action = body.getString("action");
        switch (action) {
          case "create":
            doCreate(body);
            break;
          case "ack":
            doAck(body);
            break;
          case "fail":
            doFail(body);
            break;
          case "shutdown":
            sendOK(message);
            container.exit();
            break;
          default:
            sendError(message, "Invalid action.");
            break;
        }
      }
    }
  };

  /**
   * Creates a message.
   */
  private void doCreate(JsonObject body) {
    JsonObject info = body.getObject("id");
    if (info != null) {
      MessageId id = DefaultMessageId.fromJson(info);
      JsonArray children = body.getArray("children");
      if (children == null) {
        complete(id);
        return;
      }

      auditor.create(id);
      for (Object child : children) {
        auditor.fork(DefaultMessageId.fromJson((JsonObject) child));
      }
    }
  }

  /**
   * Acks a message.
   */
  private void doAck(JsonObject body) {
    JsonObject info = body.getObject("id");
    if (info != null) {
      // It's very important that this be done in this order. Child message IDs
      // must be stored by calling fork() *prior* to acking the given message ID.
      MessageId id = DefaultMessageId.fromJson(info);
      JsonArray children = body.getArray("children");
      if (children != null) {
        for (Object child : children) {
          auditor.fork(DefaultMessageId.fromJson((JsonObject) child));
        }
      }
      auditor.ack(id);
    }
  }

  /**
   * Fails a message.
   */
  private void doFail(JsonObject body) {
    JsonObject info = body.getObject("id");
    if (info != null) {
      auditor.fail(DefaultMessageId.fromJson(info));
    }
  }

  /**
   * Completes processing of a message.
   *
   * @param messageId
   *   The root message ID.
   */
  public void complete(MessageId messageId) {
    eb.send(messageId.owner(), new JsonObject().putString("action", "ack")
        .putObject("id", messageId.toJson()));
  }

  /**
   * Fails processing of a message.
   *
   * @param messageId
   *   The root message ID.
   */
  public void fail(MessageId messageId) {
    eb.send(messageId.owner(), new JsonObject().putString("action", "fail")
        .putObject("id", messageId.toJson()));
  }

  /**
   * Times out processing of a message.
   *
   * @param messageId
   *   The root message ID.
   */
  public void timeout(MessageId messageId) {
    eb.send(messageId.owner(), new JsonObject().putString("action", "timeout")
        .putObject("id", messageId.toJson()));
  }

  @Override
  public void stop() {
    auditor.stop();
  }

}
