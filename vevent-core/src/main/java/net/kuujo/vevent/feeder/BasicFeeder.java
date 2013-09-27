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
package net.kuujo.vevent.feeder;

import java.util.Collection;
import java.util.Iterator;

import net.kuujo.vevent.context.ConnectionContext;
import net.kuujo.vevent.context.WorkerContext;
import net.kuujo.vevent.eventbus.ReliableEventBus;
import net.kuujo.vevent.eventbus.WrappedReliableEventBus;
import net.kuujo.vevent.messaging.ConnectionPool;
import net.kuujo.vevent.messaging.CoordinatingOutputCollector;
import net.kuujo.vevent.messaging.DefaultJsonMessage;
import net.kuujo.vevent.messaging.Dispatcher;
import net.kuujo.vevent.messaging.EventBusChannel;
import net.kuujo.vevent.messaging.EventBusConnection;
import net.kuujo.vevent.messaging.EventBusConnectionPool;
import net.kuujo.vevent.messaging.JsonMessage;
import net.kuujo.vevent.messaging.OutputCollector;
import net.kuujo.vevent.node.FailureException;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A basic feeder.
 *
 * @author Jordan Halterman
 */
public class BasicFeeder implements Feeder {

  protected Vertx vertx;

  protected Container container;

  protected ReliableEventBus eventBus;

  protected WorkerContext context;

  protected OutputCollector output;

  protected long enqueued;

  protected long maxQueueSize = 1000;

  protected boolean paused;

  private boolean rescheduled;

  protected Handler<Feeder> feedHandler;

  public BasicFeeder(Vertx vertx, Container container, WorkerContext context) {
    this.vertx = vertx;
    this.eventBus = new WrappedReliableEventBus(vertx.eventBus(), vertx);
    this.container = container;
    this.context = context;
  }

  @Override
  public Feeder setMaxQueueSize(long maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  @Override
  public void start() {
    setupOutputs();
    vertx.setTimer(1000, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        recursiveFeed();
      }
    });
  }

  private void recursiveFeed() {
    rescheduled = false;
    while (!paused && !rescheduled) {
      doFeed();
    }
  }

  private void doFeed() {
    if (feedHandler != null) {
      feedHandler.handle(this);
    }
    else {
      rescheduled = true;
    }
  }

  protected void scheduleFeed() {
    vertx.setTimer(500, new Handler<Long>() {
      @Override
      public void handle(Long id) {
        recursiveFeed();
      }
    });
    rescheduled = true;
  }

  /**
   * Sets up feeder outputs.
   */
  private void setupOutputs() {
    output = new CoordinatingOutputCollector();

    Collection<ConnectionContext> connections = context.getContext().getConnectionContexts();
    Iterator<ConnectionContext> iter = connections.iterator();
    while (iter.hasNext()) {
      ConnectionContext connectionContext = iter.next();
      try {
        JsonObject grouping = connectionContext.getGrouping();
        Dispatcher dispatcher = (Dispatcher) Class.forName(grouping.getString("dispatcher")).newInstance();

        // Set options on the dispatcher. All non-"dispatcher" values
        // are considered to be dispatcher options.
        Iterator<String> fieldNames = grouping.getFieldNames().iterator();
        while (fieldNames.hasNext()) {
          String fieldName = fieldNames.next();
          if (fieldName != "dispatcher") {
            String value = grouping.getString(fieldName);
            dispatcher.setOption(fieldName, value);
          }
        }

        // Create a connection pool from which the dispatcher will dispatch messages.
        ConnectionPool<EventBusConnection> connectionPool = new EventBusConnectionPool();
        String[] addresses = connectionContext.getAddresses();
        for (String address : addresses) {
          connectionPool.add(new EventBusConnection(address, eventBus));
        }

        // Initialize the dispatcher and add a channel to the channels list.
        dispatcher.init(connectionPool);
        output.addChannel(connectionContext.getNodeName(), new EventBusChannel(dispatcher));
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        container.logger().error("Failed to find grouping handler.");
      }
    }
  }

  @Override
  public Feeder feedHandler(Handler<Feeder> handler) {
    this.feedHandler = handler;
    rescheduled = false;
    return this;
  }

  @Override
  public Feeder feed(JsonObject data) {
    output.emit(DefaultJsonMessage.create(data));
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler) {
    return feed(data, 0, doneHandler);
  }

  @Override
  public Feeder feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    doEmit(DefaultJsonMessage.create(data), timeout, doneHandler);
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, String tag) {
    output.emit(DefaultJsonMessage.create(data, tag));
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> doneHandler) {
    return feed(data, tag, 0, doneHandler);
  }

  @Override
  public Feeder feed(JsonObject data, String tag, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    doEmit(DefaultJsonMessage.create(data, tag), timeout, doneHandler);
    return this;
  }

  private void doEmit(JsonMessage message, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    output.emit(message, timeout, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        enqueued--;
        if (enqueued < maxQueueSize) {
          paused = false;
        }
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else if (!result.result()) {
          future.setFailure(new FailureException("Message was failed."));
        }
        else {
          future.setResult(null);
        }
      }
    });
    enqueued++;
    if (enqueued >= maxQueueSize) {
      paused = true;
    }
  }

}
