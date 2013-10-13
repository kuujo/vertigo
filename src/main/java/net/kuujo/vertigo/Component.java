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
package net.kuujo.vertigo;

import net.kuujo.vertigo.filter.Filter;
import net.kuujo.vertigo.grouping.Grouping;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A network component definition.
 *
 * @author Jordan Halterman
 */
public class Component implements Serializable<JsonObject> {

  private JsonObject definition = new JsonObject();

  private Network network;

  private static final int DEFAULT_NUM_WORKERS = 1;

  private static final long DEFAULT_HEARTBEAT_INTERVAL = 2500;

  public static final String VERTICLE = "verticle";
  public static final String MODULE = "module";

  public Component(String name) {
    definition.putString("name", name);
  }

  private Component(JsonObject json) {
    definition = json;
  }

  /**
   * Creates a component from JSON.
   *
   * @param json
   *   A JSON representation of the component.
   * @return
   *   A new component instance.
   */
  public static Component fromJson(JsonObject json) {
    return new Component(json);
  }

  public Component setNetwork(Network network) {
    this.network = network;
    return this;
  }

  /**
   * Gets the component name.
   */
  public String name() {
    return definition.getString("name");
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
  public Component setType(String type) {
    definition.putString("type", type);
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
  public Component setMain(String main) {
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
  public Component setModule(String moduleName) {
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
  public Component setConfig(JsonObject config) {
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
  public Component groupBy(Grouping grouping) {
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
  public Component filterBy(Filter filter) {
    JsonArray filters = definition.getArray("filters");
    if (filters == null) {
      filters = new JsonArray();
      definition.putArray("filters", filters);
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
  public Component setWorkers(int workers) {
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
  public Component setHeartbeatInterval(long interval) {
    definition.putNumber("heartbeat", interval);
    return this;
  }

  /**
   * Gets the component heartbeat interval.
   *
   * @return
   *   A component heartbeat interval.
   */
  public long getHeartbeatInterval() {
    return definition.getLong("heartbeat", DEFAULT_HEARTBEAT_INTERVAL);
  }

  /**
   * Adds a connection to a component definition.
   */
  Component addConnection(Component definition) {
    JsonArray connections = this.definition.getArray("connections");
    if (connections == null) {
      connections = new JsonArray();
      this.definition.putArray("connections", connections);
    }
    if (!connections.contains(definition.name())) {
      connections.add(definition.name());
    }
    if (network != null) {
      network.addDefinition(definition);
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
  public Component to(Component definition) {
    return addConnection(definition);
  }

  /**
   * Adds a channel to a verticle component.
   *
   * @param name
   *   The component name.
   * @return
   *   A new component definition instance.
   */
  public Component toVerticle(String name) {
    return addConnection(new Component(name).setType(Component.VERTICLE));
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
  public Component toVerticle(String name, String main) {
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
  public Component toVerticle(String name, String main, JsonObject config) {
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
  public Component toVerticle(String name, String main, int workers) {
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
  public Component toVerticle(String name, String main, JsonObject config, int workers) {
    return addConnection(new Component(name).setType(Component.VERTICLE).setMain(main).setConfig(config).setWorkers(workers));
  }

  /**
   * Adds a channel to a module component.
   *
   * @param name
   *   The component name.
   * @return
   *   A new component definition instance.
   */
  public Component toModule(String name) {
    return addConnection(new Component(name).setType(Component.MODULE));
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
  public Component toModule(String name, String moduleName) {
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
  public Component toModule(String name, String moduleName, JsonObject config) {
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
  public Component toModule(String name, String moduleName, int workers) {
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
  public Component toModule(String name, String moduleName, JsonObject config, int workers) {
    return addConnection(new Component(name).setType(Component.MODULE).setModule(moduleName).setConfig(config).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

}
