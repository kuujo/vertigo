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

import net.kuujo.vertigo.io.group.OutputGroup;

import org.vertx.java.core.Handler;

/**
 * Support for creating output groups.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The type to which the support belongs.
 */
public interface OutputGroupSupport<T extends OutputGroupSupport<T>> {

  /**
   * Creates a uniquely named output group.<p>
   *
   * Messages sent through groups will always be sent to the same
   * connection(s). Groups can also be nested.
   *
   * @param handler A handler to be called once the group has been setup.
   * @return The called object.
   */
  T group(Handler<OutputGroup> handler);

  /**
   * Creates a named output group.<p>
   *
   * Messages sent through groups will always be sent to the same
   * connection(s). Groups can also be nested.
   *
   * @param name The output group name.
   * @param handler A handler to be called once the group has been setup.
   * @return The called object.
   */
  T group(String name, Handler<OutputGroup> handler);

}
