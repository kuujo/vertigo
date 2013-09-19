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
package net.kuujo.vine.vine;

import net.kuujo.vine.context.VineContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A vine instance.
 *
 * @author Jordan Halterman
 */
public interface Vine {

  /**
   * Sets the seed vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   * @return
   *   The called seed instance.
   */
  public Vine setVertx(Vertx vertx);

  /**
   * Sets the seed container instance.
   *
   * @param container
   *   A Vert.x container.
   * @return
   *   The called seed instance.
   */
  public Vine setContainer(Container container);

  /**
   * Sets the seed context.
   *
   * @param context
   *   A seed context.
   * @return
   *   The called seed instance.
   */
  public Vine setContext(VineContext context);

  /**
   * Starts the vine.
   */
  public void start();

  /**
   * Feeds data to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(JsonObject data);

  /**
   * Feeds data to the vine, providing a completion handler.
   *
   * @param data
   *   The data to feed.
   * @param doneHandler
   *   A handler to be invoked once data processing is complete.
   */
  public void feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Executes the vine as a remote procedure.
   *
   * @param data
   *   The data with which to execute.
   * @param resultHandler
   *   A handler to be invoked with the procedure result.
   */
  public void execute(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler);

}
