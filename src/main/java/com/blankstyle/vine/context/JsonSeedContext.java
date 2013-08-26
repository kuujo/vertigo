package com.blankstyle.vine.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Serializeable;
import com.blankstyle.vine.definition.JsonSeedDefinition;
import com.blankstyle.vine.definition.SeedDefinition;

public class JsonSeedContext implements SeedContext, Serializeable<JsonObject> {

  private JsonObject context = new JsonObject();

  private VineContext parent;

  private Handler<SeedContext> updateHandler;

  public JsonSeedContext() {
  }

  public JsonSeedContext(String name) {
    context.putString("name", name);
  }

  public JsonSeedContext(JsonObject json) {
    context = json;
  }

  public JsonSeedContext(JsonObject json, VineContext parent) {
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

  @Override
  public Collection<WorkerContext> getWorkerContexts() {
    JsonArray workers = context.getArray("workers");
    ArrayList<WorkerContext> contexts = new ArrayList<WorkerContext>();
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
  public VineContext getContext() {
    return parent;
  }

  @Override
  public SeedContext setContext(VineContext context) {
    parent = context;
    return this;
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
