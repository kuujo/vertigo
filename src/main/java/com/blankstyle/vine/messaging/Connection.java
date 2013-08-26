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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A single point-to-point connection.
 *
 * @author Jordan Halterman
 */
public interface Connection {

  /**
   * Gets the remote channel address.
   *
   * @return
   *   The remote channel address.
   */
  public String getAddress();

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Object message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public Connection send(Object message, @SuppressWarnings("rawtypes") Handler<Message> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(JsonObject message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(JsonObject message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(JsonArray message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(JsonArray message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Buffer message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Buffer message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(byte[] message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(byte[] message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(String message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(String message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Integer message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Integer message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Long message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Long message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Float message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Float message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Double message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Double message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Boolean message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Boolean message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Short message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Short message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Character message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Character message, Handler<Message<T>> replyHandler);

  /**
   * Sends a message through the channel.
   *
   * @param message
   *   The message to send.
   */
  public Connection send(Byte message);

  /**
   * Sends a message through the channel, providing a handler for a return value.
   *
   * @param message
   *   The message to send.
   * @param replyHandler
   *   A reply handler.
   */
  public <T> Connection send(Byte message, Handler<Message<T>> replyHandler);

  /**
   * Registers a message handler.
   *
   * @param handler
   *   The handler to register.
   */
  public Connection registerHandler(Handler<? extends Message<?>> handler);

  /**
   * Registers a message handler.
   *
   * @param handler
   *   The handler to register.
   * @param resultHandler
   *   A handler to be invoked once the registration has been propagated
   *   across the cluster.
   */
  public Connection registerHandler(Handler<? extends Message<?>> handler, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Registers a local message handler.
   *
   * @param handler
   *   The handler to register.
   */
  public Connection registerLocalHandler(Handler<? extends Message<?>> handler);

  /**
   * Unregisters a message handler.
   *
   * @param handler
   *   The handler to unregister.
   */
  public Connection unregisterHandler(Handler<? extends Message<?>> handler);

  /**
   * Unregisters a message handler.
   *
   * @param handler
   *   The handler to unregister.
   * @param resultHandler
   *   A handler to be invoked once the unregistration has been propagated
   *   across the cluster.
   */
  public Connection unregisterHandler(Handler<? extends Message<?>> handler, Handler<AsyncResult<Void>> resultHandler);

}
