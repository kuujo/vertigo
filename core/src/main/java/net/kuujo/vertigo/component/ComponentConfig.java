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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.TypeConfig;
import net.kuujo.vertigo.io.InputConfig;
import net.kuujo.vertigo.io.OutputConfig;
import net.kuujo.vertigo.network.Network;

import java.util.Collection;
import java.util.Set;

/**
 * Component info.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface ComponentConfig extends TypeConfig {

  /**
   * {@code name} is a string indicating the network unique component name. This
   * name is used as the basis for generating unique event bus addresses.
   */
  public static final String COMPONENT_NAME = "name";

  /**
   * {@code identifier} is a string indicating the verticle identifier. This field is required
   * for all components.
   */
  public static final String COMPONENT_IDENTIFIER = "identifier";

  /**
   * {@code config} is an object defining the configuration to pass to each instance
   * of the component. If no configuration is provided then an empty configuration will be
   * passed to component instances.
   */
  public static final String COMPONENT_CONFIG = "config";

  /**
   * {@code worker} is a boolean indicating whether this verticle should be deployed
   * as a worker verticle. Defaults to {@code false}
   */
  public static final String COMPONENT_WORKER = "worker";

  /**
   * {@code multi-threaded} is a boolean indicating whether a worker verticle is
   * multi-threaded. This option only applies to verticles where {@code worker} is
   * {@code true}. Defaults to {@code false}
   */
  public static final String COMPONENT_MULTI_THREADED = "multi-threaded";

  /**
   * {@code stateful} is a boolean indicating whether a component is stateful. Defaults
   * to {@code false}
   */
  public static final String COMPONENT_STATEFUL = "stateful";

  /**
   * {@code replicas} is an integer indicating the number of replicas to deploy
   * for components where {@code stateful} is {@code true}. Defaults to {@code 1}
   */
  public static final String COMPONENT_REPLICAS = "replicas";

  /**
   * {@code resources} is a list of resources that should be distributed with this
   * component when clustering.
   */
  public static final String COMPONENT_RESOURCES = "resources";

  /**
   * {@code input} is a list of component input port configurations.
   */
  public static final String COMPONENT_INPUT = "input";

  /**
   * {@code output} is a list of component output port configurations.
   */
  public static final String COMPONENT_OUTPUT = "output";

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
   * @return The component info.
   */
  @Fluent
  ComponentConfig setName(String name);

  /**
   * Returns the component verticle identifier.
   *
   * @return The component verticle identifier.
   */
  String getIdentifier();

  /**
   * Sets the component verticle identifier.
   *
   * @param identifier The component verticle identifier.
   * @return The component info.
   */
  @Fluent
  ComponentConfig setIdentifier(String identifier);

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
   * @return The component info.
   */
  @Fluent
  ComponentConfig setConfig(JsonObject config);

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
   * @return The component info.
   */
  @Fluent
  ComponentConfig setWorker(boolean worker);

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
   * @return The component info.
   */
  @Fluent
  ComponentConfig setMultiThreaded(boolean multiThreaded);

  /**
   * Returns the component input info.
   *
   * @return The component input info.
   */
  InputConfig getInput();

  /**
   * Sets the component input info.
   *
   * @param input The component input info.
   * @return The component info.
   */
  @Fluent
  ComponentConfig setInput(InputConfig input);

  /**
   * Returns the component output info.
   *
   * @return The component output info.
   */
  OutputConfig getOutput();

  /**
   * Sets the component output info.
   *
   * @param output The component output info.
   * @return The component info.
   */
  @Fluent
  ComponentConfig setOutput(OutputConfig output);

  /**
   * Sets whether the component is stateful.
   *
   * @param stateful Whether the component is stateful.
   * @return The component info.
   */
  @Fluent
  ComponentConfig setStateful(boolean stateful);

  /**
   * Returns whether the component is stateful.
   *
   * @return Whether the component is stateful.
   */
  boolean isStateful();

  /**
   * Sets the component replication factor.
   *
   * @param replication The component replication factor.
   * @return The component info.
   */
  @Fluent
  ComponentConfig setReplicas(int replication);

  /**
   * Returns the component replication factor.
   *
   * @return The component replication factor.
   */
  int getReplicas();

  /**
   * Adds a resource to the component.
   *
   * @param resource The resource to add.
   * @return The component options.
   */
  @Fluent
  ComponentConfig addResource(String resource);

  /**
   * Removes a resource from the component.
   *
   * @param resource The resource to remove.
   * @return The component options.
   */
  @Fluent
  ComponentConfig removeResource(String resource);

  /**
   * Sets the component resources.
   *
   * @param resources The component resources.
   * @return The component options.
   */
  @Fluent
  ComponentConfig setResources(String... resources);

  /**
   * Sets the component resources.
   *
   * @param resources The component resources.
   * @return The component options.
   */
  @Fluent
  ComponentConfig setResources(Collection<String> resources);

  /**
   * Returns the component resources.
   *
   * @return The component resources.
   */
  Set<String> getResources();

  /**
   * Sets the parent network.
   *
   * @param network The parent network.
   * @return The component info.
   */
  ComponentConfig setNetwork(Network network);

  /**
   * Returns the parent network.
   *
   * @return The parent network.
   */
  Network getNetwork();

}
