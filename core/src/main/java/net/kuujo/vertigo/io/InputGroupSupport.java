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

import net.kuujo.vertigo.io.group.InputGroup;

import org.vertx.java.core.Handler;

/**
 * Support for receiving input groups.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The type to which the support belongs.
 */
public interface InputGroupSupport<T extends InputGroupSupport<T>> {

  /**
   * Registers a group handler.
   *
   * @param handler The handler to register. This handler will be called
   *        whenever any group on the input is started.
   * @return The called object.
   */
  T groupHandler(Handler<InputGroup> handler);

  /**
   * Registers a group handler.
   *
   * @param name The name of the group for which to register the handler.
   * @param handler The handler to register. This handler will be called
   *        whenever a group of the given name is started.
   * @return The called object.
   */
  T groupHandler(String name, Handler<InputGroup> handler);

}
