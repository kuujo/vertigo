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

import net.kuujo.vertigo.definition.NetworkDefinition;

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
   * Returns a list of network component contexts.
   */
  public Collection<ComponentContext> getComponentContexts() {
    JsonObject components = context.getObject("components");
    ArrayList<ComponentContext> contexts = new ArrayList<ComponentContext>();
    Iterator<String> iter = components.getFieldNames().iterator();
    while (iter.hasNext()) {
      contexts.add(new ComponentContext(components.getObject(iter.next()), this));
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
    if (componentContext == null) {
      return null;
    }
    return new ComponentContext(componentContext);
  }

  /**
   * Returns the network definition.
   *
   * @return
   *   The network definition.
   */
  public NetworkDefinition getDefinition() {
    JsonObject definition = context.getObject("definition");
    if (definition != null) {
      return new NetworkDefinition(definition);
    }
    return null;
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
