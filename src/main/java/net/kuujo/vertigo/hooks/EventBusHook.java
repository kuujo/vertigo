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

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.impl.DefaultInstanceContext;

import org.vertx.java.core.eventbus.EventBus;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An event bus publishing hook.
 *
 * This hook publishes events to the event bus. Messages are published using the
 * string format "vertigo.hooks.%s" where the string argument is the full
 * component address. If the method argument is a component, the component
 * context will be provided. If the method argument is a string ID, the ID
 * will be provided.
 *
 * @author Jordan Halterman
 */
public class EventBusHook implements ComponentHook {
  @JsonIgnore private InstanceContext context;
  @JsonIgnore private EventBus eventBus;
  @JsonIgnore private String address;

  @Override
  public void handleStart(Component component) {
    this.eventBus = component.vertx().eventBus();
    this.context = component.context();
    this.address = component.context().component().address();
    eventBus.publish(String.format("vertigo.hooks.%s.start", address), DefaultInstanceContext.toJson(context));
  }

  @Override
  public void handleReceive(String port, String messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s.%s.receive", address, port), messageId);
  }

  @Override
  public void handleSend(String port, String messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s.%s.send", address, port), messageId);
  }

  @Override
  public void handleStop(Component subject) {
    eventBus.publish(String.format("vertigo.hooks.%s.stop", address), DefaultInstanceContext.toJson(context));
  }

}
