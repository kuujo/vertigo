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
package net.kuujo.vertigo.streams;

import org.vertx.java.core.Handler;

/**
 * Data receive stream.
 *
 * @author Jordan Halterman
 *
 * @param <T> The receive stream type.
 */
public interface ReceiveStream<T extends ReceiveStream<T>> {

  /**
   * Pauses the receive stream.
   *
   * @return The stream.
   */
  T pause();

  /**
   * Resumes receiving data on the stream.
   *
   * @return The stream.
   */
  T resume();

  /**
   * Registers a message handler.
   *
   * @param handler A message handler.
   * @return The stream.
   */
  @SuppressWarnings("rawtypes")
  T messageHandler(Handler handler);

}
