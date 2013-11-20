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
package net.kuujo.vertigo.auditor;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.message.MessageId;

/**
 * A network message acker.
 *
 * @author Jordan Halterman
 */
public interface Auditor {

  /**
   * Sets the acker Vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   */
  void setVertx(Vertx vertx);

  /**
   * Sets the acker container instance.
   *
   * @param container
   *   A container instance.
   */
  void setContainer(Container container);

  /**
   * Sets the auditor's parent acker instance.
   *
   * @param auditorVerticle
   *   The parent acker.
   */
  void setAcker(AuditorVerticle auditorVerticle);

  /**
   * Sets the message timeout.
   *
   * @param timeout
   *   The message timeout.
   */
  void setTimeout(long timeout);

  /**
   * Called when the auditor has started.
   */
  void start();

  /**
   * Creates a new message tree.
   *
   * @param messageId
   *   The root message ID.
   */
  void create(MessageId messageId);

  /**
   * Forks a message.
   *
   * @param messageId
   *   The forked message ID.
   */
  void fork(MessageId messageId);

  /**
   * Acks a message.
   *
   * @param messageId
   *   The acked message ID.
   */
  void ack(MessageId messageId);

  /**
   * Fails a message.
   *
   * @param messageId
   *   The failed message ID.
   */
  void fail(MessageId messageId);

  /**
   * Called when the auditor has stopped.
   */
  void stop();

}
