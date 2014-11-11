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
package net.kuujo.vertigo;

import io.vertx.core.shareddata.Shareable;

/**
 * Immutable configuration information for Vertigo types.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface TypeContext<T extends TypeContext<T>> extends Shareable {

  /**
   * Returns a formatted representation of the type context.
   *
   * @param formatted Whether to format the type context.
   * @return A formatted representation of the type context.
   */
  String toString(boolean formatted);

  /**
   * Type context builder.
   *
   * @param <T> The type built by the builder.
   */
  public static interface Builder<T extends Builder<T, U>, U extends TypeContext<U>> {

    /**
     * Builds the instance.
     *
     * @return The built type context instance.
     */
    U build();

  }

}
