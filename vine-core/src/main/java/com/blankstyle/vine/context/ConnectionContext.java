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

import org.vertx.java.core.json.JsonObject;

/**
 * A seed connection context.
 *
 * @author Jordan Halterman
 */
public class ConnectionContext {

  private JsonObject context;

  private Context parent;

  public ConnectionContext() {
    this.context = new JsonObject();
  }

  public ConnectionContext(JsonObject context) {
    this.context = context;
  }

  public ConnectionContext(JsonObject context, Context parent) {
    this.context = context;
    this.parent = parent;
  }

  /**
   * Returns the name of the seed to which the connection is connected.
   */
  public String getSeedName() {
    return context.getString("name");
  }

  /**
   * Returns the connection grouping.
   */
  public JsonObject getGrouping() {
    return context.getObject("grouping");
  }

  /**
   * Returns an array of address to which the connection connects.
   */
  public String[] getAddresses() {
    return (String[]) context.getArray("addresses").toArray();
  }

  /**
   * Returns the connection parent.
   */
  public Context getParent() {
    return parent;
  }

}
