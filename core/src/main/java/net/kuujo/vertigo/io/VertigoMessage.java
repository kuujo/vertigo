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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.MultiMap;

/**
 * Vertigo message.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface VertigoMessage<T> {

  /**
   * Returns the unique message identifier.
   *
   * @return The unique message identifier.
   */
  String id();

  /**
   * Returns the message connection index.
   *
   * @return The message connection index.
   */
  long index();

  /**
   * Returns the name of the port on which the message was received.
   *
   * @return The name of the port on which the message was received.
   */
  String port();

  /**
   * Returns the message body.
   *
   * @return The message body.
   */
  T body();

  /**
   * Returns the message headers.
   *
   * @return The message headers.
   */
  MultiMap headers();

}
