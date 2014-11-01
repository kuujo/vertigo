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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.component.impl.ComponentInfoImpl;
import net.kuujo.vertigo.network.NetworkInfo;

import java.util.Collection;
import java.util.List;

/**
 * A component context which contains information regarding each component instance within
 * a single network component.<p>
 *
 * Contexts are immutable as they are constructed once a network has been deployed.
 * The component context is not actually used by any Vertigo object, but is a
 * wrapper around multiple {@link InstanceInfo} instances, with each instance
 * representing an instance of the component - a Vert.x verticle or module.<p>
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface ComponentInfo extends TypeInfo<ComponentInfo> {

  /**
   * Returns a new component info builder.
   *
   * @return A new component info builder.
   */
  static Builder builder() {
    return new ComponentInfoImpl.Builder();
  }

  /**
   * Returns a new component info builder.
   *
   * @param component An existing component info object to wrap.
   * @return A component info builder wrapper.
   */
  static TypeInfo.Builder<ComponentInfo> builder(ComponentInfo component) {
    return new ComponentInfoImpl.Builder((ComponentInfoImpl) component);
  }

  /**
   * Returns the component name.
   *
   * @return The component name.
   */
  String name();

  /**
   * Returns the verticle main.
   *
   * @return The verticle main.
   */
  String main();

  /**
   * Returns the verticle component.
   *
   * @return The verticle component instance.
   */
  Component component();

  /**
   * Gets the component configuration.
   * 
   * @return The component configuration.
   */
  JsonObject config();

  /**
   * Gets a list of all component instance contexts.
   * 
   * @return A list of component instance contexts.
   */
  List<InstanceInfo> instances();

  /**
   * Returns the number of component instances.
   * 
   * @return The number of component instances.
   */
  int numInstances();

  /**
   * Gets a component instance context by instance ID.
   * 
   * @param instanceNumber The instance number.
   * @return A component instance or <code>null</code> if the instance doesn't exist.
   */
  InstanceInfo instance(int instanceNumber);

  /**
   * Gets a component instance context by instance id.
   * 
   * @param id The instance id.
   * @return A component instance or <code>null</code> if the instance doesn't exist.
   */
  InstanceInfo instance(String id);

  /**
   * Returns a boolean indicating whether the verticle is a worker verticle.
   *
   * @return Indicates whether the verticle is a worker verticle.
   */
  boolean isWorker();

  /**
   * Returns a boolean indicating whether the verticle is a worker and is multi-threaded.
   * If the verticle is not a worker then <code>false</code> will be returned.
   *
   * @return Indicates whether the verticle is a worker and is multi-threaded.
   */
  boolean isMultiThreaded();

  /**
   * Returns the parent network context.
   * 
   * @return The parent network context.
   */
  NetworkInfo network();

  /**
   * Component info builder.
   */
  public static interface Builder extends TypeInfo.Builder<ComponentInfo> {

    /**
     * Sets the component name.
     *
     * @param name The component name.
     * @return The component info builder.
     */
    Builder setName(String name);

    /**
     * Sets the component main.
     *
     * @param main The component main.
     * @return The component info builder.
     */
    Builder setMain(String main);

    /**
     * Sets the component instance.
     *
     * @param component The component instance.
     * @return The component info builder.
     */
    Builder setComponent(Component component);

    /**
     * Sets the component configuration.
     *
     * @param config The component configuration.
     * @return The component info builder.
     */
    Builder setConfig(JsonObject config);

    /**
     * Sets whether the component should be deployed as a worker.
     *
     * @param isWorker Indicates whether to deploy the component as a worker.
     * @return The component info builder.
     */
    Builder setWorker(boolean isWorker);

    /**
     * Sets whether the component should be deployed in a multi-threaded context.
     *
     * @param isMultiThreaded Indicates whether to deploy the component as multi-threaded.
     * @return The component info builder.
     */
    Builder setMultiThreaded(boolean isMultiThreaded);

    /**
     * Sets the component options.
     *
     * @param options Component options.
     * @return The component builder.
     */
    Builder setOptions(ComponentOptions options);

    /**
     * Adds an instance to the component info.
     *
     * @param instance The instance info to add.
     * @return The component info builder.
     */
    Builder addInstance(InstanceInfo instance);

    /**
     * Removes an instance from the component info.
     *
     * @param instance The instance info to remove.
     * @return The component info builder.
     */
    Builder removeInstance(InstanceInfo instance);

    /**
     * Sets the component instances.
     *
     * @param instances A collection of instance info to add.
     * @return The component info builder.
     */
    Builder setInstances(InstanceInfo... instances);

    /**
     * Sets the component instances.
     *
     * @param instances A collection of instance info to add.
     * @return The component info builder.
     */
    Builder setInstances(Collection<InstanceInfo> instances);

    /**
     * Clears all component instance info.
     *
     * @return The component info builder.
     */
    Builder clearInstances();

    /**
     * Sets the parent network info.
     *
     * @param network The parent network info.
     * @return The component info builder.
     */
    Builder setNetwork(NetworkInfo network);
  }

}
