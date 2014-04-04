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
package net.kuujo.vertigo.context;

import net.kuujo.vertigo.util.Observable;
import net.kuujo.vertigo.util.serializer.JsonSerializable;

import org.vertx.java.core.json.JsonObject;

/**
 * Base context.
 * 
 * @author Jordan Halterman
 */
public interface Context<T extends Context<T>> extends Observable<T>, JsonSerializable {

  /**
   * Returns the context address.
   *
   * @return The context address.
   */
  String address();

  /**
   * Returns all component options.
   *
   * @return A json object of component options.
   */
  JsonObject options();

  /**
   * Returns an option value.
   *
   * @param option The option name.
   * @return The option value.
   */
  @SuppressWarnings("hiding")
  <T> T option(String option);

  /**
   * Returns an option value.
   *
   * @param option The option name.
   * @param defaultValue A default value for the option.
   * @return The option value.
   */
  @SuppressWarnings("hiding")
  <T> T option(String option, T defaultValue);

}
