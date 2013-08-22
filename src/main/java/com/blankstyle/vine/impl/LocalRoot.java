package com.blankstyle.vine.impl;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.RootException;

/**
 * A local root implementation.
 *
 * This root does not communicate with a remote root and seeds are not assigned
 * to specific machines. Seeds are simply started using the local Vert.x
 * container.
 *
 * @author Jordan Halterman
 */
public class LocalRoot implements Root {

  protected static final String ADDRESS_KEY = "address";

  protected static final String SEEDS_KEY = "seeds";

  protected Vertx vertx;

  private Container container;

  private Map<String, String> deploymentMap = new HashMap<String, String>();

  public LocalRoot(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
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
  public void deploy(JsonObject config, final Handler<AsyncResult<Feeder>> feedHandler) {
    String name = config.getString("name");
    if (name == null) {
      name = config.getString("address");
    }
    deploy(name, config, feedHandler);
  }

  @Override
  public void deploy(final String name, JsonObject config, final Handler<AsyncResult<Feeder>> feedHandler) {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    future.setHandler(feedHandler);

    JsonObject context = new JsonObject();
    final String address = config.getString(ADDRESS_KEY);
    if (deploymentMap.containsKey(address)) {
      future.setFailure(new RootException("Cannot redeploy vine."));
      return;
    }

    context.putString("address", address);
    context.putObject(SEEDS_KEY, config.getObject(SEEDS_KEY));
    container.deployVerticle("com.blankstyle.vine.VineVerticle", context, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          deploymentMap.put(address, result.result());
          future.setResult(new DefaultFeeder(address, vertx.eventBus()));
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

}
