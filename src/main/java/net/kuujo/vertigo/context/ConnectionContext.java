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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.kuujo.vertigo.util.Json;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A component connection context.
 *
 * @author Jordan Halterman
 */
public class ConnectionContext {

  private JsonObject context;

  private ComponentContext parent;

  public ConnectionContext() {
    this.context = new JsonObject();
  }

  public ConnectionContext(JsonObject context) {
    this.context = context;
  }

  public ConnectionContext(JsonObject context, ComponentContext parent) {
    this.context = context;
    this.parent = parent;
  }

  /**
   * Returns the connection grouping.
   */
  public GroupingContext getGrouping() {
    return new GroupingContext(context.getObject("grouping"));
  }

  /**
   * Returns connection filters.
   */
  public Collection<FilterContext> getFilters() {
    Set<FilterContext> contexts = new HashSet<FilterContext>();
    JsonArray filters = context.getArray("filters");
    for (Object context : filters) {
      contexts.add(new FilterContext((JsonObject) context));
    }
    return contexts;
  }

  /**
   * Returns an array of address to which the connection connects.
   */
  public Set<String> getAddresses() {
    JsonArray addresses = context.getArray("addresses");
    if (addresses == null) {
      addresses = new JsonArray();
    }
    return Json.<String>arrayToSet(addresses);
  }

  /**
   * Returns the parent component.
   */
  public Context getComponentContext() {
    return parent;
  }

}
