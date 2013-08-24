package com.blankstyle.vine;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.context.JsonVineContext;
import com.blankstyle.vine.definition.VineDefinition;

public class RemoteRoot implements Root {

  private Container container;

  private Vertx vertx;

  private String address;

  private EventBus eventBus;

  public RemoteRoot(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  /**
   * Loads a remote vine context.
   *
   * @param name
   *   The vine name.
   * @return
   *   A remote vine context. The context will be updated once a response is
   *   received from the remote root.
   */
  public JsonVineContext loadContext(String name) {
    final JsonVineContext context = createRemoteContext(name);
    eventBus.send(address, new JsonObject().putString("action", "load").putString("context", name), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        context.update(message.body());
      }
    });
    return context;
  }

  private JsonVineContext createRemoteContext(String name) {
    final JsonVineContext context = new JsonVineContext(name);
    eventBus.registerHandler(String.format("vine.context.%s", name), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        context.update(message.body());
      }
    });
    return context;
  }

  @Override
  public void setVertx(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public void setContainer(Container container) {
    this.container = container;
  }

  @Override
  public Container getContainer() {
    return container;
  }

  @Override
  public void deploy(VineDefinition component) {
    eventBus.send(address, new JsonObject().putString("action", "deploy").putObject("definition", component.serialize()));
  }

  @Override
  public void deploy(final VineDefinition component, final Handler<AsyncResult<Feeder>> doneHandler) {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    eventBus.send(address, new JsonObject().putString("action", "deploy").putObject("definition", component.serialize()), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Feeder feeder = new BasicFeeder(component.getAddress(), eventBus);
        future.setResult(feeder);
        doneHandler.handle(future);
      }
    });
  }

}
