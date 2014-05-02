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
package net.kuujo.vertigo.io;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * Basic input interface.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The input type.
 */
public interface Input<T extends Input<T>> {

  /**
   * Returns the input's vertx instance.
   *
   * @return The inputs Vert.x instance.
   */
  Vertx vertx();

  /**
   * Pauses the input.<p>
   *
   * When the input is paused, messages received by the input will be buffered,
   * so it is important that inputs not be paused for too long if messages
   * continue to flow.
   *
   * @return The input.
   */
  T pause();

  /**
   * Resumes receiving data on the input.<p>
   *
   * When the input is resumed, any messages that were buffered during the pause
   * will be processed first. Once the input's buffer is empty it will resume
   * normal operation.
   *
   * @return The input.
   */
  T resume();

  /**
   * Registers a message handler on the input.<p>
   *
   * The message handler can accept any message type that is supported by
   * the Vert.x event bus. Vertigo messages cannot be replied to and do not
   * need to be acked. The input handles communicating and coordinating with
   * outputs internally.
   *
   * @param handler A message handler.
   * @return The input.
   */
  @SuppressWarnings("rawtypes")
  T messageHandler(Handler handler);

}
