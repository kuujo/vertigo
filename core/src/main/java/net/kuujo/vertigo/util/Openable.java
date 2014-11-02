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
package net.kuujo.vertigo.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * An openable type.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The openable type.
 */
public interface Openable<T extends Openable<T>> {

  /**
   * Opens the object.
   *
   * @return The opened object.
   */
  T open();

  /**
   * Opens the object.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The opened object.
   */
  T open(Handler<AsyncResult<Void>> doneHandler);

}
