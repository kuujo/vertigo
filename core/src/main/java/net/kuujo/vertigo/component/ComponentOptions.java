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

import io.vertx.codegen.annotations.Options;
import io.vertx.core.json.JsonObject;

/**
 * Components are synonymous with Vert.x verticles.
 * Each component can have its own configuration and any number
 * of instances.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@Options
public interface ComponentOptions {

  /**
   * <code>name</code> is a string indicating the network unique component name. This
   * name is used as the basis for generating unique event bus addresses.
   */
  public static final String COMPONENT_NAME = "name";

  /**
   * <code>main</code> is a string indicating the verticle main. This field is required
   * for all verticle components.
   */
  public static final String VERTICLE_MAIN = "main";

  /**
   * <code>config</code> is an object defining the configuration to pass to each instance
   * of the component. If no configuration is provided then an empty configuration will be
   * passed to component instances.
   */
  public static final String COMPONENT_CONFIG = "config";

  /**
   * <code>instances</code> is a number indicating the number of instances of the
   * component to deploy. Defaults to <code>1</code>
   */
  public static final String COMPONENT_NUM_INSTANCES = "instances";

  /**
   * <code>worker</code> is a boolean indicating whether this verticle should be deployed
   * as a worker verticle. Defaults to <code>false</code>
   */
  public static final String VERTICLE_IS_WORKER = "worker";

  /**
   * <code>multi-threaded</code> is a boolean indicating whether a worker verticle is
   * multi-threaded. This option only applies to verticles where <code>worker</code> is
   * <code>true</code>. Defaults to <code>false</code>
   */
  public static final String VERTICLE_IS_MULTI_THREADED = "multi-threaded";

  /**
   * <code>hooks</code> is an array defining component hooks. Each element in the array
   * must be an object containing a <code>hook</code> field which indicates the hook
   * class name.
   */
  public static final String COMPONENT_HOOKS = "hooks";

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
   * @return The component configuration.
   */
  ComponentOptions setName(String name);

  /**
   * Sets the verticle main.
   *
   * @param main The verticle main.
   * @return The verticle configuration.
   */
  ComponentOptions setMain(String main);

  /**
   * Gets the verticle main.
   *
   * @return The verticle main.
   */
  String getMain();

  /**
   * Returns the component configuration.
   * 
   * @return The component configuration.
   */
  JsonObject getConfig();

  /**
   * Sets the component configuration.
   * <p>
   * 
   * This configuration will be passed to component implementations as the verticle or
   * module configuration when the component is started.
   * 
   * @param config The component configuration.
   * @return The component configuration.
   */
  ComponentOptions setConfig(JsonObject config);

  /**
   * Returns the number of component instances to deploy within the network.
   * 
   * @return The number of component instances.
   */
  int getInstances();

  /**
   * Sets the number of component instances to deploy within the network.
   * 
   * @param instances The number of component instances.
   * @return The component configuration.
   */
  ComponentOptions setInstances(int instances);

  /**
   * Sets the verticle worker option.
   *
   * @param isWorker Indicates whether the verticle should be deployed as a worker.
   * @return The verticle configuration.
   */
  ComponentOptions setWorker(boolean isWorker);

  /**
   * Returns a boolean indicating whether the verticle is a worker.
   *
   * @return Indicates whether the verticle is a worker.
   */
  boolean isWorker();

  /**
   * Sets the verticle multi-threaded option. This option only applies to worker
   * verticles.
   *
   * @param isMultiThreaded Indicates whether the worker verticle is multi-threaded.
   * @return The verticle configuration.
   */
  ComponentOptions setMultiThreaded(boolean isMultiThreaded);

  /**
   * Returns a boolean indicating whether the verticle is a worker and is multi-threaded.
   *
   * @return Indicates whether the verticle is a worker and is multi-threaded. If the
   *         verticle is not a worker verticle then <code>false</code> will be returned.
   */
  boolean isMultiThreaded();

}
