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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.messaging.DefaultJsonMessage;
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
  private Vertx vertx;
  private EventBus eventBus;
  private ComponentContext context;
  private List<String> auditors;
  private Random random;
  private Map<String, Channel> channels = new HashMap<>();
  private Map<String, Long> connectionTimers = new HashMap<>();
  private static final long LISTEN_INTERVAL = 15000;

  public DefaultOutputCollector(Vertx vertx, ComponentContext context) {
    this(vertx, vertx.eventBus(), context);
  }

  public DefaultOutputCollector(Vertx vertx, EventBus eventBus, ComponentContext context) {
    this.vertx = vertx;
    this.eventBus = eventBus;
    this.context = context;
    auditors = context.getNetwork().getAuditors();
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
    final String address = info.getString("address");
    if (address == null) {
      return;
    }

    try {
      Output output = Serializer.deserialize(info);
      String grouping = output.getSelector().getGrouping();
      if (grouping == null) {
        grouping = UUID.randomUUID().toString();
      }

      final Channel channel;
      if (!channels.containsKey(grouping)) {
        channel = new DefaultChannel(output.getSelector(), output.getConditions());
        channels.put(grouping, channel);
      }
      else {
        channel = channels.get(grouping);
      }

      if (!channel.containsConnection(address)) {
        channel.addConnection(new EventBusConnection(address, eventBus));
      }

      if (connectionTimers.containsKey(address)) {
        vertx.cancelTimer(connectionTimers.remove(address));
      }

      // Set a timer that, if triggered, will remove the connection from the channel.
      // This indicates that we haven't received a keep-alive message in LISTEN_INTERVAL.
      connectionTimers.put(address, vertx.setTimer(LISTEN_INTERVAL, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          if (channel.containsConnection(address)) {
            channel.removeConnection(channel.getConnection(address));
          }
          connectionTimers.remove(address);
        }
      }));
    }
    catch (SerializationException e) {
      // Failed to unserialize the input info.
    }
  }

  @Override
  public String getAddress() {
    return context.getAddress();
  }

  @Override
  public OutputCollector emit(JsonObject body) {
    return doEmit(DefaultJsonMessage.create(context.getAddress(), body, selectRandomAuditor()));
  }

  @Override
  public OutputCollector emit(JsonObject body, String tag) {
    return doEmit(DefaultJsonMessage.create(context.getAddress(), body, tag, selectRandomAuditor()));
  }

  @Override
  public OutputCollector emit(JsonObject body, JsonMessage parent) {
    return doEmit(parent.createChild(body));
  }

  @Override
  public OutputCollector emit(JsonObject body, String tag, JsonMessage parent) {
    return doEmit(parent.createChild(body, tag));
  }

  /**
   * Emits a message.
   */
  private OutputCollector doEmit(JsonMessage message) {
    for (Channel channel : channels.values()) {
      channel.publish(message);
    }
    return this;
  }

  /**
   * Returns a random auditor address.
   */
  private String selectRandomAuditor() {
    return auditors.get(random.nextInt(auditors.size()));
  }

  @Override
  public void start() {
    eventBus.registerHandler(context.getAddress(), handler);
  }

  @Override
  public void start(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.registerHandler(context.getAddress(), handler, doneHandler);
  }

  @Override
  public void stop() {
    eventBus.unregisterHandler(context.getAddress(), handler);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.unregisterHandler(context.getAddress(), handler, doneHandler);
  }

}
