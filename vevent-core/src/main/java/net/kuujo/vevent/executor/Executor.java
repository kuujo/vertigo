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
package net.kuujo.vevent.executor;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A vine executor.
 *
 * @author Jordan Halterman
 */
public interface Executor {

  /**
   * Sets the feed queue max size.
   *
   * @param maxSize
   *   The feed queue max size.
   * @return
   *   The called executor instance.
   */
  public Executor setFeedQueueMaxSize(long maxSize);

  /**
   * Indicates whether the feeder queue is full.
   *
   * @return
   *   A boolean indicating whether the feeder queue is full.
   */
  public boolean feedQueueFull();

  /**
   * Executes the vine as a remote procedure.
   *
   * @param args
   *   Arguments to the vine.
   * @param resultHandler
   *   An asynchronous handler to be invoked with the execution result.
   * @return
   *   The called executor instance.
   */
  public Executor execute(JsonObject args, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Executes the vine as a remote procedure with a timeout.
   *
   * @param args
   *   Arguments to the vine.
   * @param timeout
   *   An execution timeout. If a timeout occurs, the result will fail.
   * @param resultHandler
   *   An asynchronous handler to be invoked with the execution result.
   * @return
   *   The called executor instance.
   */
  public Executor execute(JsonObject args, long timeout, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Sets a drain handler on the feeder.
   *
   * @param drainHandler
   *   The drain handler.
   * @return
   *   The called object.
   * @return
   *   The called executor instance.
   */
  public Executor drainHandler(Handler<Void> handler);

}
