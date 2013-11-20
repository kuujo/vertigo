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

import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Network;

/**
 * An input hook.
 *
 * This hook type may be added to any {@link InputCollector} instance
 * either directly via the {@link InputCollector} interface or via a
 * {@link Network} definition.
 *
 * @author Jordan Halterman
 */
public interface InputHook extends Hook<InputCollector> {

  /**
   * Called when the component receives an input message.
   *
   * @param messageId
   *   The unique message identifier.
   */
  void handleReceive(MessageId messageId);

  /**
   * Called when the component acks a received message.
   *
   * @param messageId
   *   The unique message identifier.
   */
  void handleAck(MessageId messageId);

  /**
   * Called when the component fails a received message.
   *
   * @param messageId
   *   The unique message identifier.
   */
  void handleFail(MessageId messageId);

}
