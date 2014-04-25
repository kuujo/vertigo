/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.hook;

import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.component.impl.DefaultInstanceContext;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Event bus hook listener.
 *
 * This class assists in listening for messages arriving on the event bus
 * via the {@link EventBusHook}.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
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
  public EventBusHookListener startHandler(final Handler<InstanceContext> startHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.start", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          startHandler.handle(DefaultInstanceContext.fromJson(body));
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
  @SuppressWarnings("rawtypes")
  public EventBusHookListener receiveHandler(final Handler receiveHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.receive", componentAddress), new Handler<Message<Object>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(Message<Object> message) {
        receiveHandler.handle(message.body());
      }
    });
    return this;
  }

  /**
   * Sets a send handler.
   *
   * @param sendHandler
   *   A send handler.
   * @return
   *   The called listener instance.
   */
  @SuppressWarnings("rawtypes")
  public EventBusHookListener emitHandler(final Handler sendHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.emit", componentAddress), new Handler<Message<Object>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(Message<Object> message) {
        sendHandler.handle(message.body());
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
  public EventBusHookListener stopHandler(final Handler<InstanceContext> stopHandler) {
    eventBus.registerHandler(String.format("vertigo.hooks.%s.stop", componentAddress), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          stopHandler.handle(DefaultInstanceContext.fromJson(body));
        }
      }
    });
    return this;
  }

}
