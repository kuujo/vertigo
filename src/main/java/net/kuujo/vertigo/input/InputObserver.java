package net.kuujo.vertigo.input;

import java.util.UUID;

import net.kuujo.vertigo.messaging.DefaultJsonMessage;
import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * A basic input observer.
 *
 * @author Jordan Halterman
 */
public class InputObserver implements Observer {
  private String address;
  private Input input;
  private Vertx vertx;
  private EventBus eventBus;
  private long timerID;
  private static final long OBSERVE_INTERVAL = 5000;
  private Handler<JsonMessage> messageHandler;

  public InputObserver(Input input, Vertx vertx) {
    this.address = UUID.randomUUID().toString();
    this.input = input;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public InputObserver(Input input, Vertx vertx, EventBus eventBus) {
    this.address = UUID.randomUUID().toString();
    this.input = input;
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  @Override
  public Observer messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    return this;
  }

  private Handler<Message<JsonObject>> eventBusHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body != null) {
        String action = body.getString("action");
        if (action != null) {
          switch (action) {
            case "receive":
              doReceive(message);
              break;
          }
        }
      }
    }
  };

  /**
   * Handles receiving a message for processing.
   */
  private void doReceive(Message<JsonObject> message) {
    if (messageHandler != null) {
      messageHandler.handle(new DefaultJsonMessage(message.body()));
    }
  }

  @Override
  public Observer start() {
    eventBus.registerHandler(address, eventBusHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          periodicObserve();
        }
      }
    });
    return this;
  }

  @Override
  public Observer start(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    eventBus.registerHandler(address, eventBusHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          future.setResult(null);
          periodicObserve();
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
    return this;
  }

  @Override
  public void stop() {
    cancelObserve();
    eventBus.unregisterHandler(address, eventBusHandler);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.unregisterHandler(address, eventBusHandler, doneHandler);
  }

  /**
   * Sends periodic observe message to the observed address.
   */
  private void periodicObserve() {
    timerID = vertx.setTimer(OBSERVE_INTERVAL, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        eventBus.publish(input.getAddress(), new JsonObject()
          .putString("action", "observe")
          .putObject("context", Serializer.serialize(input)));
        periodicObserve();
      }
    });
  }

  /**
   * Cancels periodic observations.
   */
  private void cancelObserve() {
    vertx.cancelTimer(timerID);
  }

}
