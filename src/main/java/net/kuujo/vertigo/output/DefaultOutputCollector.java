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
package net.kuujo.vertigo.output;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * A default output collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputCollector implements OutputCollector {

  private String address;
  private Vertx vertx;
  private EventBus eventBus;
  private Map<String, Channel> channels = new HashMap<>();

  public DefaultOutputCollector(String address, Vertx vertx) {
    this.address = address;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultOutputCollector(String address, Vertx vertx, EventBus eventBus) {
    this.address = address;
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  private Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body != null) {
        String action = body.getString("action");
        switch (action) {
          case "listen":
            doListen(body);
            break;
        }
      }
    }
  };

  /**
   * Starts listening to messages from this output collector.
   */
  private void doListen(JsonObject info) {
    String address = info.getString("address");
    try {
      Output output = Output.fromInput((Input) Serializer.deserialize(info));
      String group = output.getGroup();
      Channel channel;
      if (!channels.containsKey(group)) {
        channel = new DefaultChannel(output);
        channels.put(group, channel);
      }
      else {
        channel = channels.get(group);
      }

      if (!channel.containsConnection(address)) {
        channel.addConnection(new EventBusConnection(address, eventBus));
      }
    }
    catch (SerializationException e) {
      // Failed to unserialize the input info.
    }
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public OutputCollector emit(JsonObject data) {
    return this;
  }

  @Override
  public OutputCollector emit(JsonObject data, String tag) {
    return this;
  }

  @Override
  public OutputCollector emit(JsonObject data, JsonMessage parent) {
    return this;
  }

  @Override
  public OutputCollector emit(JsonObject data, String tag, JsonMessage parent) {
    return this;
  }

  @Override
  public void start() {
    
  }

  @Override
  public void start(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.registerHandler(address, handler, doneHandler);
  }

  @Override
  public void stop() {
    eventBus.unregisterHandler(address, handler);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.unregisterHandler(address, handler, doneHandler);
  }

}
