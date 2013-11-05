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

import net.kuujo.vertigo.serializer.Serializable;

/**
 * A component instance context.
 *
 * @author Jordan Halterman
 */
public class InstanceContext implements Serializable {
  public static final String ID = "id";

  private JsonObject context;
  private ComponentContext parent;

  public InstanceContext() {
    context = new JsonObject();
  }

  private InstanceContext(JsonObject context) {
    this.context = context;
    if (context.getFieldNames().contains("parent")) {
      try {
        parent = ComponentContext.fromJson(context.getObject("parent"));
      }
      catch (MalformedContextException e) {
        // Invalid parent.
      }
    }
  }

  /**
   * Creates a new instance context from JSON.
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
   * Sets the instance parent.
   */
  InstanceContext setParent(ComponentContext parent) {
    this.parent = parent;
    return this;
  }

  /**
   * Returns the instance id.
   *
   * @return
   *   The unique instance id.
   */
  public String id() {
    return context.getString(ID);
  }

  /**
   * Returns the parent component context.
   *
   * @return
   *   The parent component context.
   */
  public ComponentContext getComponent() {
    return parent;
  }

  @Override
  public JsonObject getState() {
    JsonObject context = this.context.copy();
    if (parent != null) {
      context.putObject("parent", parent.getState());
    }
    return context;
  }

  @Override
  public void setState(JsonObject state) {
    context = state.copy();
    if (context.getFieldNames().contains("parent")) {
      try {
        parent = ComponentContext.fromJson(context.getObject("parent"));
      }
      catch (MalformedContextException e) {
        // Invalid parent.
      }
    }
  }

}
