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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.JsonMessageBuilder;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
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
  private final boolean ackingEnabled;
  private final String componentAddress;
  private final List<OutputHook> hooks = new ArrayList<>();
  private final List<String> auditors;
  private Handler<String> ackHandler;
  private Handler<String> failHandler;
  private Handler<String> timeoutHandler;
  private Random random = new Random();
  private List<Channel> channels = new ArrayList<>();
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
    ackingEnabled = context.getComponent().getNetwork().isAckingEnabled();
    auditors = context.getComponent().getNetwork().getAuditors();
    componentAddress = context.getComponent().getAddress();
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
          case "timeout":
            doTimeout(message);
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
    final String statusAddress = info.getString("status");
    if (address == null || statusAddress == null) {
      return;
    }

    try {
      Output output = Serializer.deserialize(info);

      final Channel channel = findChannel(output);
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
            Connection connection = channel.getConnection(address);
            // if null it means the connection doesn't exist or it is already a PseudConnection
            // so we don't need to remove it again
            if (connection != null) {
              channel.removeConnection(connection);
            }
          connectionTimers.remove(address);
        }
      }));
      eventBus.send(statusAddress, new JsonObject().putString("id", context.id()));
    }
    catch (SerializationException e) {
      logger.error(e);
    }
  }

  /**
   * Finds a channel by ID.
   */
  private Channel findChannel(Output output) {
    for (Channel channel : channels) {
      if (channel.id().equals(output.id())) {
        return channel;
      }
    }
    Channel channel = new DefaultChannel(output.id(), output.getSelector(),
        output.getConditions(), eventBus).setConnectionCount(output.getCount());
    channels.add(channel);
    return channel;
  }

  @Override
  public String getAddress() {
    return context.getComponent().getAddress();
  }

  @Override
  public OutputCollector addHook(OutputHook hook) {
    hooks.add(hook);
    return this;
  }

  /**
   * Calls start hooks.
   */
  private void hookStart() {
    for (OutputHook hook : hooks) {
      hook.handleStart(this);
    }
  }

  /**
   * Calls acked hooks.
   */
  private void hookAcked(final String id) {
    for (OutputHook hook : hooks) {
      hook.handleAcked(id);
    }
  }

  /**
   * Calls failed hooks.
   */
  private void hookFailed(final String id) {
    for (OutputHook hook : hooks) {
      hook.handleFailed(id);
    }
  }

  /**
   * Calls timed-out hooks.
   */
  private void hookTimeout(final String id) {
    for (OutputHook hook : hooks) {
      hook.handleTimeout(id);
    }
  }

  /**
   * Calls emit hooks.
   */
  private void hookEmit(final String id) {
    for (OutputHook hook : hooks) {
      hook.handleEmit(id);
    }
  }

  /**
   * Calls stop hooks.
   */
  private void hookStop() {
    for (OutputHook hook : hooks) {
      hook.handleStop(this);
    }
  }

  @Override
  public OutputCollector ackHandler(Handler<String> handler) {
    this.ackHandler = handler;
    return this;
  }

  /**
   * Receives an ack message.
   */
  private void doAck(Message<JsonObject> message) {
    if (ackHandler != null) {
      String id = message.body().getString("id");
      if (id != null) {
        ackHandler.handle(id);
        hookAcked(id);
      }
    }
  }

  @Override
  public OutputCollector failHandler(Handler<String> handler) {
    this.failHandler = handler;
    return this;
  }

  /**
   * Receives a fail message.
   */
  private void doFail(Message<JsonObject> message) {
    if (failHandler != null) {
      String id = message.body().getString("id");
      if (id != null) {
        failHandler.handle(id);
        hookFailed(id);
      }
    }
  }

  @Override
  public OutputCollector timeoutHandler(Handler<String> handler) {
    this.timeoutHandler = handler;
    return this;
  }

  /**
   * Receives a timeput message.
   */
  private void doTimeout(Message<JsonObject> message) {
    if (timeoutHandler != null) {
      String id = message.body().getString("id");
      if (id != null) {
        timeoutHandler.handle(id);
        hookTimeout(id);
      }
    }
  }

  @Override
  public String emit(JsonObject body) {
    return emitNew(JsonMessageBuilder.create(body).setSource(componentAddress)
        .setAuditor(selectRandomAuditor()).toMessage());
  }

  @Override
  public String emit(JsonObject body, String tag) {
    return emitNew(JsonMessageBuilder.create(body, tag).setSource(componentAddress)
        .setAuditor(selectRandomAuditor()).toMessage());
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
    final List<Object> ids = new ArrayList<>();
    for (Channel channel : channels) {
      ids.addAll(channel.publish(message.createChild()));
    }
    final String auditor = message.auditor();
    if (auditor != null) {
      eventBus.send(auditor, new JsonObject().putString("action", "create")
          .putString("id", message.id()).putArray("forks", new JsonArray(ids)));
    }
    hookEmit(message.id());
    return message.id();
  }

  /**
   * Emits a child message.
   */
  private String emitChild(JsonMessage message) {
    final List<Object> ids = new ArrayList<>();
    for (Channel channel : channels) {
      ids.addAll(channel.publish(message.copy()));
    }
    final String auditor = message.auditor();
    if (auditor != null) {
      eventBus.send(auditor, new JsonObject().putString("action", "fork")
          .putString("parent", message.parent()).putArray("forks", new JsonArray(ids)));
    }
    hookEmit(message.parent());
    return message.parent();
  }

  /**
   * Returns a random auditor address.
   */
  private String selectRandomAuditor() {
    // If acking is not enabled then don't assign any auditor to the message.
    if (ackingEnabled) {
      return auditors.get(random.nextInt(auditors.size()));
    }
    return null;
  }

  @Override
  public OutputCollector start() {
    eventBus.registerHandler(context.getComponent().getAddress(), handler);
    eventBus.registerHandler(context.getComponent().getNetwork().getBroadcastAddress(), ackerHandler);
    hookStart();
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
                hookStart();
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
    hookStop();
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
                hookStop();
              }
            }
          });
        }
      }
    });
  }

}
