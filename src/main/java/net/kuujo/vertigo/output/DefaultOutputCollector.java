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
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.JsonMessageBuilder;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.monitor.acker.Acker;
import net.kuujo.vertigo.monitor.acker.DefaultAcker;
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
  private final Serializer serializer = Serializer.getInstance();
  private final Vertx vertx;
  private final Logger logger;
  private final EventBus eventBus;
  private final InstanceContext context;
  private final Acker acker;
  private final boolean ackingEnabled;
  private final String componentAddress;
  private final List<OutputHook> hooks = new ArrayList<>();
  private final List<String> auditors;
  private final JsonMessageBuilder messageBuilder;
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
    acker = new DefaultAcker(context.id(), eventBus);
    messageBuilder = new JsonMessageBuilder(context.id());
    ackingEnabled = context.getComponent().getNetwork().isAckingEnabled();
    auditors = context.getComponent().getNetwork().getAuditors();
    componentAddress = context.getComponent().getAddress();
  }

  public DefaultOutputCollector(Vertx vertx, Container container, InstanceContext context, Acker acker) {
    this(vertx, container, vertx.eventBus(), context, acker);
  }

  public DefaultOutputCollector(Vertx vertx, Container container, EventBus eventBus, InstanceContext context, Acker acker) {
    this.vertx = vertx;
    this.logger = container.logger();
    this.eventBus = eventBus;
    this.context = context;
    this.acker = acker;
    messageBuilder = new JsonMessageBuilder(context.id());
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
            doListen(body.getString("address"), body.getString("status"), body.getObject("input"));
            break;
        }
      }
    }
  };

  /**
   * Starts listening to messages from this output collector.
   */
  private void doListen(final String address, final String statusAddress, final JsonObject info) {
    if (address == null || statusAddress == null) {
      return;
    }

    try {
      Input input = serializer.deserialize(info, Input.class);
      Output output = new Output(input.id(), input.getCount(), input.getGrouping().createSelector());

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
        eventBus, messageBuilder).setConnectionCount(output.getCount());
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
  private void hookAcked(final MessageId messageId) {
    for (OutputHook hook : hooks) {
      hook.handleAcked(messageId);
    }
  }

  /**
   * Calls failed hooks.
   */
  private void hookFailed(final MessageId messageId) {
    for (OutputHook hook : hooks) {
      hook.handleFailed(messageId);
    }
  }

  /**
   * Calls timed-out hooks.
   */
  private void hookTimeout(final MessageId messageId) {
    for (OutputHook hook : hooks) {
      hook.handleTimeout(messageId);
    }
  }

  /**
   * Calls emit hooks.
   */
  private void hookEmit(final MessageId messageId) {
    for (OutputHook hook : hooks) {
      hook.handleEmit(messageId);
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
  public OutputCollector ackHandler(Handler<MessageId> handler) {
    acker.ackHandler(createAckHandler(handler));
    return this;
  }

  private Handler<MessageId> createAckHandler(final Handler<MessageId> handler) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        handler.handle(messageId);
        hookAcked(messageId);
      }
    };
  }

  @Override
  public OutputCollector failHandler(Handler<MessageId> handler) {
    acker.failHandler(createFailHandler(handler));
    return this;
  }

  private Handler<MessageId> createFailHandler(final Handler<MessageId> handler) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        handler.handle(messageId);
        hookFailed(messageId);
      }
    };
  }

  @Override
  public OutputCollector timeoutHandler(Handler<MessageId> handler) {
    acker.timeoutHandler(createTimeoutHandler(handler));
    return this;
  }

  private Handler<MessageId> createTimeoutHandler(final Handler<MessageId> handler) {
    return new Handler<MessageId>() {
      @Override
      public void handle(MessageId messageId) {
        handler.handle(messageId);
        hookTimeout(messageId);
      }
    };
  }

  @Override
  public MessageId emit(JsonObject body) {
    JsonMessage message = messageBuilder.createNew(selectRandomAuditor()).toMessage();
    MessageId messageId = message.messageId();
    JsonMessage child = messageBuilder.createChild(message).setBody(body)
        .setSource(componentAddress).toMessage();
    for (Channel channel : channels) {
      acker.fork(messageId, channel.publish(child));
    }
    acker.create(messageId);
    hookEmit(messageId);
    return messageId;
  }

  @Override
  public MessageId emit(JsonObject body, String tag) {
    JsonMessage message = messageBuilder.createNew(selectRandomAuditor()).toMessage();
    MessageId messageId = message.messageId();
    JsonMessage child = messageBuilder.createChild(message).setBody(body)
        .setTag(tag).setSource(componentAddress).toMessage();
    for (Channel channel : channels) {
      acker.fork(messageId, channel.publish(child));
    }
    acker.create(messageId);
    hookEmit(messageId);
    return messageId;
  }

  @Override
  public MessageId emit(JsonObject body, JsonMessage parent) {
    JsonMessage message = messageBuilder.createChild(parent).toMessage();
    MessageId messageId = message.messageId();
    JsonMessage child = messageBuilder.createChild(message).setBody(body).toMessage();
    for (Channel channel : channels) {
      acker.fork(parent.messageId(), channel.publish(child));
    }
    hookEmit(messageId);
    return messageId;
  }

  @Override
  public MessageId emit(JsonObject body, String tag, JsonMessage parent) {
    JsonMessage message = messageBuilder.createChild(parent).toMessage();
    MessageId messageId = message.messageId();
    JsonMessage child = messageBuilder.createChild(message)
        .setBody(body).setTag(tag).toMessage();
    for (Channel channel : channels) {
      acker.fork(parent.messageId(), channel.publish(child));
    }
    hookEmit(messageId);
    return messageId;
  }

  /**
   * Returns a random auditor address.
   */
  private String selectRandomAuditor() {
    // If acking is not enabled then don't assign any acker to the message.
    if (ackingEnabled) {
      return auditors.get(random.nextInt(auditors.size()));
    }
    return null;
  }

  @Override
  public OutputCollector start() {
    eventBus.registerHandler(context.getComponent().getAddress(), handler);
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
          future.setResult(null);
          hookStart();
        }
      }
    });
    return this;
  }

  @Override
  public void stop() {
    eventBus.unregisterHandler(context.getComponent().getAddress(), handler);
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
          future.setResult(null);
          hookStop();
        }
      }
    });
  }

}
