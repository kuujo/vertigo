package net.kuujo.vertigo.auditor.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.auditor.Acker;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.impl.DefaultMessageId;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A remote acker.
 *
 * @author Jordan Halterman
 */
public class DefaultAcker implements Acker {
  private final String address;
  private final EventBus eventBus;
  private Map<String, List<MessageId>> children = new HashMap<>();
  private Handler<MessageId> ackHandler;
  private Handler<MessageId> failHandler;
  private Handler<MessageId> timeoutHandler;

  public DefaultAcker(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  @Override
  public Acker start(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.registerHandler(address, handler, doneHandler);
    return this;
  }

  private final Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body != null) {
        String action = body.getString("action");
        if (action != null) {
          switch (action) {
            case "ack":
              doAck(message);
              break;
            case "fail":
              doFail(message);
              break;
            case "timeout":
              doTimeout(message);
              break;
          }
        }
      }
    }
  };

  @Override
  public Acker ackHandler(Handler<MessageId> ackHandler) {
    this.ackHandler = ackHandler;
    return this;
  }

  private void doAck(Message<JsonObject> message) {
    if (ackHandler != null) {
      JsonObject id = message.body().getObject("id");
      if (id != null) {
        ackHandler.handle(DefaultMessageId.fromJson(id));
      }
    }
  }

  @Override
  public Acker failHandler(Handler<MessageId> failHandler) {
    this.failHandler = failHandler;
    return this;
  }

  private void doFail(Message<JsonObject> message) {
    if (failHandler != null) {
      JsonObject id = message.body().getObject("id");
      if (id != null) {
        failHandler.handle(DefaultMessageId.fromJson(id));
      }
    }
  }

  @Override
  public Acker timeoutHandler(Handler<MessageId> timeoutHandler) {
    this.timeoutHandler = timeoutHandler;
    return this;
  }

  private void doTimeout(Message<JsonObject> message) {
    if (timeoutHandler != null) {
      JsonObject id = message.body().getObject("id");
      if (id != null) {
        timeoutHandler.handle(DefaultMessageId.fromJson(id));
      }
    }
  }

  @Override
  public Acker create(MessageId messageId, final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.sendWithTimeout(messageId.auditor(), new JsonObject()
        .putString("action", "create")
        .putObject("id", messageId.toJson()), 30000, new Handler<AsyncResult<Message<Void>>>() {
          @Override
          public void handle(AsyncResult<Message<Void>> result) {
            if (result.failed()) {
              new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
            }
            else {
              new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
            }
          }
    });
    return this;
  }

  @Override
  public Acker commit(MessageId messageId) {
    List<MessageId> messageIds = children.remove(messageId.correlationId());
    if (messageIds != null && !messageIds.isEmpty()) {
      eventBus.send(messageId.auditor(), new JsonObject()
          .putString("action", "commit")
          .putObject("id", messageId.toJson())
          .putArray("children", messageIdsToArray(messageIds)));
    }
    else {
      eventBus.send(messageId.auditor(), new JsonObject()
          .putString("action", "commit")
          .putObject("id", messageId.toJson()));
    }
    return this;
  }

  @Override
  public Acker fork(MessageId messageId, List<MessageId> children) {
    List<MessageId> messageIds = this.children.get(messageId.correlationId());
    if (messageIds == null) {
      this.children.put(messageId.correlationId(), children);
    }
    else {
      messageIds.addAll(children);
    }
    return this;
  }

  @Override
  public Acker ack(MessageId messageId) {
    List<MessageId> messageIds = children.remove(messageId.correlationId());
    if (messageIds != null) {
      eventBus.send(messageId.auditor(), new JsonObject()
          .putString("action", "ack")
          .putObject("id", messageId.toJson())
          .putArray("children", messageIdsToArray(messageIds)));
    }
    else {
      eventBus.send(messageId.auditor(), new JsonObject()
          .putString("action", "ack")
          .putObject("id", messageId.toJson()));
    }
    return this;
  }

  @Override
  public Acker fail(MessageId messageId) {
    children.remove(messageId.correlationId());
    eventBus.send(messageId.auditor(), new JsonObject()
        .putString("action", "fail")
        .putObject("id", messageId.toJson()));
    return this;
  }

  /**
   * Converts a list of message IDs into a JSON array.
   */
  private static JsonArray messageIdsToArray(List<MessageId> messageIds) {
    JsonArray ids = new JsonArray();
    for (MessageId id : messageIds) {
      ids.add(id.toJson());
    }
    return ids;
  }

}
