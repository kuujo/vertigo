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

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.component.impl.DefaultInstanceContext;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Event bus based hook implementation.<p>
 *
 * This hook publishes events to the event bus. Messages are published to the
 * given address. If no address is provided then messages will be published
 * to the internal address of the component to which the hook is attached.<p>
 *
 * Messages are formatted as {@link JsonObject} instances. Each message will have
 * an <code>event</code> key which indicates the event that occurred. Additional
 * keys depend on the event.<p>
 *
 * You can use an {@link EventBusHookListener} to listen for messages from an
 * <code>EventBusHook</code>. This listener will handle parsing messages and
 * calling event handlers.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class EventBusHook implements ComponentHook {
  @JsonIgnore private InstanceContext context;
  @JsonIgnore private EventBus eventBus;
  private String address;

  public EventBusHook() {
  }

  public EventBusHook(String address) {
    this.address = address;
  }

  @Override
  public void handleStart(Component component) {
    this.eventBus = component.vertx().eventBus();
    this.context = component.context();
    if (this.address == null) {
      this.address = component.context().component().address();
    }
    eventBus.publish(address, new JsonObject().putString("event", "start").putObject("context", DefaultInstanceContext.toJson(context)));
  }

  @Override
  public void handleSend(Object message) {
    eventBus.publish(address, new JsonObject().putString("event", "send").putValue("message", message));
  }

  @Override
  public void handleReceive(Object message) {
    eventBus.publish(address, new JsonObject().putString("event", "receive").putValue("message", message));
  }

  @Override
  public void handleStop(Component subject) {
    eventBus.publish(address, new JsonObject().putString("event", "stop").putObject("context", DefaultInstanceContext.toJson(context)));
  }

}
