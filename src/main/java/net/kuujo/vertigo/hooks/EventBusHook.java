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
  public void handleReceive(String id) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "receive").putString("id", id));
  }

  @Override
  public void handleAck(String id) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "ack").putString("id", id));
  }

  @Override
  public void handleFail(String id) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "fail").putString("id", id));
  }

  @Override
  public void handleEmit(String id) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "emit").putString("id", id));
  }

  @Override
  public void handleAcked(String id) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "acked").putString("id", id));
  }

  @Override
  public void handleFailed(String id) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "failed").putString("id", id));
  }

  @Override
  public void handleTimeout(String id) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "timeout").putString("id", id));
  }

  @Override
  public void handleStop(Component<?> subject) {
    eventBus.publish(String.format("vertigo.hooks.%s", address),
        new JsonObject().putString("event", "stop").putObject("context", context.getState()));
  }

}
