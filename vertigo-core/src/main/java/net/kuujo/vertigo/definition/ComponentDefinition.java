/*
* Copyright 2013 the original author or authors.
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
package net.kuujo.vertigo.definition;

import net.kuujo.vertigo.filter.Filter;
import net.kuujo.vertigo.grouping.Grouping;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A component definition.
 *
 * @author Jordan Halterman
 */
public class ComponentDefinition implements Definition {

  private JsonObject definition = new JsonObject();

  private static final int DEFAULT_NUM_WORKERS = 1;

  private static final long DEFAULT_HEARTBEAT_INTERVAL = 2500;

  public static final String VERTICLE = "verticle";
  public static final String MODULE = "module";

  public ComponentDefinition() {
  }

  public ComponentDefinition(JsonObject json) {
    definition = json;
  }

  /**
   * Gets the component type.
   */
  public String type() {
    return definition.getString("type", VERTICLE);
  }

  /**
   * Sets the component type.
   *
   * @param type
   *   The component type.
   * @return
   *   The called component definition.
   */
  public ComponentDefinition setType(String type) {
    definition.putString("type", type);
    return this;
  }

  /**
   * Gets the component name.
   */
  public String name() {
    return definition.getString("name");
  }

  /**
   * Sets the component name.
   *
   * @param name
   *   The component name.
   */
  public ComponentDefinition setName(String name) {
    definition.putString("name", name);
    return this;
  }

  /**
   * Gets the component main.
   */
  public String main() {
    return definition.getString("main");
  }

  /**
   * Sets the component main. This is a string reference to the verticle
   * to be run when a component worker is started.
   *
   * @param main
   *   The component main.
   * @return
   *   The called component definition.
   */
  public ComponentDefinition setMain(String main) {
    definition.putString("main", main);
    return this;
  }

  /**
   * Gets the module name.
   */
  public String module() {
    return definition.getString("module");
  }

  /**
   * Sets the module name.
   *
   * @param moduleName
   *   A module name.
   * @return
   *   The called component definition.
   */
  public ComponentDefinition setModule(String moduleName) {
    definition.putString("module", moduleName);
    return this;
  }

  /**
   * Gets the component configuration.
   *
   * @return
   *   The component configuration.
   */
  public JsonObject config() {
    JsonObject config = definition.getObject("config");
    if (config == null) {
      config = new JsonObject();
    }
    return config;
  }

  /**
   * Sets the component configuration.
   *
   * @param config
   *   The component configuration.
   * @return
   *   The called component definition.
   */
  public ComponentDefinition setConfig(JsonObject config) {
    definition.putObject("config", config);
    return this;
  }

  /**
   * Sets the component worker grouping.
   *
   * @param grouping
   *   A grouping definition.
   * @return
   *   The called component definition.
   */
  public ComponentDefinition groupBy(Grouping grouping) {
    definition.putObject("grouping", new JsonObject().putString("grouping", grouping.getClass().getName())
        .putObject("definition", grouping.serialize()));
    return this;
  }

  /**
   * Adds a filter to the component.
   *
   * @param filter
   *   The filter to add.
   * @return
   *   The called component definition.
   */
  public ComponentDefinition filterBy(Filter filter) {
    JsonArray filters = definition.getArray("filters");
    if (filters == null) {
      filters = new JsonArray();
    }
    filters.add(new JsonObject().putString("filter", filter.getClass().getName())
        .putObject("definition", filter.serialize()));
    return this;
  }

  /**
   * Sets the number of component workers.
   *
   * @param workers
   *   The number of component workers.
   * @return
   *   The called component definition.
   */
  public ComponentDefinition setWorkers(int workers) {
    definition.putNumber("workers", workers);
    return this;
  }

  /**
   * Gets the number of component workers.
   */
  public int workers() {
    return definition.getInteger("workers", DEFAULT_NUM_WORKERS);
  }

  /**
   * Sets the component worker heartbeat interval.
   *
   * @param interval
   *   A heartbeat interval.
   * @return
   *   The called component definition.
   */
  public ComponentDefinition setHeartbeatInterval(long interval) {
    definition.putNumber("heartbeat", interval);
    return this;
  }

  /**
   * Gets the component heartbeat interval.
   *
   * @return
   *   A component heartbeat interval.
   */
  public long heartbeatInterval() {
    return definition.getLong("heartbeat", DEFAULT_HEARTBEAT_INTERVAL);
  }

  /**
   * Adds a connection to a component definition.
   */
  private ComponentDefinition addDefinition(ComponentDefinition definition) {
    JsonObject connections = this.definition.getObject("connections");
    if (connections == null) {
      connections = new JsonObject();
      this.definition.putObject("connections", connections);
    }
    if (!connections.getFieldNames().contains(definition.name())) {
      connections.putObject(definition.name(), definition.serialize());
    }
    return definition;
  }

  /**
   * Adds a channel to a component.
   *
   * @param definition
   *   The component definition.
   * @return
   *   The component definition.
   */
  public ComponentDefinition to(ComponentDefinition definition) {
    return addDefinition(definition);
  }

  /**
   * Adds a channel to a verticle component.
   *
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toVerticle() {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.VERTICLE));
  }

  /**
   * Adds a channel to a verticle component.
   *
   * @param name
   *   The component name.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toVerticle(String name) {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.VERTICLE).setName(name));
  }

  /**
   * Adds a channel to a verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toVerticle(String name, String main) {
    return toVerticle(name, main, new JsonObject(), 1);
  }

  /**
   * Adds a channel to a verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toVerticle(String name, String main, JsonObject config) {
    return toVerticle(name, main, config, 1);
  }

  /**
   * Adds a channel to a verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toVerticle(String name, String main, int workers) {
    return toVerticle(name, main, new JsonObject(), workers);
  }

  /**
   * Adds a channel to a verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toVerticle(String name, String main, JsonObject config, int workers) {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.VERTICLE).setName(name).setMain(main).setConfig(config).setWorkers(workers));
  }

  /**
   * Adds a channel to a module component.
   *
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toModule() {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.MODULE));
  }

  /**
   * Adds a channel to a module component.
   *
   * @param name
   *   The component name.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toModule(String name) {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.MODULE).setName(name));
  }

  /**
   * Adds a channel to a module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toModule(String name, String moduleName) {
    return toModule(name, moduleName, new JsonObject(), 1);
  }

  /**
   * Adds a channel to a module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toModule(String name, String moduleName, JsonObject config) {
    return toModule(name, moduleName, config, 1);
  }

  /**
   * Adds a channel to a module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toModule(String name, String moduleName, int workers) {
    return toModule(name, moduleName, new JsonObject(), workers);
  }

  /**
   * Adds a channel to a module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public ComponentDefinition toModule(String name, String moduleName, JsonObject config, int workers) {
    return addDefinition(new ComponentDefinition().setType(ComponentDefinition.MODULE).setName(name).setModule(moduleName).setConfig(config).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

}
