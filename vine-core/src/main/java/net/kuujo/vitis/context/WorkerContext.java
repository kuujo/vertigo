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
package net.kuujo.vitis.context;

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

  public WorkerContext(JsonObject json) {
    context = json;
    JsonObject seedContext = context.getObject("seed");
    if (seedContext != null) {
      parent = new NodeContext(seedContext);
    }
  }

  public WorkerContext(JsonObject json, NodeContext parent) {
    this(json);
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
   * Gets the worker stem address.
   *
   * @return
   *   A stem address.
   */
  public String getStem() {
    return context.getString("stem");
  }

  /**
   * Sets the worker stem address.
   *
   * @param address
   *   A stem address.
   * @return
   *   The called worker context.
   */
  public WorkerContext setStem(String address) {
    context.putString("stem", address);
    return this;
  }

  /**
   * Gets the parent seed context.
   *
   * @return
   *   The worker's parent seed context.
   */
  public NodeContext getContext() {
    return parent;
  }

  /**
   * Sets the parent seed context.
   *
   * @param context
   *   A seed context.
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
      context.putObject("seed", parent.serialize().copy());
    }
    return context;
  }

}
