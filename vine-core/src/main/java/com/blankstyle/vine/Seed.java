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
package com.blankstyle.vine;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.messaging.JsonMessage;

/**
 * A helper for Vert.x seed verticle implementations.
 *
 * @author Jordan Halterman
 */
public interface Seed {

  /**
   * Sets the seed vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   */
  public void setVertx(Vertx vertx);

  /**
   * Sets the seed container instance.
   *
   * @param container
   *   A Vert.x container.
   */
  public void setContainer(Container container);

  /**
   * Initializes the seed with the given context.
   *
   * @param context
   *   A seed context.
   */
  public void init(WorkerContext context);

  /**
   * Emits data from the seed.
   *
   * @param data
   *   The data to emit.
   */
  public void emit(JsonObject data);

  /**
   * Emits data from the seed.
   *
   * @param data
   *   The data to emit.
   * @param parents
   *   A list of parents to the given data.
   */
  public void emit(JsonObject data, JsonMessage... parents);

  /**
   * Sets a data handler on the seed.
   *
   * @param handler
   *   The data handler.
   * @return
   *   The called seed instance.
   */
  public Seed dataHandler(Handler<JsonMessage> handler);

  /**
   * Sets an ack handler on the seed.
   *
   * @param handler
   *   The ack handler.
   * @return
   *   The called seed instance.
   */
  public Seed ackHandler(Handler<JsonMessage> handler);

  /**
   * Sets a fail handler on the seed.
   *
   * @param handler
   *   The fail handler.
   * @return
   *   The called seed instance.
   */
  public Seed failHandler(Handler<JsonMessage> handler);

}
