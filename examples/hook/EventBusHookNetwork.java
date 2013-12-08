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
package net.kuujo.vertigo;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.hooks.EventBusHook;
import net.kuujo.vertigo.hooks.EventBusHookListener;
import net.kuujo.vertigo.java.RichFeederVerticle;
import net.kuujo.vertigo.java.RichWorkerVerticle;
import net.kuujo.vertigo.java.VertigoVerticle;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Network;

/**
 * A component hook example.
 *
 * This example demonstrates how hooks can be used to listen for events on
 * Vertigo components. In this case, we use the built-in EventBusHook to publish
 * component events over the event bus.
 *
 * @author Jordan Halterman
 */
public class EventBusHookNetwork extends VertigoVerticle {

  public static class ExampleFeeder extends RichFeederVerticle {
    @Override
    protected void nextMessage() {
      emit(new JsonObject().putString("body", "Hello world!"));
    }
  }

  public static class ExampleWorker extends RichWorkerVerticle {
    @Override
    protected void handleMessage(JsonMessage message) {
      emit(message);
      ack(message);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void start() {
    // Set up the network.
    Network network = vertigo.createNetwork("hooks");
    network.addFeeder("hooks.feeder", ExampleFeeder.class.getName());
    network.addWorker("hooks.worker", ExampleWorker.class.getName())
      .addHook(new EventBusHook()).addInput("hooks.feeder");

    // Deploy the network.
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.succeeded()) {
          // Create an event bus hook listener. This special class will handle
          // event bus handlers and deserialization of component events.
          EventBusHookListener listener = new EventBusHookListener("hooks.worker", vertx.eventBus());
          listener.receiveHandler(new Handler<MessageId>() {
            @Override
            public void handle(MessageId messageId) {
              container.logger().info("Worker received message " + messageId.correlationId());
            }
          });
        }
      }
    });
  }

}
