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
package net.kuujo.vertigo.java;

import org.vertx.java.core.Handler;

import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.worker.Worker;

/**
 * A Java worker verticle.
 *
 * @author Jordan Halterman
 */
public abstract class WorkerVerticle extends ComponentVerticle<Worker> {
  protected Worker worker;

  @Override
  protected Worker createComponent(InstanceContext<Worker> context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createWorker(context);
  }

  @Override
  protected void start(final Worker worker) {
    this.worker = worker;
    worker.messageHandler(new Handler<JsonMessage>() {
      @Override
      public void handle(JsonMessage message) {
        handleMessage(message, worker);
      }
    });
  }

  /**
   * Called when a new message is received by the worker.
   *
   * @param message
   *   The message that was received.
   */
  protected abstract void handleMessage(JsonMessage message, Worker worker);

}
