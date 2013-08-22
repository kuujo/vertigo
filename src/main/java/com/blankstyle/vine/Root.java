package com.blankstyle.vine;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

public interface Root {

  /**
   * Sets the root vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   */
  public void setVertx(Vertx vertx);

  /**
   * Gets the root vertx instance.
   *
   * @return
   *   A vertx instance.
   */
  public Vertx getVertx();

  /**
   * Sets the root container instance.
   *
   * @param container
   *   A container instance.
   */
  public void setContainer(Container container);

  /**
   * Gets the root container instance.
   *
   * @return
   *   A container instance.
   */
  public Container getContainer();

  /**
   * Deploys a vine.
   *
   * @param config
   *   The vine configuration.
   * @param feedHandler
   *   A handler to invoke with a feeder to the vine.
   */
  public void deploy(JsonObject config, Handler<AsyncResult<Feeder>> feedHandler);

  /**
   * Deploys a vine.
   *
   * @param name
   *   The vine name.
   * @param config
   *   The vine configuration.
   * @param feedHandler
   *   A handler to invoke with a feeder to the vine.
   */
  public void deploy(String name, JsonObject config, Handler<AsyncResult<Feeder>> feedHandler);

}
