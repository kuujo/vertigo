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
import net.kuujo.vertigo.TypeContext;
import net.kuujo.vertigo.component.impl.ComponentContextImpl;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.network.NetworkContext;

import java.util.Collection;
import java.util.Set;

/**
 * A component context which contains information regarding each component partition within
 * a single network component.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface ComponentContext extends TypeContext<ComponentContext> {

  /**
   * Returns a new component context builder.
   *
   * @return A new component context builder.
   */
  static Builder builder() {
    return new ComponentContextImpl.Builder();
  }

  /**
   * Returns a new component context builder.
   *
   * @param component An existing component context object to wrap.
   * @return A component context builder wrapper.
   */
  static Builder builder(ComponentContext component) {
    return new ComponentContextImpl.Builder((ComponentContextImpl) component);
  }

  /**
   * Returns the component name.
   *
   * @return The component name.
   */
  String name();

  /**
   * Returns the component address.
   *
   * @return The component address.
   */
  String address();

  /**
   * Returns the verticle identifier.
   *
   * @return The verticle identifier.
   */
  String main();

  /**
   * Returns the verticle component.
   *
   * @return The verticle component partition.
   */
  Component component();

  /**
   * Gets the component configuration.
   * 
   * @return The component configuration.
   */
  JsonObject config();

  /**
   * Returns a boolean indicating whether the verticle is a worker verticle.
   *
   * @return Indicates whether the verticle is a worker verticle.
   */
  boolean worker();

  /**
   * Returns a boolean indicating whether the verticle is a worker and is multi-threaded.
   * If the verticle is not a worker then <code>false</code> will be returned.
   *
   * @return Indicates whether the verticle is a worker and is multi-threaded.
   */
  boolean multiThreaded();

  /**
   * Returns a boolean indicating whether the component is stateful.
   *
   * @return Indicates whether the component is stateful.
   */
  boolean stateful();

  /**
   * Returns the number of component replicas.
   *
   * @return The number of component replicas.
   */
  int replicas();

  /**
   * Returns the component resources.
   *
   * @return The component resources.
   */
  Set<String> resources();

  /**
   * Returns the component input context.
   *
   * @return The component input context.
   */
  InputContext input();

  /**
   * Returns the component output context.
   *
   * @return The component output context.
   */
  OutputContext output();

  /**
   * Returns the parent network context.
   * 
   * @return The parent network context.
   */
  NetworkContext network();

  /**
   * Component context builder.
   */
  public static interface Builder extends TypeContext.Builder<Builder, ComponentContext> {

    /**
     * Sets the component name.
     *
     * @param name The component name.
     * @return The component context builder.
     */
    Builder setName(String name);

    /**
     * Sets the component address.
     *
     * @param address The component address.
     * @return The component context builder.
     */
    Builder setAddress(String address);

    /**
     * Sets the component verticle identifier.
     *
     * @param identifier The component verticle identifier.
     * @return The component context builder.
     */
    Builder setIdentifier(String identifier);

    /**
     * Sets the component configuration.
     *
     * @param config The component configuration.
     * @return The component context builder.
     */
    Builder setConfig(JsonObject config);

    /**
     * Sets whether the component should be deployed as a worker.
     *
     * @param isWorker Indicates whether to deploy the component as a worker.
     * @return The component context builder.
     */
    Builder setWorker(boolean isWorker);

    /**
     * Sets whether the component should be deployed in a multi-threaded context.
     *
     * @param isMultiThreaded Indicates whether to deploy the component as multi-threaded.
     * @return The component context builder.
     */
    Builder setMultiThreaded(boolean isMultiThreaded);

    /**
     * Sets whether the component is stateful.
     *
     * @param isStateful Whether the component is stateful.
     * @return The component context builder.
     */
    Builder setStateful(boolean isStateful);

    /**
     * Sets the number of component replicas.
     *
     * @param replicas The number of component replicas.
     * @return The component context builder.
     */
    Builder setReplicas(int replicas);

    /**
     * Sets the component input context.
     *
     * @param input The component input context.
     * @return The component context builder.
     */
    Builder setInput(InputContext input);

    /**
     * Sets the component output context.
     *
     * @param output The component output context.
     * @return The component context builder.
     */
    Builder setOutput(OutputContext output);

    /**
     * Adds a resource to the component.
     *
     * @param resource The resource to add.
     * @return The component context builder.
     */
    Builder addResource(String resource);

    /**
     * Removes a resource from the component.
     *
     * @param resource The resource to remove.
     * @return The component context builder.
     */
    Builder removeResource(String resource);

    /**
     * Sets the component resources.
     *
     * @param resources The component resources.
     * @return The component context builder.
     */
    Builder setResources(String... resources);

    /**
     * Sets the component resources.
     *
     * @param resources The component resources.
     * @return The component context builder.
     */
    Builder setResources(Collection<String> resources);

    /**
     * Sets the parent network context.
     *
     * @param network The parent network context.
     * @return The component context builder.
     */
    Builder setNetwork(NetworkContext network);
  }

}
