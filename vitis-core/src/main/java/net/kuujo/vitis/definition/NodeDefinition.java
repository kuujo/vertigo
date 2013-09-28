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

  public NodeDefinition() {
  }

  public NodeDefinition(JsonObject json) {
    definition = json;
  }

  /**
   * Gets the component name.
   */
  public String getName() {
    return definition.getString("name");
  }

  /**
   * Sets the component name.
   *
   * @param name
   *   The component name.
   */
  public NodeDefinition setName(String name) {
    definition.putString("name", name);
    return this;
  }

  /**
   * Gets the component main.
   */
  public String getMain() {
    return definition.getString("main");
  }

  /**
   * Sets the component main. This is a string reference to the verticle
   * to be run when a component worker is started.
   *
   * @param main
   *   The component main.
   */
  public NodeDefinition setMain(String main) {
    definition.putString("main", main);
    return this;
  }

  /**
   * Sets a component option.
   *
   * @param option
   *   The option to set.
   * @param value
   *   The option value.
   * @return
   *   The called component definition.
   */
  public NodeDefinition setOption(String option, String value) {
    switch (option) {
      case "name":
        return setName(value);
      case "main":
        return setMain(value);
      default:
        definition.putString(option, value);
        break;
    }
    return this;
  }

  /**
   * Gets a component option.
   *
   * @param option
   *   The option to get.
   * @return
   *   The option value.
   */
  public String getOption(String option) {
    return definition.getString(option);
  }

  /**
   * Sets the component worker grouping.
   *
   * @param grouping
   *   A grouping definition.
   * @return
   *   The called component definition.
   */
  public NodeDefinition groupBy(GroupingDefinition grouping) {
    definition.putObject("grouping", grouping.serialize());
    return this;
  }

  /**
   * Gets the component worker grouping.
   */
  public GroupingDefinition getGrouping() {
    return new GroupingDefinition(definition.getObject("grouping"));
  }

  /**
   * Sets the number of component workers.
   *
   * @param workers
   *   The number of component workers.
   * @return
   *   The called component definition.
   */
  public NodeDefinition setWorkers(int workers) {
    definition.putNumber("workers", workers);
    return this;
  }

  /**
   * Gets the number of component workers.
   */
  public int getWorkers() {
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
  public NodeDefinition setHeartbeatInterval(long interval) {
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
   * Adds a connection to a node definition.
   */
  private NodeDefinition addDefinition(NodeDefinition definition) {
    JsonObject connections = this.definition.getObject("connections");
    if (connections == null) {
      connections = new JsonObject();
      this.definition.putObject("connections", connections);
    }
    if (!connections.getFieldNames().contains(definition.getName())) {
      connections.putObject(definition.getName(), definition.serialize());
    }
    return definition;
  }

  /**
   * Creates a connection to the given definition.
   *
   * @param definition
   *   A node definition.
   */
  public NodeDefinition to(NodeDefinition definition) {
    return addDefinition(definition);
  }

  /**
   * Creates a connection to a node, creating a new node definition.
   *
   * @param name
   *   The node name.
   * @return
   *   A new node definition.
   */
  public NodeDefinition to(String name) {
    return to(name, null, 1);
  }

  /**
   * Creates a connection to a node, creating a new node definition.
   *
   * @param name
   *   The node name.
   * @param main
   *   The node main.
   * @return
   *   A new node definition.
   */
  public NodeDefinition to(String name, String main) {
    return to(name, main, 1);
  }

  /**
   * Creates a connection to a node, creating a new node definition.
   *
   * @param name
   *   The node name.
   * @param main
   *   The node main.
   * @param workers
   *   The number of node workers.
   * @return
   *   A new node definition.
   */
  public NodeDefinition to(String name, String main, int workers) {
    return addDefinition(new NodeDefinition().setName(name).setMain(main).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

}
