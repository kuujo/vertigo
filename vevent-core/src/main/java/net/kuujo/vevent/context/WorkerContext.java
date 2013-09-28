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

import org.vertx.java.core.json.JsonObject;

/**
 * A JSON based worker context.
 *
 * @author Jordan Halterman
 */
public class WorkerContext implements Context {

  private JsonObject context = new JsonObject();

  private NodeContext parent;

  public WorkerContext() {
  }

  public WorkerContext(String name) {
    context.putString("name", name);
  }

  public WorkerContext(JsonObject context) {
    this.context = context;
    JsonObject nodeContext = context.getObject("node");
    if (nodeContext != null) {
      parent = new NodeContext(nodeContext);
    }
  }

  public WorkerContext(JsonObject context, NodeContext parent) {
    this(context);
    this.parent = parent;
  }

  /**
   * Gets the unique worker address.
   *
   * @return
   *   An eventbus address.
   */
  public String getAddress() {
    return context.getString("address");
  }

  /**
   * Sets the unique worker address.
   *
   * @param address
   *   An eventbus address.
   * @return
   *   The called worker context.
   */
  public WorkerContext setAddress(String address) {
    context.putString("address", address);
    return this;
  }

  /**
   * Gets the parent node context.
   *
   * @return
   *   The worker's parent node context.
   */
  public NodeContext getContext() {
    return parent;
  }

  /**
   * Sets the parent node context.
   *
   * @param context
   *   A node context.
   * @return
   *   The called worker context.
   */
  public WorkerContext setContext(NodeContext context) {
    parent = context;
    return this;
  }

  @Override
  public JsonObject serialize() {
    JsonObject context = this.context.copy();
    if (parent != null) {
      context.putObject("node", parent.serialize().copy());
    }
    return context;
  }

}
