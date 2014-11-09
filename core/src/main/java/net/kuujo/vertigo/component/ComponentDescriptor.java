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

import java.util.List;
import java.util.Set;

/**
 * Component descriptor.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ComponentDescriptor {

  /**
   * <code>id</code> is a string indicating the network unique component id. This
   * name is used as the basis for generating unique event bus addresses.
   */
  public static final String COMPONENT_ID = "id";

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
   * Returns the unique component ID.
   *
   * @return The unique component ID.
   */
  String id();

  /**
   * Returns the component main.
   *
   * @return The component main.
   */
  String main();

  /**
   * Returns the component configuration.
   *
   * @return The component configuration.
   */
  JsonObject config();

  /**
   * Returns the default number of component partitions.
   *
   * @return The default number of component partitions.
   */
  int partitions();

  /**
   * Returns whether the component is a worker.
   *
   * @return Whether the component is a worker.
   */
  boolean worker();

  /**
   * Returns whether the component is multi-threaded.
   *
   * @return Whether the component is multi-threaded.
   */
  boolean multiThreaded();

  /**
   * Returns a set of explicitly defined component input ports.
   *
   * @return A set of explicitly defined component input ports.
   */
  Set<String> input();

  /**
   * Returns a set of explicitly defined component output ports.
   *
   * @return A set of explicitly defined component output ports.
   */
  Set<String> output();

  /**
   * Returns a list of component resources.
   *
   * @return A list of component resources.
   */
  List<String> resources();

}
