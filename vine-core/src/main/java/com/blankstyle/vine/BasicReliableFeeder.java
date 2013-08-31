package com.blankstyle.vine;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;

public class BasicReliableFeeder extends BasicFeeder implements ReliableFeeder {

  private ReliableEventBus eventBus;

  public BasicReliableFeeder(String address, EventBus eventBus) {
    super(address, eventBus);
    this.eventBus = new WrappedReliableEventBus(eventBus);
  }

  public BasicReliableFeeder setVertx(Vertx vertx) {
    eventBus.setVertx(vertx);
    return this;
  }

  @Override
  public void feed(JsonObject data, long timeout, final Handler<AsyncResult<JsonObject>> resultHandler) {
    final Future<JsonObject> future = new DefaultFutureResult<JsonObject>().setHandler(resultHandler);
    eventBus.send(address, new JsonObject().putString("action", "feed").putObject("data", data), timeout, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.succeeded()) {
          Message<JsonObject> message = result.result();
          String error = message.body().getString("error");
          if (error != null) {
            // If a "Vine queue full." error was received, set the queueFull variable
            // and send an error back to the asynchronous handler.
            if (error == "Vine queue full.") {
              queueFull = true;
            }
  
            future.setFailure(new VineException(error));
          }
          else {
            // Invoke the result handler with the JSON message body.
            future.setResult(message.body());
  
            // If a message response is received, the queue should *always* be not full.
            queueFull = false;
            if (drainHandler != null) {
              drainHandler.handle(null);
            }
          }
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

}
