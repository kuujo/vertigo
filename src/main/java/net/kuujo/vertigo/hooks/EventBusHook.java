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
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

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
  private InstanceContext context;
  private EventBus eventBus;
  private String address;

  @Override
  public void handleStart(Component<?> component) {
    this.eventBus = component.getVertx().eventBus();
    this.context = component.getContext();
    this.address = component.getContext().getComponent().getAddress();
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "start").putObject("context", context.getState()));
  }

  @Override
  public void handleReceive(MessageId messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "receive").putObject("id", messageId.toJson()));
  }

  @Override
  public void handleAck(MessageId messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "ack").putObject("id", messageId.toJson()));
  }

  @Override
  public void handleFail(MessageId messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "fail").putObject("id", messageId.toJson()));
  }

  @Override
  public void handleEmit(MessageId messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "emit").putObject("id", messageId.toJson()));
  }

  @Override
  public void handleAcked(MessageId messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "acked").putObject("id", messageId.toJson()));
  }

  @Override
  public void handleFailed(MessageId messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "failed").putObject("id", messageId.toJson()));
  }

  @Override
  public void handleTimeout(MessageId messageId) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "timeout").putObject("id", messageId.toJson()));
  }

  @Override
  public void handleStop(Component<?> subject) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "stop").putObject("context", context.getState()));
  }

}
