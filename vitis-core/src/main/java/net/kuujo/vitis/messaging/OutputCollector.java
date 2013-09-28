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

/**
 * An output collector.
 *
 * Output collectors manage multiple output channels and emit messages
 * to those channels, monitoring replies for message acking. This allows
 * multiple messages to multiple output channels to be acked together.
 *
 * @author Jordan Halterman
 */
public interface OutputCollector {

  /**
   * Adds an output channel to the collector.
   *
   * @param channel
   *   The output channel.
   * @return
   *   The called output collector.
   */
  public OutputCollector addChannel(Channel channel);

  /**
   * Removes an output channel from the collector.
   *
   * @param channel
   *   The channel to remove.
   * @return
   *   The called output collector.
   */
  public OutputCollector removeChannel(Channel channel);

  /**
   * Returns the number of channels in the output.
   */
  public int size();

  /**
   * Emits a message to all output channels.
   *
   * @param message
   *   The message to emit.
   * @return
   *   The called output collector.
   */
  public OutputCollector emit(JsonMessage message);

  /**
   * Emits a set of messages to all output channels.
   *
   * @param messages
   *   The messages to emit.
   * @return
   *   The called output collector.
   */
  public OutputCollector emit(JsonMessage... messages);

  /**
   * Acks a message.
   *
   * @param message
   *   The message to ack.
   * @return
   *   The called output collector.
   */
  public OutputCollector ack(JsonMessage message);

  /**
   * Acks a set of messages.
   *
   * @param messages
   *   The messages to ack.
   * @return
   *   The called output collector.
   */
  public OutputCollector ack(JsonMessage... messages);

  /**
   * Fails a message.
   *
   * @param message
   *   The message to fail.
   * @return
   *   The called output collector.
   */
  public OutputCollector fail(JsonMessage message);

  /**
   * Fails a set of messages.
   *
   * @param messages
   *   The messages to fail.
   * @return
   *   The called output collector.
   */
  public OutputCollector fail(JsonMessage... messages);

}
