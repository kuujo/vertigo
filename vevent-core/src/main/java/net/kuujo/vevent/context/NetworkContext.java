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
package net.kuujo.vevent.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.kuujo.vevent.definition.NetworkDefinition;

import org.vertx.java.core.json.JsonObject;

/**
 * A remote vine context.
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
   * Returns the vine address.
   */
  public String getAddress() {
    return context.getString("address");
  }

  /**
   * Returns a list of feeder connection contexts.
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
   * Returns a specific feeder connection context.
   *
   * @param name
   *   The connection (seed) name.
   */
  public ConnectionContext getConnectionContext(String name) {
    JsonObject connection = context.getObject("connections", new JsonObject()).getObject(name);
    if (connection != null) {
      return new ConnectionContext(connection);
    }
    return new ConnectionContext();
  }

  /**
   * Returns a list of vine seed contexts.
   */
  public Collection<NodeContext> getSeedContexts() {
    JsonObject seeds = context.getObject("seeds");
    ArrayList<NodeContext> contexts = new ArrayList<NodeContext>();
    Iterator<String> iter = seeds.getFieldNames().iterator();
    while (iter.hasNext()) {
      contexts.add(new NodeContext(seeds.getObject(iter.next()), this));
    }
    return contexts;
  }

  /**
   * Returns a specific seed context.
   *
   * @param name
   *   The seed name.
   */
  public NodeContext getSeedContext(String name) {
    JsonObject seeds = context.getObject("seeds");
    if (seeds == null) {
      return null;
    }
    JsonObject seedContext = seeds.getObject(name);
    if (seedContext == null) {
      return null;
    }
    return new NodeContext(seedContext);
  }

  /**
   * Returns the vine definition.
   *
   * @return
   *   The vine definition.
   */
  public NetworkDefinition getDefinition() {
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
