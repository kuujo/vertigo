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
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A bi-directional communication channel.
 *
 * @author Jordan Halterman
 */
public interface Channel {

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Object message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public void send(Object message, Handler<?> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(JsonObject message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(JsonObject message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(JsonArray message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(JsonArray message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Buffer message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Buffer message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(byte[] message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(byte[] message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(String message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(String message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Integer message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Integer message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Long message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Long message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Float message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Float message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Double message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Double message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Boolean message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Boolean message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Short message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Short message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Character message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Character message, Handler<T> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public void send(Byte message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> void send(Byte message, Handler<T> replyHandler);

  /**
   * Registers a message handler.
   *
   * @param handler
   *   The handler to register.
   */
  public void registerHandler(Handler<? extends Message<?>> handler);

  /**
   * Registers a message handler.
   *
   * @param handler
   *   The handler to register.
   * @param resultHandler
   *   A handler to be invoked once the registration has been propagated
   *   across the cluster.
   */
  public void registerHandler(Handler<? extends Message<?>> handler, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Registers a local message handler.
   *
   * @param handler
   *   The handler to register.
   */
  public void registerLocalHandler(Handler<? extends Message<?>> handler);

  /**
   * Unregisters a message handler.
   *
   * @param handler
   *   The handler to unregister.
   */
  public void unregisterHandler(Handler<? extends Message<?>> handler);

  /**
   * Unregisters a message handler.
   *
   * @param handler
   *   The handler to unregister.
   * @param resultHandler
   *   A handler to be invoked once the unregistration has been propagated
   *   across the cluster.
   */
  public void unregisterHandler(Handler<? extends Message<?>> handler, Handler<AsyncResult<Void>> resultHandler);

}
