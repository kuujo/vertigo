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
package net.kuujo.vevent.eventbus;

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A Vert.x eventbus with reply timeout support.
 *
 * @author Jordan Halterman
 */
public interface ReliableEventBus extends EventBus {

  /**
   * Sets the eventbus vertx instance.
   *
   * @param vertx
   *   A Vertx instance.
   * @return
   *   The called eventbus object.
   */
  public ReliableEventBus setVertx(Vertx vertx);

  /**
   * Gets the eventbus vertx instance.
   *
   * @return
   *   A Vertx instance.
   */
  public Vertx getVertx();

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public EventBus send(String address, Object message, AsyncResultHandler<Message> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public EventBus send(String address, Object message, long timeout, AsyncResultHandler<Message> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public EventBus send(String address, Object message, long timeout, boolean retry, AsyncResultHandler<Message> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public EventBus send(String address, Object message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, JsonObject message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, JsonObject message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, JsonObject message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, JsonObject message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, JsonArray message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, JsonArray message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, JsonArray message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, JsonArray message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Buffer message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Buffer message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Buffer message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Buffer message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, byte[] message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, byte[] message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, byte[] message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, byte[] message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, String message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, String message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, String message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, String message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Integer message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Integer message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Integer message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Integer message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Long message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Long message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Long message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Long message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Float message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Float message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Float message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Float message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Double message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Double message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Double message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Double message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Boolean message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Boolean message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Boolean message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Boolean message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Short message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Short message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Short message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Short message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Character message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Character message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Character message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Character message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a default timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Byte message, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   */
  public <T> EventBus send(String address, Byte message, long timeout, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Byte message, long timeout, boolean retry, AsyncResultHandler<Message<T>> replyHandler);

  /**
   * Sends a message with a timeout, attempting to re-send the message if a timeout occurs.
   *
   * @param address
   *   The address to which to send the message.
   * @param message
   *   The message.
   * @param timeout
   *   The message timeout.
   * @param retry
   *   A boolean indicating whether to attempt to re-send messages.
   * @param attempts
   *   The number of re-send attempts allowed.
   * @param replyHandler
   *   An asynchronous reply handler. This will be invoked with the message reply,
   *   or with a TimeoutException if the response times out.
   * @return
   *   The called eventbus instance.
   */
  public <T> EventBus send(String address, Byte message, long timeout, boolean retry, int attempts, AsyncResultHandler<Message<T>> replyHandler);

}
