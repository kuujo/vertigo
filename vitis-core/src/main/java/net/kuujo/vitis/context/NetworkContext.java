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
package net.kuujo.vitis.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.kuujo.vitis.definition.NetworkDefinition;

import org.vertx.java.core.json.JsonObject;

/**
 * A network context.
 *
 * @author Jordan Halterman
 */
public class NetworkContext implements Context {

  private JsonObject context = new JsonObject();

  public NetworkContext() {
  }

  public NetworkContext(JsonObject json) {
    context = json;
  }

  /**
   * Returns the network address.
   */
  public String address() {
    return context.getString("address");
  }

  /**
   * Returns the network auditor address.
   */
  public String auditAddress() {
    return context.getString("audit");
  }

  /**
   * Returns the network broadcast address.
   */
  public String broadcastAddress() {
    return context.getString("broadcast");
  }

  /**
   * Returns a list of network node contexts.
   */
  public Collection<NodeContext> contexts() {
    JsonObject components = context.getObject("components");
    ArrayList<NodeContext> contexts = new ArrayList<NodeContext>();
    Iterator<String> iter = components.getFieldNames().iterator();
    while (iter.hasNext()) {
      contexts.add(new NodeContext(components.getObject(iter.next()), this));
    }
    return contexts;
  }

  /**
   * Returns a specific node context.
   *
   * @param name
   *   The node name.
   */
  public NodeContext context(String name) {
    JsonObject nodes = context.getObject("components");
    if (nodes == null) {
      return null;
    }
    JsonObject nodeContext = nodes.getObject(name);
    if (nodeContext == null) {
      return null;
    }
    return new NodeContext(nodeContext);
  }

  /**
   * Returns the network definition.
   *
   * @return
   *   The network definition.
   */
  public NetworkDefinition definition() {
    JsonObject definition = context.getObject("definition");
    if (definition != null) {
      return new NetworkDefinition(definition);
    }
    return new NetworkDefinition();
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
