/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.eventbus;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Event bus implementation that supports adaptive timeouts.<p>
 *
 * The adaptive event bus allows Vertigo to detect event bus reply
 * timeouts more quickly by periodically calculating the average
 * response time for a given event bus address. Given a delay threshold,
 * the event bus automatically adjusts the current reply timeout
 * based on brief historical data. This is used to help empty output
 * queues more quickly in the event of a temporary event bus failure.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface AdaptiveEventBus extends EventBus {

  @Override
  AdaptiveEventBus send(String address, Object message);

  @Override
  @SuppressWarnings("rawtypes")
  AdaptiveEventBus send(String address, Object message, Handler<Message> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Object message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Object message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  <T> AdaptiveEventBus send(String address, JsonObject message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, JsonObject message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, JsonObject message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, JsonObject message);

  @Override
  <T> AdaptiveEventBus send(String address, JsonArray message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, JsonArray message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, JsonArray message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, JsonArray message);

  @Override
  <T> AdaptiveEventBus send(String address, Buffer message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Buffer message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Buffer message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Buffer message);

  @Override
  <T> AdaptiveEventBus send(String address, byte[] message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, byte[] message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, byte[] message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, byte[] message);

  @Override
  <T> AdaptiveEventBus send(String address, String message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, String message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, String message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, String message);

  @Override
  <T> AdaptiveEventBus send(String address, Integer message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Integer message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Integer message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Integer message);

  @Override
  <T> AdaptiveEventBus send(String address, Long message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Long message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Long message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Long message);

  @Override
  <T> AdaptiveEventBus send(String address, Float message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Float message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Float message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Float message);

  @Override
  <T> AdaptiveEventBus send(String address, Double message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Double message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Double message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Double message);

  @Override
  <T> AdaptiveEventBus send(String address, Boolean message, Handler<Message<T>> replyHandler) ;

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Boolean message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Boolean message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Boolean message);

  @Override
  <T> AdaptiveEventBus send(String address, Short message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Short message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Short message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Short message);

  @Override
  <T> AdaptiveEventBus send(String address, Character message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Character message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Character message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Character message);

  @Override
  <T> AdaptiveEventBus send(String address, Byte message, Handler<Message<T>> replyHandler);

  @Override
  <T> AdaptiveEventBus sendWithTimeout(String address, Byte message, long timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  /**
   * Sends a message with an adaptive timeout.
   *
   * @param address The address to which to send the message.
   * @param message The message to send.
   * @param timeout The adaptive timeout.
   * @param replyHandler An asynchronous reply handler.
   * @return The adaptive event bus.
   */
  <T> AdaptiveEventBus sendWithAdaptiveTimeout(String address, Byte message, float timeout, Handler<AsyncResult<Message<T>>> replyHandler);

  @Override
  AdaptiveEventBus send(String address, Byte message);

  @Override
  AdaptiveEventBus publish(String address, Object message);

  @Override
  AdaptiveEventBus publish(String address, JsonObject message);

  @Override
  AdaptiveEventBus publish(String address, JsonArray message);

  @Override
  AdaptiveEventBus publish(String address, Buffer message);

  @Override
  AdaptiveEventBus publish(String address, byte[] message);

  @Override
  AdaptiveEventBus publish(String address, String message);

  @Override
  AdaptiveEventBus publish(String address, Integer message);

  @Override
  AdaptiveEventBus publish(String address, Long message);

  @Override
  AdaptiveEventBus publish(String address, Float message);

  @Override
  AdaptiveEventBus publish(String address, Double message);

  @Override
  AdaptiveEventBus publish(String address, Boolean message);

  @Override
  AdaptiveEventBus publish(String address, Short message);

  @Override
  AdaptiveEventBus publish(String address, Character message);

  @Override
  AdaptiveEventBus publish(String address, Byte message);

  @Override
  @SuppressWarnings("rawtypes")
  AdaptiveEventBus unregisterHandler(String address, Handler<? extends Message> handler,
                            Handler<AsyncResult<Void>> resultHandler);

  @Override
  @SuppressWarnings("rawtypes")
  AdaptiveEventBus unregisterHandler(String address, Handler<? extends Message> handler);

  @Override
  @SuppressWarnings("rawtypes")
  AdaptiveEventBus registerHandler(String address, Handler<? extends Message> handler,
                           Handler<AsyncResult<Void>> resultHandler);

  @Override
  @SuppressWarnings("rawtypes")
  AdaptiveEventBus registerHandler(String address, Handler<? extends Message> handler);

  @Override
  @SuppressWarnings("rawtypes")
  AdaptiveEventBus registerLocalHandler(String address, Handler<? extends Message> handler);

  @Override
  AdaptiveEventBus setDefaultReplyTimeout(long timeoutMs);

  /**
   * Sets the default adaptive timeout threshold.
   *
   * @param timeout The adaptive timeout threshold.
   * @return The adaptive event bus.
   */
  AdaptiveEventBus setDefaultAdaptiveTimeout(float timeout);

  /**
   * Returns the default adaptive timeout threshold.
   *
   * @return The default adaptive timeout threshold.
   */
  float getDefaultAdaptiveTimeout();

}
