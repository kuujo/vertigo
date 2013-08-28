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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Context;
import com.blankstyle.vine.Serializeable;
import com.blankstyle.vine.definition.SeedDefinition;

/**
 * A JSON object-based seed context.
 *
 * @author Jordan Halterman
 */
public class SeedContext implements Context<SeedContext>, Serializeable<JsonObject> {

  private JsonObject context = new JsonObject();

  private VineContext parent;

  private Handler<SeedContext> updateHandler;

  public SeedContext() {
  }

  public SeedContext(String name) {
    context.putString("name", name);
  }

  public SeedContext(JsonObject json) {
    context = json;
  }

  public SeedContext(JsonObject json, VineContext parent) {
    this(json);
    this.parent = parent;
  }

  @Override
  public void update(JsonObject json) {
    context = json;
    if (updateHandler != null) {
      updateHandler.handle(this);
    }
  }

  @Override
  public void updateHandler(Handler<SeedContext> handler) {
    updateHandler = handler;
  }

  public Collection<WorkerContext> getWorkerContexts() {
    JsonArray workers = context.getArray("workers");
    ArrayList<WorkerContext> contexts = new ArrayList<WorkerContext>();
    Iterator<Object> iter = workers.iterator();
    while (iter.hasNext()) {
      contexts.add(new WorkerContext((JsonObject) iter.next(), this));
    }
    return contexts;
  }

  public SeedDefinition getDefinition() {
    JsonObject definition = context.getObject("definition");
    if (definition != null) {
      return new SeedDefinition(definition);
    }
    return new SeedDefinition();
  }

  public VineContext getContext() {
    return parent;
  }

  public SeedContext setContext(VineContext context) {
    parent = context;
    return this;
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
