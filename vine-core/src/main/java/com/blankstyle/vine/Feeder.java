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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A Vine feeder.
 *
 * @author Jordan Halterman
 */
public interface Feeder {

  /**
   * Indicates whether the feed queue is full.
   *
   * @return
   *   The called object.
   */
  public boolean feedQueueFull();

  /**
   * Feeds a JSON object to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(JsonObject data);

  /**
   * Feeds a JSON object to the vine, providing a handler for completion.
   *
   * @param data
   *   The data to feed.
   * @param doneHandler
   *   A handler to be invoked once processing is complete.
   */
  public void feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Feeds a JSON object to the vine, awaiting completion with a timeout.
   *
   * @param data
   *   The data to feed.
   * @param timeout
   *   A response timeout.
   * @param doneHandler
   *   A handler to be invoked once processing is complete.
   */
  public void feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Executes a remote procedure call on the vine.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to be invoked with the return value.
   */
  public void execute(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Executes a remote procedure call on the vine with a timeout.
   *
   * @param data
   *   The data to feed.
   * @param timeout
   *   A response timeout.
   * @param resultHandler
   *   A handler to be invoked with the return value.
   */
  public void execute(JsonObject data, long timeout, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Sets a drain handler on the feeder.
   *
   * @param drainHandler
   *   The drain handler.
   * @return
   *   The called object.
   */
  public Feeder drainHandler(Handler<Void> drainHandler);

}
