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

/**
 * Network-like configuration builder.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface NetworkLikeBuilder<T> {

  /**
   * Returns a component builder for a uniquely identified new component.
   *
   * @return A new component builder.
   */
  ComponentBuilder component();

  /**
   * Returns a component builder for a new or existing component.
   *
   * @param id The unique ID of the component.
   * @return The component builder.
   */
  ComponentBuilder component(String id);

  /**
   * Returns a connection builder.
   *
   * @return A new connection builder.
   */
  ConnectionSourceBuilder connect();

  /**
   * Returns a connection builder for the given source component. If the component doesn't already exist it
   * will be created.
   *
   * @param component The source component identifier.
   * @return The sourc component connection builder.
   */
  ConnectionSourceComponentBuilder connect(String component);

}
