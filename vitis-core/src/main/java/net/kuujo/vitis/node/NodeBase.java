package net.kuujo.vitis.node;

import java.util.Collection;
import java.util.Iterator;

import net.kuujo.via.heartbeat.DefaultHeartbeatEmitter;
import net.kuujo.via.heartbeat.HeartbeatEmitter;
import net.kuujo.vitis.VitisException;
import net.kuujo.vitis.context.ConnectionContext;
import net.kuujo.vitis.context.NetworkContext;
import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.dispatcher.Dispatcher;
import net.kuujo.vitis.messaging.ConnectionPool;
import net.kuujo.vitis.messaging.DefaultJsonMessage;
import net.kuujo.vitis.messaging.BasicChannel;
import net.kuujo.vitis.messaging.EventBusConnection;
import net.kuujo.vitis.messaging.ConnectionSet;
import net.kuujo.vitis.messaging.JsonMessage;

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
 * An abstract worker.
 *
 * @author Jordan Halterman
 */
public abstract class NodeBase implements Node {

  protected Vertx vertx;

  protected EventBus eventBus;

  protected Container container;

  protected Logger logger;

  protected WorkerContext context;

  protected String address;

  protected String networkAddress;

  protected String auditAddress;

  protected String broadcastAddress;

  protected HeartbeatEmitter heartbeat;

  protected OutputCollector output;

  protected NodeBase(Vertx vertx, Container container, WorkerContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.container = container;
    this.logger = container.logger();
    this.context = context;
    this.address = context.address();
    NetworkContext networkContext = context.context().context();
    networkAddress = networkContext.address();
    auditAddress = networkContext.auditAddress();
    broadcastAddress = networkContext.broadcastAddress();
  }

  @Override
  public JsonObject config() {
    return context.config();
  }

  @Override
  public WorkerContext context() {
    return context;
  }

  /**
   * Sets up the heartbeat.
   */
  protected void setupHeartbeat() {
    setupHeartbeat(null);
  }

  /**
   * Sets up the heartbeat.
   */
  protected void setupHeartbeat(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    if (doneHandler != null) {
      future.setHandler(doneHandler);
    }

    eventBus.sendWithTimeout(networkAddress, new JsonObject().putString("action", "register").putString("address", address), 10000, new Handler<AsyncResult<Message<String>>>() {
      @Override
      public void handle(AsyncResult<Message<String>> result) {
        if (result.succeeded()) {
          String heartbeatAddress = result.result().body();
          heartbeat = new DefaultHeartbeatEmitter(heartbeatAddress, vertx);
          heartbeat.setInterval(context.context().definition().heartbeatInterval());
          heartbeat.start();
          future.setResult(null);
        }
        else {
          future.setFailure(new VitisException(String.format("Failed to fetch heartbeat address from network.")));
        }
      }
    });
  }

  /**
   * Sets up outputs.
   */
  protected void setupOutputs() {
    setupOutputs(null);
  }

  /**
   * Sets up outputs.
   */
  protected void setupOutputs(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    if (doneHandler != null) {
      future.setHandler(doneHandler);
    }

    output = new LinearOutputCollector(auditAddress, eventBus);

    Collection<ConnectionContext> connections = context.context().connectionContexts();
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
        ConnectionPool connectionPool = new ConnectionSet();
        String[] addresses = connectionContext.getAddresses();
        for (String address : addresses) {
          connectionPool.add(new EventBusConnection(address, eventBus));
        }

        // Initialize the dispatcher and add a channel to the channels list.
        dispatcher.init(connectionPool);
        output.addChannel(new BasicChannel(dispatcher));
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        container.logger().error("Failed to find grouping handler.");
      }
    }
    future.setResult(null);
  }

  /**
   * Sets up inputs.
   */
  protected void setupInputs() {
    setupInputs(null);
  }

  /**
   * Sets up input handlers.
   */
  protected void setupInputs(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    if (doneHandler != null) {
      future.setHandler(doneHandler);
    }

    eventBus.registerHandler(address, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          doReceive(new DefaultJsonMessage(body));
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          eventBus.registerHandler(broadcastAddress, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
              JsonObject body = message.body();
              if (body != null) {
                String action = body.getString("action");
                if (action != null) {
                  switch (action) {
                    case "ack":
                      String ackId = body.getString("id");
                      if (ackId != null) {
                        doAck(ackId);
                      }
                      break;
                    case "fail":
                      String failId = body.getString("id");
                      if (failId != null) {
                        doFail(failId);
                      }
                      break;
                  }
                }
              }
            }
          }, new Handler<AsyncResult<Void>>() {
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

  /**
   * Indicates to the network that the node is ready.
   */
  protected void ready() {
    ready(null);
  }

  /**
   * Indicates to the network that the node is ready.
   */
  protected void ready(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    if (doneHandler != null) {
      future.setHandler(doneHandler);
    }

    eventBus.send(networkAddress, new JsonObject().putString("action", "ready").putString("address", address), new Handler<Message<Void>>() {
      @Override
      public void handle(Message<Void> message) {
        future.setResult(null);
      }
    });
  }

  /**
   * Called when a message is acked.
   */
  protected void doAck(String id) {
  }

  /**
   * Called when a message is failed.
   */
  protected void doFail(String id) {
  }

  /**
   * Called when a message is received.
   */
  protected void doReceive(JsonMessage message) {
  }

}
