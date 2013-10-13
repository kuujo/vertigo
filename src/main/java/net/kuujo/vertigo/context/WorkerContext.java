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

/**
 * A JSON based worker context.
 *
 * @author Jordan Halterman
 */
public class WorkerContext implements Context {

  private JsonObject context = new JsonObject();

  private ComponentContext parent;

  public WorkerContext() {
  }

  public WorkerContext(String name) {
    context.putString("name", name);
  }

  public WorkerContext(JsonObject context) {
    this.context = context;
    JsonObject componentContext = context.getObject("component");
    if (componentContext != null) {
      parent = ComponentContext.fromJson(componentContext);
    }
  }

  public WorkerContext(JsonObject context, ComponentContext parent) {
    this(context);
    this.parent = parent;
  }

  /**
   * Gets the unique worker address.
   *
   * @return
   *   An eventbus address.
   */
  public String address() {
    return context.getString("address");
  }

  /**
   * Gets the worker configuration.
   *
   * @return
   *   A JSON configuration.
   */
  public JsonObject config() {
    return getComponentContext().getDefinition().config();
  }

  /**
   * Gets the parent component context.
   *
   * @return
   *   The worker's parent component context.
   */
  public ComponentContext getComponentContext() {
    return parent;
  }

  @Override
  public JsonObject serialize() {
    JsonObject context = this.context.copy();
    if (parent != null) {
      context.putObject("component", parent.serialize().copy());
    }
    return context;
  }

}
