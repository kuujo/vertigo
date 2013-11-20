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
package net.kuujo.vertigo.hooks;

import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Network;

/**
 * An output hook.
 *
 * This hook type may be added to any {@link OutputCollector} instance
 * either directly via the {@link OutputCollector} interface or via a
 * {@link Network} definition.
 *
 * @author Jordan Halterman
 */
public interface OutputHook extends Hook<OutputCollector> {

  /**
   * Called when the output emits a message.
   *
   * @param messageId
   *   The unique message identifier.
   */
  void handleEmit(MessageId messageId);

  /**
   * Called when the output receives an ack for an emitted message.
   *
   * @param messageId
   *   The unique message identifier.
   */
  void handleAcked(MessageId messageId);

  /**
   * Called when the output receives a failure for an emitted message.
   *
   * @param messageId
   *   The unique message identifier.
   */
  void handleFailed(MessageId messageId);

  /**
   * Called when the output receives a timeout for an emitted message.
   *
   * @param messageId
   *   The unique message identifier.
   */
  void handleTimeout(MessageId messageId);

}
