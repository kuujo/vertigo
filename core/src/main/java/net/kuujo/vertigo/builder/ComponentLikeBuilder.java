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
package net.kuujo.vertigo.builder;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ComponentLikeBuilder<T> {

  /**
   * Sets the component identifier.
   *
   * @param identifier The component verticle identifier.
   * @return The component builder.
   */
  T identifier(String identifier);

  /**
   * Sets the component configuration.
   *
   * @param config The component configuration.
   * @return The component builder.
   */
  T config(JsonObject config);

  /**
   * Sets the component as a worker verticle.
   *
   * @return The component builder.
   */
  T worker();

  /**
   * Sets whether the component is a worker verticle.
   *
   * @param worker Whether the component is a worker verticle.
   * @return The component builder.
   */
  T worker(boolean worker);

  /**
   * Sets the component as multi-threaded.
   *
   * @return The component builder.
   */
  T multiThreaded();

  /**
   * Sets whether the component is a multi-threaded verticle.
   *
   * @param multiThreaded Whether the component verticle is multi-threaded.
   * @return The component builder.
   */
  T multiThreaded(boolean multiThreaded);

}
