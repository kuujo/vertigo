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
 * A JSON based worker context.
 *
 * @author Jordan Halterman
 */
public class WorkerContext implements Context {

  private JsonObject context = new JsonObject();

  private SeedContext parent;

  public WorkerContext() {
  }

  public WorkerContext(String name) {
    context.putString("name", name);
  }

  public WorkerContext(JsonObject json) {
    context = json;
  }

  public WorkerContext(JsonObject json, SeedContext parent) {
    this(json);
    this.parent = parent;
  }

  public String getAddress() {
    return context.getString("address");
  }

  public WorkerContext setAddress(String address) {
    context.putString("address", address);
    return this;
  }

  public SeedContext getContext() {
    return parent;
  }

  public WorkerContext setContext(SeedContext context) {
    parent = context;
    return this;
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
