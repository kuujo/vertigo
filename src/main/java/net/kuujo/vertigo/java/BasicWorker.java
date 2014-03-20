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
import net.kuujo.vertigo.component.impl.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.worker.Worker;

/**
 * A Java worker verticle.
 * <p>
 * 
 * To implement a worker using this class, override the
 * {@link #handleMessage(JsonMessage, Worker)} method. Each time the worker receives a
 * message, this method will be called.
 * <p>
 * 
 * <pre>
 * public class MyWorker extends WorkerVerticle {
 *   protected void handleMessage(JsonMessage message, Worker worker) {
 *     int num1 = message.body().getInteger(&quot;num1&quot;);
 *     int num2 = message.body().getInteger(&quot;num2&quot;);
 *     int total = num1 + num2;
 *     worker.emit(new JsonObject().putNumber(&quot;total&quot;, total), message);
 *     worker.ack(message);
 *   }
 * }
 * </pre>
 * 
 * @author Jordan Halterman
 */
public abstract class BasicWorker extends ComponentVerticle<Worker> {
  protected Worker worker;

  @Override
  protected Worker createComponent(InstanceContext context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createComponent(net.kuujo.vertigo.worker.impl.BasicWorker.class, context);
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
   * @param message The message that was received.
   */
  protected abstract void handleMessage(JsonMessage message, Worker worker);

}
