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
package net.kuujo.vitis.messaging;

import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An output collector.
 *
 * Output collectors manage multiple output streams and emit messages
 * to those streams, monitoring replies for message acking. This allows
 * multiple messages to multiple output streams to be acked together.
 *
 * @author Jordan Halterman
 */
public interface OutputCollector {

  /**
   * Adds an output stream to the collector.
   *
   * @param name
   *   The stream name.
   * @param stream
   *   The output stream.
   * @return
   *   The called output collector.
   */
  public OutputCollector addStream(String name, Shoot<?> stream);

  /**
   * Removes an output stream from the collector.
   *
   * @param name
   *   The name of the stream to remove.
   * @return
   *   The called output collector.
   */
  public OutputCollector removeStream(String name);

  /**
   * Returns a set of stream names.
   */
  public Set<String> getStreamNames();

  /**
   * Returns a stream by name.
   */
  public Shoot<?> getStream(String name);

  /**
   * Returns the number of streams in the output.
   */
  public int size();

  /**
   * Emits a message to all output streams.
   *
   * @param message
   *   The message to emit.
   * @return
   *   The called output collector.
   */
  public OutputCollector emit(JsonMessage message);

  /**
   * Emits a message to all output streams.
   *
   * @param message
   *   The message to emit.
   * @param ackHandler
   *   An asynchronous handler to be invoked once the message is acked.
   * @return
   *   The called output collector.
   */
  public OutputCollector emit(JsonMessage message, Handler<AsyncResult<Boolean>> ackHandler);

  /**
   * Emits a message to all output streams.
   *
   * @param message
   *   The message to emit.
   * @param timeout
   *   An ack timeout.
   * @param ackHandler
   *   An asynchronous handler to be invoked once the message is acked.
   * @return
   *   The called output collector.
   */
  public OutputCollector emit(JsonMessage message, long timeout, Handler<AsyncResult<Boolean>> ackHandler);

  /**
   * Emits a set of messages to all output streams.
   *
   * @param messages
   *   The messages to emit.
   * @return
   *   The called output collector.
   */
  public OutputCollector emit(JsonMessage[] messages);

  /**
   * Emits a set of messages to all output streams.
   *
   * @param messages
   *   The messages to emit.
   * @param ackHandler
   *   An asynchronous handler to be invoked once the message is acked.
   * @return
   *   The called output collector.
   */
  public OutputCollector emit(JsonMessage[] messages, Handler<AsyncResult<Boolean>> ackHandler);

  /**
   * Emits a set of messages to all output streams.
   *
   * @param messages
   *   The messages to emit.
   * @param timeout
   *   An ack timeout.
   * @param ackHandler
   *   An asynchronous handler to be invoked once the message is acked.
   * @return
   *   The called output collector.
   */
  public OutputCollector emit(JsonMessage[] messages, long timeout, Handler<AsyncResult<Boolean>> ackHandler);

  /**
   * Emits a message to a specific output stream.
   *
   * @param streamName
   *   The name of stream to which to emit.
   * @param message
   *   The message to emit.
   * @return
   *   The called output collector.
   */
  public OutputCollector emitTo(String streamName, JsonMessage message);

  /**
   * Emits a message to a specific output stream.
   *
   * @param streamName
   *   The name of stream to which to emit.
   * @param message
   *   The message to emit.
   * @param ackHandler
   *   An asynchronous handler to be invoked once the message is acked.
   * @return
   *   The called output collector.
   */
  public OutputCollector emitTo(String streamName, JsonMessage message, Handler<AsyncResult<Boolean>> ackHandler);

  /**
   * Emits a message to a specific output stream.
   *
   * @param streamName
   *   The name of stream to which to emit.
   * @param message
   *   The message to emit.
   * @param timeout
   *   An ack timeout.
   * @param ackHandler
   *   An asynchronous handler to be invoked once the message is acked.
   * @return
   *   The called output collector.
   */
  public OutputCollector emitTo(String streamName, JsonMessage message, long timeout, Handler<AsyncResult<Boolean>> ackHandler);

  /**
   * Emits a set of messages to a specific output stream.
   *
   * @param streamName
   *   The name of stream to which to emit.
   * @param messages
   *   The messages to emit.
   * @return
   *   The called output collector.
   */
  public OutputCollector emitTo(String streamName, JsonMessage[] messages);

  /**
   * Emits a set of messages to a specific output stream.
   *
   * @param streamName
   *   The name of stream to which to emit.
   * @param messages
   *   The messages to emit.
   * @param ackHandler
   *   An asynchronous handler to be invoked once the message is acked.
   * @return
   *   The called output collector.
   */
  public OutputCollector emitTo(String streamName, JsonMessage[] messages, Handler<AsyncResult<Boolean>> ackHandler);

  /**
   * Emits a set of messages to a specific output stream.
   *
   * @param streamName
   *   The name of stream to which to emit.
   * @param messages
   *   The messages to emit.
   * @param timeout
   *   An ack timeout.
   * @param ackHandler
   *   An asynchronous handler to be invoked once the message is acked.
   * @return
   *   The called output collector.
   */
  public OutputCollector emitTo(String streamName, JsonMessage[] messages, long timeout, Handler<AsyncResult<Boolean>> ackHandler);

}
