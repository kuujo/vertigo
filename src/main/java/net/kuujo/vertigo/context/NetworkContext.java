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
package net.kuujo.vertigo.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import net.kuujo.vertigo.Network;
import net.kuujo.vertigo.util.Json;

import org.vertx.java.core.json.JsonArray;
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

  private NetworkContext(JsonObject json) {
    context = json;
  }

  /**
   * Creates a network context from JSON.
   *
   * @param json
   *   A JSON representation of the network context.
   * @return
   *   A new network context instance.
   */
  public static NetworkContext fromJson(JsonObject json) {
    return new NetworkContext(json);
  }

  /**
   * Returns the network address.
   */
  public String address() {
    return context.getString("address");
  }

  /**
   * Returns the network broadcast address.
   */
  public String getBroadcastAddress() {
    return context.getString("broadcast");
  }

  /**
   * Get the number of auditors for the network.
   *
   * @return
   *   The number of network auditors.
   */
  public int getNumAuditors() {
    return getDefinition().getNumAuditors();
  }

  /**
   * Returns an array of network auditor addresses.
   */
  public Set<String> getAuditors() {
    JsonArray auditors = context.getArray("auditors");
    if (auditors == null) {
      auditors = new JsonArray();
    }
    return Json.<String>arrayToSet(auditors);
  }

  /**
   * Returns a list of network component contexts.
   */
  public Collection<ComponentContext> getComponentContexts() {
    JsonObject components = context.getObject("components");
    ArrayList<ComponentContext> contexts = new ArrayList<ComponentContext>();
    Iterator<String> iter = components.getFieldNames().iterator();
    while (iter.hasNext()) {
      contexts.add(ComponentContext.fromJson(components.getObject(iter.next()), this));
    }
    return contexts;
  }

  /**
   * Returns a specific component context.
   *
   * @param name
   *   The component name.
   */
  public ComponentContext getComponentContext(String name) {
    JsonObject components = context.getObject("components");
    if (components == null) {
      return null;
    }
    JsonObject componentContext = components.getObject(name);
    return componentContext != null ? ComponentContext.fromJson(componentContext) : null;
  }

  /**
   * Returns the network definition.
   *
   * @return
   *   The network definition.
   */
  public Network getDefinition() {
    JsonObject definition = context.getObject("definition");
    return definition != null ? Network.fromJson(definition) : null;
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
