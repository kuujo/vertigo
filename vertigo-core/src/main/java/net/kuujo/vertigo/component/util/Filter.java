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
package net.kuujo.vertigo.component.util;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.Handler;

/**
 * An incoming message filter.
 *
 * @author Jordan Halterman
 */
public interface Filter<T extends Filter<T>> extends Handler<JsonMessage> {

  /**
   * Sets a message handler on the filter.
   *
   * This is the handler that will be invoked for messages that successfully
   * pass the filter constraints.
   *
   * @param handler
   *   The message handler.
   * @return
   *   The called filter instance.
   */
  public T messageHandler(Handler<JsonMessage> handler);

  /**
   * Sets a discard handler on the filter.
   *
   * This is the handler that will be invoked for messages that failed the
   * filter constraints.
   *
   * @param handler
   *   The discard filter.
   * @return
   *   The called filter instance.
   */
  public T discardHandler(Handler<JsonMessage> handler);

}
