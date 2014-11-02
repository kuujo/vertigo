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

import net.kuujo.vertigo.input.Input;

/**
 * Controllable input.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ControllableInput<T extends Input<T, U>, U> extends Input<T, U> {

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

}
