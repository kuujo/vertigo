package com.blankstyle.vine.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.definition.JsonSeedDefinition;
import com.blankstyle.vine.definition.SeedDefinition;

public class JsonSeedContext implements SeedContext<JsonObject> {

  private JsonObject context = new JsonObject();

  private VineContext<JsonObject> parent;

  private Handler<SeedContext<JsonObject>> updateHandler;

  public JsonSeedContext() {
  }

  public JsonSeedContext(String name) {
    context.putString("name", name);
  }

  public JsonSeedContext(JsonObject json) {
    context = json;
  }

  public JsonSeedContext(JsonObject json, VineContext<JsonObject> parent) {
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
  public void updateHandler(Handler<SeedContext<JsonObject>> handler) {
    updateHandler = handler;
  }

  @Override
  public Collection<WorkerContext<JsonObject>> getWorkerContexts() {
    JsonArray workers = context.getArray("workers");
    ArrayList<WorkerContext<JsonObject>> contexts = new ArrayList<WorkerContext<JsonObject>>();
    Iterator<Object> iter = workers.iterator();
    while (iter.hasNext()) {
      contexts.add(new JsonWorkerContext((JsonObject) iter.next(), this));
    }
    return contexts;
  }

  @Override
  public SeedDefinition getDefinition() {
    JsonObject definition = context.getObject("definition");
    if (definition != null) {
      return new JsonSeedDefinition(definition);
    }
    return new JsonSeedDefinition();
  }

  @Override
  public VineContext<JsonObject> getContext() {
    return parent;
  }

  @Override
  public SeedContext<JsonObject> setContext(VineContext<JsonObject> context) {
    parent = context;
    return this;
  }

}
