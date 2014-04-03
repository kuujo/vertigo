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
package net.kuujo.vertigo.message;

import java.util.List;

/**
 * Message acker.
 *
 * @author Jordan Halterman
 */
public interface MessageAcker {

  /**
   * Anchors a parent message to a child message.
   *
   * @param parent The parent message.
   * @param child The child message.
   */
  void anchor(JsonMessage child);

  /**
   * Anchors a parent message to a list of child messages.
   *
   * @param parent The parent message.
   * @param children A list of child messages to anchor.
   */
  void anchor(List<JsonMessage> children);

  /**
   * Acks a message.
   *
   * @param message The message to ack.
   */
  void ack();

  /**
   * Times out a message.
   *
   * @param message The message to time out.
   */
  void timeout();

}
