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

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.InstanceContext;

/**
 * This hook integrates directly with the mod-metrics module by
 * Tim Yates. To add the hook to a component, simply instantiate
 * the hook with the event bus address of the mod-metrics module.<p>
 *
 * <pre>
 * network.addWorker("foo.bar", FooBar.class.getName()).addHook(new MetricsHook("com.bloidonia.metrics"));
 * </pre>
 *
 * @author Jordan Halterman
 */
public class MetricsHook implements ComponentHook {
  @JsonIgnore private InstanceContext context;
  @JsonIgnore private EventBus eventBus;
  private String address;

  public MetricsHook() {
  }

  public MetricsHook(String address) {
    this.address = address;
  }

  @Override
  public void handleStart(Component<?> component) {
    context = component.context();
    eventBus = component.vertx().eventBus();
  }

  @Override
  public void handleReceive(String messageId) {
    eventBus.send(address, new JsonObject()
      .putString("action", "mark")
      .putString("name", String.format("%s.receive", context.address())));
  }

  @Override
  public void handleAck(String messageId) {
    eventBus.send(address, new JsonObject()
      .putString("action", "mark")
      .putString("name", String.format("%s.ack", context.address())));
  }

  @Override
  public void handleFail(String messageId) {
    eventBus.send(address, new JsonObject()
      .putString("action", "mark")
      .putString("name", String.format("%s.fail", context.address())));
  }

  @Override
  public void handleEmit(String messageId) {
    eventBus.send(address, new JsonObject()
      .putString("action", "mark")
      .putString("name", String.format("%s.emit", context.address())));
  }

  @Override
  public void handleAcked(String messageId) {
    eventBus.send(address, new JsonObject()
      .putString("action", "mark")
      .putString("name", String.format("%s.acked", context.address())));
  }

  @Override
  public void handleFailed(String messageId) {
    eventBus.send(address, new JsonObject()
      .putString("action", "mark")
      .putString("name", String.format("%s.failed", context.address())));
  }

  @Override
  public void handleTimeout(String messageId) {
    eventBus.send(address, new JsonObject()
      .putString("action", "mark")
      .putString("name", String.format("%s.timeout", context.address())));
  }

  @Override
  public void handleStop(Component<?> component) {
    
  }

}
