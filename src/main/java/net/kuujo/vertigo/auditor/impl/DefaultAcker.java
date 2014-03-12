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
package net.kuujo.vertigo.auditor.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.auditor.Acker;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * A remote acker.
 *
 * @author Jordan Halterman
 */
public class DefaultAcker implements Acker {
  private final String address = UUID.randomUUID().toString();
  private final EventBus eventBus;
  private Map<String, Long> children = new HashMap<>();
  private Handler<String> ackHandler;
  private Handler<String> failHandler;
  private Handler<String> timeoutHandler;

  public DefaultAcker(EventBus eventBus) {
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
  public Acker ackHandler(Handler<String> ackHandler) {
    this.ackHandler = ackHandler;
    return this;
  }

  private void doAck(Message<JsonObject> message) {
    if (ackHandler != null) {
      String id = message.body().getString("id");
      if (id != null) {
        ackHandler.handle(id);
      }
    }
  }

  @Override
  public Acker failHandler(Handler<String> failHandler) {
    this.failHandler = failHandler;
    return this;
  }

  private void doFail(Message<JsonObject> message) {
    if (failHandler != null) {
      String id = message.body().getString("id");
      if (id != null) {
        failHandler.handle(id);
      }
    }
  }

  @Override
  public Acker timeoutHandler(Handler<String> timeoutHandler) {
    this.timeoutHandler = timeoutHandler;
    return this;
  }

  private void doTimeout(Message<JsonObject> message) {
    if (timeoutHandler != null) {
      String id = message.body().getString("id");
      if (id != null) {
        timeoutHandler.handle(id);
      }
    }
  }

  @Override
  public Acker create(MessageId messageId, final Handler<AsyncResult<Void>> doneHandler) {
    eventBus.sendWithTimeout(messageId.auditor(), new JsonObject()
        .putString("action", "create")
        .putString("tree", messageId.tree())
        .putString("source", address), 30000, new Handler<AsyncResult<Message<Void>>>() {
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
  public Acker fork(MessageId messageId, List<MessageId> children) {
    Long ack = this.children.get(messageId.tree());
    if (ack == null) {
      ack = (long) 0;
    }
    for (MessageId child : children) {
      ack += child.ackCode();
    }
    this.children.put(messageId.tree(), ack);
    return this;
  }

  @Override
  public Acker commit(MessageId messageId) {
    Long ack = children.remove(messageId.tree());
    if (ack != null) {
      eventBus.send(messageId.auditor(), new JsonObject()
          .putString("action", "commit")
          .putString("tree", messageId.tree())
          .putNumber("parent", messageId.ackCode())
          .putNumber("children", ack));
    }
    else {
      eventBus.send(messageId.auditor(), new JsonObject()
          .putString("action", "commit")
          .putString("tree", messageId.tree())
          .putNumber("parent", messageId.ackCode()));
    }
    return this;
  }

  @Override
  public Acker ack(MessageId messageId) {
    Long ack = children.remove(messageId.tree());
    if (ack != null) {
      eventBus.send(messageId.auditor(), new JsonObject()
          .putString("action", "ack")
          .putString("tree", messageId.tree())
          .putNumber("parent", messageId.ackCode())
          .putNumber("children", ack));
    }
    else {
      eventBus.send(messageId.auditor(), new JsonObject()
          .putString("action", "ack")
          .putString("tree", messageId.tree())
          .putNumber("parent", messageId.ackCode()));
    }
    return this;
  }

  @Override
  public Acker fail(MessageId messageId) {
    children.remove(messageId.tree());
    eventBus.send(messageId.auditor(), new JsonObject()
        .putString("action", "fail")
        .putString("tree", messageId.tree()));
    return this;
  }

}
