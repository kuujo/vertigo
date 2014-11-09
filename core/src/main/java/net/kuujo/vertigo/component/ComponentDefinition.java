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
package net.kuujo.vertigo.component;

import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Set;

/**
 * Component definition.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ComponentDefinition {

  /**
   * Returns the component ID.
   *
   * @return The component ID.
   */
  String getId();

  /**
   * Sets the component ID.
   *
   * @param id The component ID.
   * @return The component definition.
   */
  ComponentDefinition setId(String id);

  /**
   * Returns the component main.
   *
   * @return The component main.
   */
  String getMain();

  /**
   * Sets the component main.
   *
   * @param main The component main.
   * @return The component definition.
   */
  ComponentDefinition setMain(String main);

  /**
   * Returns the component configuration.
   *
   * @return The component configuration.
   */
  JsonObject getConfig();

  /**
   * Sets the component configuration.
   *
   * @param config The component configuration.
   * @return The component definition.
   */
  ComponentDefinition setConfig(JsonObject config);

  /**
   * Returns the number of component partitions.
   *
   * @return The number of component partitions.
   */
  int getPartitions();

  /**
   * Sets the number of component partitions.
   *
   * @param partitions The number of component partitions.
   * @return The component definition.
   */
  ComponentDefinition setPartitions(int partitions);

  /**
   * Returns whether the component is a worker.
   *
   * @return Indicates whether the component is a worker.
   */
  boolean isWorker();

  /**
   * Sets whether the component is a worker.
   *
   * @param worker Whether the component is a worker.
   * @return The component definition.
   */
  ComponentDefinition setWorker(boolean worker);

  /**
   * Returns whether the component is multi-threaded.
   *
   * @return Indicates whether the component is multi-threaded.
   */
  boolean isMultiThreaded();

  /**
   * Sets whether the component is multi-threaded.
   *
   * @param multiThreaded Whether the component is multi-threaded.
   * @return The component definition.
   */
  ComponentDefinition setMultiThreaded(boolean multiThreaded);

  /**
   * Adds a resource to the component.
   *
   * @param resource The resource to add.
   * @return The component options.
   */
  ComponentDefinition addResource(String resource);

  /**
   * Removes a resource from the component.
   *
   * @param resource The resource to remove.
   * @return The component options.
   */
  ComponentDefinition removeResource(String resource);

  /**
   * Sets the component resources.
   *
   * @param resources The component resources.
   * @return The component options.
   */
  ComponentDefinition setResources(String... resources);

  /**
   * Sets the component resources.
   *
   * @param resources The component resources.
   * @return The component options.
   */
  ComponentDefinition setResources(Collection<String> resources);

  /**
   * Returns the component resources.
   *
   * @return The component resources.
   */
  Set<String> getResources();


}
