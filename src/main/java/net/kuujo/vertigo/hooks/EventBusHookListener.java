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
package net.kuujo.vertigo.hooks;

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.impl.DefaultMessageId;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * An event bus hook listener.
 *
 * This class assists in listening for messages arriving on the event bus
 * via the {@link EventBusHook}.
 *
 * @author Jordan Halterman
 */
public class EventBusHookListener {
  private final String componentAddress;
  private final EventBus eventBus;

  public EventBusHookListener(String componentAddress, EventBus eventBus) {
    this.componentAddress = componentAddress;
    this.eventBus = eventBus;
    registerHandler();
  }

  /**
   * Registers the event bus handler.
   */
  private void registerHandler() {
  }

  /**
   * Sets a start handler.
   *
   * @param startHandler
   *   A start handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener startHandler(final Handler<InstanceContext<?>> startHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.start", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          startHandler.handle(InstanceContext.fromJson(body));
        }
      }
    });
    return this;
  }

  /**
   * Sets a receive handler.
   *
   * @param receiveHandler
   *   A receive handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener receiveHandler(final Handler<MessageId> receiveHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.receive", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          receiveHandler.handle(DefaultMessageId.fromJson(body));
        }
      }
    });
    return this;
  }

  /**
   * Sets a ack handler.
   *
   * @param ackHandler
   *   A ack handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener ackHandler(final Handler<MessageId> ackHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.ack", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          ackHandler.handle(DefaultMessageId.fromJson(body));
        }
      }
    });
    return this;
  }

  /**
   * Sets a fail handler.
   *
   * @param failHandler
   *   A fail handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener failHandler(final Handler<MessageId> failHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.fail", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          failHandler.handle(DefaultMessageId.fromJson(body));
        }
      }
    });
    return this;
  }

  /**
   * Sets an emit handler.
   *
   * @param emitHandler
   *   An emit handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener emitHandler(final Handler<MessageId> emitHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.emit", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          emitHandler.handle(DefaultMessageId.fromJson(body));
        }
      }
    });
    return this;
  }

  /**
   * Sets an acked handler.
   *
   * @param ackedHandler
   *   An acked handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener ackedHandler(final Handler<MessageId> ackedHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.acked", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          ackedHandler.handle(DefaultMessageId.fromJson(body));
        }
      }
    });
    return this;
  }

  /**
   * Sets a failed handler.
   *
   * @param failedHandler
   *   A failed handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener failedHandler(final Handler<MessageId> failedHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.failed", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          failedHandler.handle(DefaultMessageId.fromJson(body));
        }
      }
    });
    return this;
  }

  /**
   * Sets a timeout handler.
   *
   * @param timeoutHandler
   *   A timeout handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener timeoutHandler(final Handler<MessageId> timeoutHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.timeout", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          timeoutHandler.handle(DefaultMessageId.fromJson(body));
        }
      }
    });
    return this;
  }

  /**
   * Sets a stop handler.
   *
   * @param stopHandler
   *   A stop handler.
   * @return
   *   The called listener instance.
   */
  public EventBusHookListener stopHandler(final Handler<InstanceContext<?>> stopHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.stop", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          stopHandler.handle(InstanceContext.fromJson(body));
        }
      }
    });
    return this;
  }

}
