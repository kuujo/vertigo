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
package com.blankstyle.vine.context;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Definition;

/**
 * An abstract context implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractDefinition<T> implements Definition<T> {

  protected String address;

  protected Set<SeedDefinition> connections = new HashSet<SeedDefinition>();

  @Override
  public String getAddress() {
    return address;
  }

  /**
   * Adds a seed definition to the connections set.
   */
  protected Definition<?> addConnection(SeedDefinition definition) {
    if (!connections.contains(definition)) {
      connections.add(definition);
    }
    return definition;
  }

  @Override
  public JsonObject serialize() {
    JsonObject json = new JsonObject();
    json.putString("address", address);
    JsonArray connectionsArray = new JsonArray();
    Iterator<SeedDefinition> iter = connections.iterator();
    while (iter.hasNext()) {
      connectionsArray.addObject(iter.next().serialize());
    }
    json.putArray("connections", connectionsArray);
    return json;
  }

}
