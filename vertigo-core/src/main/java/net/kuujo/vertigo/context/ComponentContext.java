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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.kuujo.vertigo.definition.ComponentDefinition;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A JSON object-based component context.
 *
 * @author Jordan Halterman
 */
public class ComponentContext implements Context {

  private JsonObject context = new JsonObject();

  private NetworkContext parent;

  public ComponentContext() {
  }

  public ComponentContext(String name) {
    context.putString("name", name);
  }

  public ComponentContext(JsonObject json) {
    context = json;
    JsonObject networkContext = context.getObject("network");
    if (networkContext != null) {
      parent = new NetworkContext(networkContext);
    }
  }

  public ComponentContext(JsonObject json, NetworkContext parent) {
    this(json);
    this.parent = parent;
  }

  /**
   * Returns the component address.
   */
  public String address() {
    return context.getString("address");
  }

  /**
   * Returns component worker addresses.
   */
  public String[] workers() {
    return (String[]) context.getArray("workers").toArray();
  }

  /**
   * Returns a list of component connections.
   */
  public Collection<ConnectionContext> connectionContexts() {
    Set<ConnectionContext> contexts = new HashSet<ConnectionContext>();
    JsonObject connections = context.getObject("connections");
    Iterator<String> iter = connections.getFieldNames().iterator();
    while (iter.hasNext()) {
      contexts.add(new ConnectionContext(connections.getObject(iter.next())));
    }
    return contexts;
  }

  /**
   * Returns all worker contexts.
   */
  public Collection<WorkerContext> workerContexts() {
    JsonArray workers = context.getArray("workers");
    ArrayList<WorkerContext> contexts = new ArrayList<WorkerContext>();
    Iterator<Object> iter = workers.iterator();
    while (iter.hasNext()) {
      contexts.add(new WorkerContext(context.copy().putString("address", (String) iter.next()), this));
    }
    return contexts;
  }

  /**
   * Returns the component definition.
   */
  public ComponentDefinition definition() {
    JsonObject definition = context.getObject("definition");
    if (definition != null) {
      return new ComponentDefinition(definition);
    }
    return new ComponentDefinition();
  }

  /**
   * Returns the parent network context.
   */
  public NetworkContext context() {
    return parent;
  }

  @Override
  public JsonObject serialize() {
    JsonObject context = this.context.copy();
    if (parent != null) {
      context.putObject("network", parent.serialize());
    }
    return context;
  }

}
