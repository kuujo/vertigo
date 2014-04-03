/*
 * Copyright 2013-2014 the original author or authors.
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

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.network.NetworkConfig;

/**
 * A component hook.
 *
 * This hook type may be added to any {@link Component} instance either
 * directly via the {@link Component} interface or externally via the
 * {@link NetworkConfig} definition.
 *
 * @author Jordan Halterman
 */
public interface ComponentHook extends Hook {

  /**
   * Called when the component has started.
   *
   * @param component The component instance.
   */
  void handleStart(Component component);

  /**
   * Called when the component receives an input message.
   *
   * @param port The port on which the message was received.
   * @param messageId The unique message identifier.
   */
  void handleReceive(String port, String messageId);

  /**
   * Called when the component sends a message.
   *
   * @param port The port on which the message was sent.
   * @param messageId The unique message identifier.
   */
  void handleSend(String port, String messageId);

  /**
   * Called when the component has stopped.
   *
   * @param component The component instance.
   */
  void handleStop(Component component);

}
