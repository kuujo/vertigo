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
   * <code>name</code> is a string indicating the network unique component name. This
   * name is used as the basis for generating unique event bus addresses.
   */
  public static final String COMPONENT_NAME = "name";

  /**
   * <code>main</code> is a string indicating the verticle main. This field is required
   * for all components.
   */
  public static final String COMPONENT_MAIN = "main";

  /**
   * <code>config</code> is an object defining the configuration to pass to each instance
   * of the component. If no configuration is provided then an empty configuration will be
   * passed to component instances.
   */
  public static final String COMPONENT_CONFIG = "config";

  /**
   * <code>partitions</code> is a number indicating the number of partitions of the
   * component to deploy. Defaults to <code>1</code>
   */
  public static final String COMPONENT_PARTITIONS = "partitions";

  /**
   * <code>worker</code> is a boolean indicating whether this verticle should be deployed
   * as a worker verticle. Defaults to <code>false</code>
   */
  public static final String COMPONENT_WORKER = "worker";

  /**
   * <code>multi-threaded</code> is a boolean indicating whether a worker verticle is
   * multi-threaded. This option only applies to verticles where <code>worker</code> is
   * <code>true</code>. Defaults to <code>false</code>
   */
  public static final String COMPONENT_MULTI_THREADED = "multi-threaded";

  /**
   * <code>stateful</code> is a boolean indicating whether a component is stateful.
   * Stateful components support stateful recovery managed by Vertigo.
   */
  public static final String COMPONENT_STATEFUL = "stateful";

  /**
   * <code>replication</code> is a number indicating the replication factor of a
   * stateful component. This is the number of replicas for the component.
   */
  public static final String COMPONENT_REPLICATION = "replication";

  /**
   * <code>resources</code> is a list of resources that should be distributed with this
   * component when clustering.
   */
  public static final String COMPONENT_RESOURCES = "resources";

  /**
   * Returns the component name.
   *
   * @return The component name.
   */
  String getName();

  /**
   * Sets the component name.
   *
   * @param name The component name.
   * @return The component definition.
   */
  ComponentDefinition setName(String name);

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
   * Returns whether the component is stateful.
   *
   * @return Indicates whether the component is stateful.
   */
  boolean isStateful();

  /**
   * Sets whether the component is stateful.
   *
   * @param stateful Whether the component is stateful.
   * @return The component definition.
   */
  ComponentDefinition setStateful(boolean stateful);

  /**
   * Returns the component replication factor.
   *
   * @return The component replication factor.
   */
  int getReplication();

  /**
   * Sets the component replication factor.
   *
   * @param replication The component replication factor.
   * @return The component definition.
   */
  ComponentDefinition setReplication(int replication);

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
