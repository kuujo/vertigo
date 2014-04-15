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
package net.kuujo.vertigo.input;

import org.vertx.java.core.Handler;

/**
 * Basic input interface.
 *
 * @author Jordan Halterman
 *
 * @param <T> The input type.
 */
public interface Input<T extends Input<T>> {

  /**
   * Pauses the input.
   *
   * @return The input.
   */
  T pause();

  /**
   * Resumes receiving data on the input.
   *
   * @return The input.
   */
  T resume();

  /**
   * Registers a message handler on the input.
   *
   * @param handler A message handler.
   * @return The stream.
   */
  @SuppressWarnings("rawtypes")
  T messageHandler(Handler handler);

  /**
   * Registers a group handler on the input.
   *
   * @param group The name of the group for which to register the handler.
   * @param handler A handler to be called when the group is received.
   * @return The input instance.
   */
  T groupHandler(String group, Handler<InputGroup> handler);

}
