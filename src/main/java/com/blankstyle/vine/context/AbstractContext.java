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

import com.blankstyle.vine.Context;

/**
 * An abstract context implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractContext<T> implements Context<T> {

  protected String address;

  protected Set<Context<?>> connections = new HashSet<Context<?>>();

  @Override
  public String getAddress() {
    return address;
  }

  /**
   * Adds a context to the connections set.
   */
  protected Context<?> addConnection(Context<?> context) {
    if (!connections.contains(context)) {
      connections.add(context);
    }
    return context;
  }

  @Override
  public JsonObject toJsonObject() {
    JsonObject json = new JsonObject();
    json.putString("address", address);
    JsonArray connectionsArray = new JsonArray();
    Iterator<Context<?>> iter = connections.iterator();
    while (iter.hasNext()) {
      connectionsArray.addObject(iter.next().toJsonObject());
    }
    json.putArray("connections", connectionsArray);
    return json;
  }

}
