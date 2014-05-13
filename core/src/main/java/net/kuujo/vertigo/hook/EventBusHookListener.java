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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Event bus hook listener.<p>
 *
 * This class assists in listening for messages arriving on the event bus
 * via the {@link EventBusHook}.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class EventBusHookListener {
  private final String address;
  private final EventBus eventBus;
  private Handler<InstanceContext> startHandler;
  @SuppressWarnings("rawtypes")
  private Handler sendHandler;
  @SuppressWarnings("rawtypes")
  private Handler receiveHandler;
  private Handler<InstanceContext> stopHandler;

  private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
    @Override
    @SuppressWarnings("unchecked")
    public void handle(Message<JsonObject> message) {
      String event = message.body().getString("event");
      if (event != null) {
        switch (event) {
          case "start":
            if (startHandler != null) {
              startHandler.handle(DefaultInstanceContext.fromJson(message.body().getObject("context")));
            }
            break;
          case "send":
            if (sendHandler != null) {
              sendHandler.handle(message.body().getValue("message"));
            }
            break;
          case "receive":
            if (receiveHandler != null) {
              receiveHandler.handle(message.body().getValue("message"));
            }
            break;
          case "stop":
            if (stopHandler != null) {
              stopHandler.handle(DefaultInstanceContext.fromJson(message.body().getObject("context")));
            }
            break;
        }
      }
    }
  };

  public EventBusHookListener(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  /**
   * Starts the hook listener, registering a handler on the event bus.
   *
   * @return The hook listener.
   */
  public EventBusHookListener start() {
    return start(null);
  }

  /**
   * Starts the hook listener, registering a handler on the event bus.
   *
   * @param doneHandler An asynchronous handler to be called once the listener
   *        handler has been registered on the event bus.
   * @return The hook listener.
   */
  public EventBusHookListener start(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.registerHandler(address, messageHandler, doneHandler);
    return this;
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
    this.startHandler = startHandler;
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
    this.receiveHandler = receiveHandler;
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
  public EventBusHookListener sendHandler(final Handler sendHandler) {
    this.sendHandler = sendHandler;
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
    this.stopHandler = stopHandler;
    return this;
  }

}
