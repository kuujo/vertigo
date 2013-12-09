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
package net.kuujo.vertigo.input;

import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An input listener.<p>
 *
 * Listeners are the underlying components of the {@link InputCollector}. Each
 * listener instance has a unique event bus address and represents a single input
 * for the component, and thus each listener subscribes to the output of a single
 * component. Listeners also handle sending ack/fail messages to network auditors,
 * so they can even be used outside of component implementations to "tap" into
 * existing network components - all while maintaining the core ack/fail functions.
 *
 * @author Jordan Halterman
 */
public interface Listener {

  /**
   * Sets auto acking on the listener.
   *
   * @param autoAck
   *   Indicates whether to automatically ack messages received by the listener.
   * @return
   *   The called listener instance.
   */
  Listener setAutoAck(boolean autoAck);

  /**
   * Gets the auto ack setting on the listener.
   *
   * @return
   *   Indicates whether the listener auto acks received messages. Defaults to true.
   */
  boolean isAutoAck();

  /**
   * Sets a message handler on the listener.
   *
   * @param handler
   *   The message handler to set.
   * @return
   *   The called listener instance.
   */
  Listener messageHandler(Handler<JsonMessage> handler);

  /**
   * Acknowledges a message.
   *
   * @param message
   *   The message to ack.
   * @return
   *   The called listener instance.
   */
  Listener ack(JsonMessage message);

  /**
   * Fails a message.
   *
   * @param message
   *   The message to fail.
   * @return
   *   The called listener instance.
   */
  Listener fail(JsonMessage message);

  /**
   * Starts the listener.
   *
   * @return
   *   The called listener instance.
   */
  Listener start();

  /**
   * Starts the listener.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the listener is started.
   * @return
   *   The called listener instance.
   */
  Listener start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops the listener.
   */
  void stop();

  /**
   * Stops the listener.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the listener is stopped.
   */
  void stop(Handler<AsyncResult<Void>> doneHandler);

}
