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

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.messaging.DefaultJsonMessage;
import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * A default output collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputCollector implements OutputCollector {
  private final Vertx vertx;
  private final Logger logger;
  private final EventBus eventBus;
  private final InstanceContext context;
  private final List<String> auditors;
  private Handler<String> ackHandler;
  private Handler<String> failHandler;
  private Random random = new Random();
  private Map<String, Channel> channels = new HashMap<>();
  private Map<String, Long> connectionTimers = new HashMap<>();
  private static final long LISTEN_INTERVAL = 15000;

  public DefaultOutputCollector(Vertx vertx, Container container, InstanceContext context) {
    this(vertx, container, vertx.eventBus(), context);
  }

  public DefaultOutputCollector(Vertx vertx, Container container, EventBus eventBus, InstanceContext context) {
    this.vertx = vertx;
    this.logger = container.logger();
    this.eventBus = eventBus;
    this.context = context;
    auditors = context.getComponent().getNetwork().getAuditors();
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

  private Handler<Message<JsonObject>> ackerHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body != null) {
        String action = body.getString("action");
        switch (action) {
          case "ack":
            doAck(message);
            break;
          case "fail":
            doFail(message);
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
        channel = new DefaultChannel(output.getSelector(), output.getConditions(), eventBus);
        channels.put(grouping, channel);
      }
      else {
        channel = channels.get(grouping);
      }

      if (!channel.containsConnection(address)) {
        channel.addConnection(new DefaultConnection(address, eventBus));
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
      logger.error(e);
    }
  }

  @Override
  public String getAddress() {
    return context.getComponent().getAddress();
  }

  @Override
  public OutputCollector ackHandler(Handler<String> handler) {
    this.ackHandler = handler;
    return this;
  }

  private void doAck(Message<JsonObject> message) {
    if (ackHandler != null) {
      String id = message.body().getString("id");
      if (id != null) {
        ackHandler.handle(id);
      }
    }
  }

  @Override
  public OutputCollector failHandler(Handler<String> handler) {
    this.failHandler = handler;
    return this;
  }

  private void doFail(Message<JsonObject> message) {
    if (failHandler != null) {
      String id = message.body().getString("id");
      if (id != null) {
        failHandler.handle(id);
      }
    }
  }

  @Override
  public String emit(JsonObject body) {
    return emitNew(DefaultJsonMessage.create(context.getComponent().getAddress(), body, selectRandomAuditor()));
  }

  @Override
  public String emit(JsonObject body, String tag) {
    return emitNew(DefaultJsonMessage.create(context.getComponent().getAddress(), body, tag, selectRandomAuditor()));
  }

  @Override
  public String emit(JsonObject body, JsonMessage parent) {
    return emitChild(parent.createChild(body));
  }

  @Override
  public String emit(JsonObject body, String tag, JsonMessage parent) {
    return emitChild(parent.createChild(body, tag));
  }

  /**
   * Emits a new message.
   */
  private String emitNew(JsonMessage message) {
    eventBus.send(message.auditor(), new JsonObject().putString("action", "create").putString("id", message.id()));
    for (Channel channel : channels.values()) {
      channel.publish(message.createChild());
    }
    return message.id();
  }

  /**
   * Emits a child message.
   */
  private String emitChild(JsonMessage message) {
    for (Channel channel : channels.values()) {
      channel.publish(message.copy());
    }
    return message.parent();
  }

  /**
   * Returns a random auditor address.
   */
  private String selectRandomAuditor() {
    return auditors.get(random.nextInt(auditors.size()));
  }

  @Override
  public OutputCollector start() {
    eventBus.registerHandler(context.getComponent().getAddress(), handler);
    eventBus.registerHandler(context.getComponent().getNetwork().getBroadcastAddress(), ackerHandler);
    return this;
  }

  @Override
  public OutputCollector start(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    eventBus.registerHandler(context.getComponent().getAddress(), handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          eventBus.registerHandler(context.getComponent().getNetwork().getBroadcastAddress(), ackerHandler, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                future.setFailure(result.cause());
              }
              else {
                future.setResult(null);
              }
            }
          });
        }
      }
    });
    return this;
  }

  @Override
  public void stop() {
    eventBus.unregisterHandler(context.getComponent().getAddress(), handler);
    eventBus.unregisterHandler(context.getComponent().getNetwork().getBroadcastAddress(), ackerHandler);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    eventBus.unregisterHandler(context.getComponent().getAddress(), handler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          eventBus.unregisterHandler(context.getComponent().getNetwork().getBroadcastAddress(), ackerHandler, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                future.setFailure(result.cause());
              }
              else {
                future.setResult(null);
              }
            }
          });
        }
      }
    });
  }

}
