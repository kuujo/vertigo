package net.kuujo.vertigo.output;

import net.kuujo.vertigo.messaging.JsonMessage;

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
      
    }
  };

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public OutputCollector emit(JsonObject data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public OutputCollector emit(JsonObject data, String tag) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public OutputCollector emit(JsonObject data, JsonMessage parent) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public OutputCollector emit(JsonObject data, String tag, JsonMessage parent) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void start() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void start(Handler<AsyncResult<Void>> doneHandler) {
    eventBus.registerHandler(address, handler, doneHandler);
  }

}
