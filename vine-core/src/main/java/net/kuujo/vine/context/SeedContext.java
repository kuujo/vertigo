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
package net.kuujo.vine.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.kuujo.vine.definition.SeedDefinition;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A JSON object-based seed context.
 *
 * @author Jordan Halterman
 */
public class SeedContext implements Context {

  private JsonObject context = new JsonObject();

  private VineContext parent;

  public SeedContext() {
  }

  public SeedContext(String name) {
    context.putString("name", name);
  }

  public SeedContext(JsonObject json) {
    context = json;
    JsonObject vineContext = context.getObject("vine");
    if (vineContext != null) {
      parent = new VineContext(vineContext);
    }
  }

  public SeedContext(JsonObject json, VineContext parent) {
    this(json);
    this.parent = parent;
  }

  /**
   * Returns the seed address.
   */
  public String getAddress() {
    return context.getString("address");
  }

  /**
   * Returns seed worker addresses.
   */
  public String[] getWorkers() {
    return (String[]) context.getArray("workers").toArray();
  }

  /**
   * Returns a list of seed connections.
   */
  public Collection<ConnectionContext> getConnectionContexts() {
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
  public Collection<WorkerContext> getWorkerContexts() {
    JsonArray workers = context.getArray("workers");
    ArrayList<WorkerContext> contexts = new ArrayList<WorkerContext>();
    Iterator<Object> iter = workers.iterator();
    while (iter.hasNext()) {
      contexts.add(new WorkerContext(context.copy().putString("address", (String) iter.next()), this));
    }
    return contexts;
  }

  /**
   * Returns the seed definition.
   */
  public SeedDefinition getDefinition() {
    JsonObject definition = context.getObject("definition");
    if (definition != null) {
      return new SeedDefinition(definition);
    }
    return new SeedDefinition();
  }

  /**
   * Returns the parent vine context.
   */
  public VineContext getContext() {
    return parent;
  }

  @Override
  public JsonObject serialize() {
    JsonObject context = this.context.copy();
    if (parent != null) {
      context.putObject("vine", parent.serialize());
    }
    return context;
  }

}
