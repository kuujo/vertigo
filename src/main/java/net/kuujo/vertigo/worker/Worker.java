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
package net.kuujo.vertigo.worker;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A worker component.
 *
 * Workers are components that both receive and emit messages. Messages arrive
 * in the form of {@link JsonMessage} instances. Messages should always be either
 * acked or failed. Failing to ack or fail a message may result in the message
 * timing out depending on the network configuration. Since network configurations
 * are abstracted from component implementations, it is important that workers
 * always ack or fail messages. Note that messages should be acked or failed
 * after any child messages have been emitted from the worker.
 *
 * @author Jordan Halterman
 */
public interface Worker extends Component<Worker> {

  /**
   * Sets a worker message handler.
   *
   * @param handler
   *   A message handler. This handler will be called for each message received
   *   by the worker.
   * @return 
   *   The called worker instance.
   */
  Worker messageHandler(Handler<JsonMessage> handler);

  /**
   * Emits a message to the default stream.
   *
   * @param body
   *   The message body.
   * @return
   *   The emitted message identifier.
   */
  MessageId emit(JsonObject body);

  /**
   * Emits child message to the default stream.
   *
   * @param body
   *   The message body.
   * @param parent
   *   The parent message.
   * @return
   *   The emitted message identifier.
   */
  MessageId emit(JsonObject body, JsonMessage parent);

  /**
   * Emits a copy of the given message to the default stream as a child of itself.
   *
   * @param message
   *   The message to emit.
   * @return
   *   The new child message identifier.
   */
  MessageId emit(JsonMessage message);

  /**
   * Emits data from the worker to a non-default stream.
   *
   * @param stream
   *   The stream to which to emit the message.
   * @param body
   *   The message body.
   * @return
   *   The emitted message identifier.
   */
  MessageId emit(String stream, JsonObject body);

  /**
   * Emits child data from the worker to a non-default stream.
   *
   * @param stream
   *   The stream to which to emit the message.
   * @param body
   *   The message body.
   * @param parent
   *   The parent message.
   * @return
   *   The emitted message identifier.
   */
  MessageId emit(String stream, JsonObject body, JsonMessage parent);

  /**
   * Emits a copy of the given message to a non-default stream as a child of itself.
   *
   * @param stream
   *   The stream to which to emit the message.
   * @param message
   *   The message to emit.
   * @return
   *   The new child message identifier.
   */
  MessageId emit(String stream, JsonMessage message);

  /**
   * Acknowledges processing of a message.
   *
   * @param message
   *   The message to ack.
   */
  void ack(JsonMessage message);

  /**
   * Fails processing of a message.
   *
   * @param message
   *   The message to fail.
   */
  void fail(JsonMessage message);

}
