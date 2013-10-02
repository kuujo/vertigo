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
package net.kuujo.vitis.definition;

import net.kuujo.vitis.Serializeable;

import org.vertx.java.core.json.JsonObject;

/**
 * A node definition.
 *
 * @author Jordan Halterman
 */
public class NodeDefinition implements Serializeable<JsonObject> {

  private JsonObject definition = new JsonObject();

  private static final int DEFAULT_NUM_WORKERS = 1;

  private static final long DEFAULT_HEARTBEAT_INTERVAL = 2500;

  public static final String VERTICLE = "verticle";
  public static final String MODULE = "module";

  public NodeDefinition() {
  }

  public NodeDefinition(JsonObject json) {
    definition = json;
  }

  /**
   * Gets the node type.
   */
  public String type() {
    return definition.getString("type", VERTICLE);
  }

  /**
   * Sets the node type.
   *
   * @param type
   *   The node type.
   * @return
   *   The called node definition.
   */
  public NodeDefinition setType(String type) {
    definition.putString("type", type);
    return this;
  }

  /**
   * Gets the node name.
   */
  public String name() {
    return definition.getString("name");
  }

  /**
   * Sets the node name.
   *
   * @param name
   *   The node name.
   */
  public NodeDefinition setName(String name) {
    definition.putString("name", name);
    return this;
  }

  /**
   * Gets the node main.
   */
  public String main() {
    return definition.getString("main");
  }

  /**
   * Sets the node main. This is a string reference to the verticle
   * to be run when a node worker is started.
   *
   * @param main
   *   The node main.
   * @return
   *   The called node definition.
   */
  public NodeDefinition setMain(String main) {
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
   *   The called node definition.
   */
  public NodeDefinition setModule(String moduleName) {
    definition.putString("module", moduleName);
    return this;
  }

  /**
   * Gets the node configuration.
   *
   * @return
   *   The node configuration.
   */
  public JsonObject config() {
    JsonObject config = definition.getObject("config");
    if (config == null) {
      config = new JsonObject();
    }
    return config;
  }

  /**
   * Sets the node configuration.
   *
   * @param config
   *   The node configuration.
   * @return
   *   The called node definition.
   */
  public NodeDefinition setConfig(JsonObject config) {
    definition.putObject("config", config);
    return this;
  }

  /**
   * Sets the node worker grouping.
   *
   * @param grouping
   *   A grouping definition.
   * @return
   *   The called node definition.
   */
  public NodeDefinition groupBy(GroupingDefinition grouping) {
    definition.putObject("grouping", grouping.serialize());
    return this;
  }

  /**
   * Gets the node worker grouping.
   */
  public GroupingDefinition grouping() {
    return new GroupingDefinition(definition.getObject("grouping"));
  }

  /**
   * Sets the number of node workers.
   *
   * @param workers
   *   The number of node workers.
   * @return
   *   The called node definition.
   */
  public NodeDefinition setWorkers(int workers) {
    definition.putNumber("workers", workers);
    return this;
  }

  /**
   * Gets the number of node workers.
   */
  public int workers() {
    return definition.getInteger("workers", DEFAULT_NUM_WORKERS);
  }

  /**
   * Sets the node worker heartbeat interval.
   *
   * @param interval
   *   A heartbeat interval.
   * @return
   *   The called node definition.
   */
  public NodeDefinition setHeartbeatInterval(long interval) {
    definition.putNumber("heartbeat", interval);
    return this;
  }

  /**
   * Gets the node heartbeat interval.
   *
   * @return
   *   A node heartbeat interval.
   */
  public long heartbeatInterval() {
    return definition.getLong("heartbeat", DEFAULT_HEARTBEAT_INTERVAL);
  }

  /**
   * Adds a connection to a node definition.
   */
  private NodeDefinition addDefinition(NodeDefinition definition) {
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
   * Adds a channel to a node.
   *
   * @param definition
   *   The node definition.
   * @return
   *   The node definition.
   */
  public NodeDefinition to(NodeDefinition definition) {
    return addDefinition(definition);
  }

  /**
   * Adds a channel to a verticle node.
   *
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toVerticle() {
    return addDefinition(new NodeDefinition().setType(NodeDefinition.VERTICLE));
  }

  /**
   * Adds a channel to a verticle node.
   *
   * @param name
   *   The node name.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toVerticle(String name) {
    return addDefinition(new NodeDefinition().setType(NodeDefinition.VERTICLE).setName(name));
  }

  /**
   * Adds a channel to a verticle node.
   *
   * @param name
   *   The node name.
   * @param main
   *   The verticle main.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toVerticle(String name, String main) {
    return toVerticle(name, main, new JsonObject(), 1);
  }

  /**
   * Adds a channel to a verticle node.
   *
   * @param name
   *   The node name.
   * @param main
   *   The verticle main.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toVerticle(String name, String main, JsonObject config) {
    return toVerticle(name, main, config, 1);
  }

  /**
   * Adds a channel to a verticle node.
   *
   * @param name
   *   The node name.
   * @param main
   *   The verticle main.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toVerticle(String name, String main, int workers) {
    return toVerticle(name, main, new JsonObject(), workers);
  }

  /**
   * Adds a channel to a verticle node.
   *
   * @param name
   *   The node name.
   * @param main
   *   The verticle main.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toVerticle(String name, String main, JsonObject config, int workers) {
    return addDefinition(new NodeDefinition().setType(NodeDefinition.VERTICLE).setName(name).setMain(main).setConfig(config).setWorkers(workers));
  }

  /**
   * Adds a channel to a module node.
   *
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toModule() {
    return addDefinition(new NodeDefinition().setType(NodeDefinition.MODULE));
  }

  /**
   * Adds a channel to a module node.
   *
   * @param name
   *   The node name.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toModule(String name) {
    return addDefinition(new NodeDefinition().setType(NodeDefinition.MODULE).setName(name));
  }

  /**
   * Adds a channel to a module node.
   *
   * @param name
   *   The node name.
   * @param moduleName
   *   The module name.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toModule(String name, String moduleName) {
    return toModule(name, moduleName, new JsonObject(), 1);
  }

  /**
   * Adds a channel to a module node.
   *
   * @param name
   *   The node name.
   * @param moduleName
   *   The module name.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toModule(String name, String moduleName, JsonObject config) {
    return toModule(name, moduleName, config, 1);
  }

  /**
   * Adds a channel to a module node.
   *
   * @param name
   *   The node name.
   * @param moduleName
   *   The module name.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toModule(String name, String moduleName, int workers) {
    return toModule(name, moduleName, new JsonObject(), workers);
  }

  /**
   * Adds a channel to a module node.
   *
   * @param name
   *   The node name.
   * @param moduleName
   *   The module name.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new node definition instance.
   */
  public NodeDefinition toModule(String name, String moduleName, JsonObject config, int workers) {
    return addDefinition(new NodeDefinition().setType(NodeDefinition.MODULE).setName(name).setModule(moduleName).setConfig(config).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

}
