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

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.serializer.Serializable;

/**
 * A component instance context.
 *
 * @author Jordan Halterman
 */
public class InstanceContext implements Serializable {

  private JsonObject context;

  private JsonObject parent;

  public InstanceContext() {
    context = new JsonObject();
    if (context.getFieldNames().contains("parent")) {
      parent = context.getObject("parent");
    }
  }

  InstanceContext(JsonObject context) {
    this.context = context;
  }

  InstanceContext(JsonObject context, JsonObject parent) {
    this.context = context;
    this.parent = parent;
  }

  /**
   * Creates an instance context from JSON.
   *
   * @param context
   *   A JSON representation of the instance context.
   * @return
   *   A new instance context instance.
   */
  public static InstanceContext fromJson(JsonObject context) {
    return new InstanceContext(context);
  }

  /**
   * Returns the unique instance address.
   *
   * @return
   *   The unique component instance address.
   */
  public String getAddress() {
    return context.getString(Component.ADDRESS);
  }

  /**
   * Returns the parent component.
   *
   * @return
   *   The parent component context.
   */
  public ComponentContext getComponent() {
    if (parent != null) {
      return ComponentContext.fromJson(parent);
    }
    else {
      JsonObject parent = context.getObject("parent");
      if (parent != null) {
        return ComponentContext.fromJson(parent);
      }
      return null;
    }
  }

  @Override
  public JsonObject getState() {
    // Always copy the context state so it can't be modified externally.
    JsonObject context = this.context.copy();
    if (parent != null) {
      context.putObject("parent", parent.copy());
    }
    return context;
  }

  @Override
  public void setState(JsonObject state) {
    context = state.copy();
  }

}
