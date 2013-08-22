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
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
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
   * Sets the feed queue max size.
   *
   * @param maxSize
   *   The max feed queue size.
   * @return
   *   The called object.
   */
  public Feeder setFeedQueueMaxSize(int maxSize);

  /**
   * Sets a drain handler on the feeder.
   *
   * @param drainHandler
   *   The drain handler.
   * @return
   *   The called object.
   */
  public Feeder drainHandler(Handler<Void> drainHandler);

  /**
   * Feeds a value to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Object data);

  /**
   * Feeds a value to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public void feed(Object data, Handler resultHandler);

  /**
   * Feeds a JSON object to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(JsonObject data);

  /**
   * Feeds a JSON object to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(JsonObject data, Handler<T> resultHandler);

  /**
   * Feeds a JSON array to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(JsonArray data);

  /**
   * Feeds a JSON array to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(JsonArray data, Handler<T> resultHandler);

  /**
   * Feeds a buffer to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Buffer data);

  /**
   * Feeds a buffer to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Buffer data, Handler<T> resultHandler);

  /**
   * Feeds a byte array to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(byte[] data);

  /**
   * Feeds a byte array to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(byte[] data, Handler<T> resultHandler);

  /**
   * Feeds a string to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(String data);

  /**
   * Feeds a string to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(String data, Handler<T> resultHandler);

  /**
   * Feeds an integer to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Integer data);

  /**
   * Feeds an integer to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Integer data, Handler<T> resultHandler);

  /**
   * Feeds a long to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Long data);

  /**
   * Feeds a long to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Long data, Handler<T> resultHandler);

  /**
   * Feeds a float to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Float data);

  /**
   * Feeds a float to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Float data, Handler<T> resultHandler);

  /**
   * Feeds a double to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Double data);

  /**
   * Feeds a double to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Double data, Handler<T> resultHandler);

  /**
   * Feeds a boolean to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Boolean data);

  /**
   * Feeds a boolean to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Boolean data, Handler<T> resultHandler);

  /**
   * Feeds a short to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Short data);

  /**
   * Feeds a short to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Short data, Handler<T> resultHandler);

  /**
   * Feeds a character to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Character data);

  /**
   * Feeds a character to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Character data, Handler<T> resultHandler);

  /**
   * Feeds a byte to the vine.
   *
   * @param data
   *   The data to feed.
   */
  public void feed(Byte data);

  /**
   * Feeds a byte to the vine, providing a handler for a return value.
   *
   * @param data
   *   The data to feed.
   * @param resultHandler
   *   A handler to invoke once the vine has completed processing data.
   */
  public <T> void feed(Byte data, Handler<T> resultHandler);

}
