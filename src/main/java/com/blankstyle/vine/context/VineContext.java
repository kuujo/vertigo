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
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Context;
import com.blankstyle.vine.Serializeable;
import com.blankstyle.vine.definition.VineDefinition;

/**
 * A remote vine context.
 *
 * @author Jordan Halterman
 */
public class VineContext implements Context<VineContext>, Serializeable<JsonObject> {

  private JsonObject context = new JsonObject();

  private Handler<VineContext> updateHandler;

  public VineContext() {
  }

  public VineContext(JsonObject json) {
    context = json;
  }

  public static VineContext createContext(VineDefinition vine) {
    JsonObject context = new JsonObject();
    context.putObject("definition", vine.serialize());
    context.putString("address", vine.getAddress());
    context.putNumber("expiration", vine.getMessageExpiration());
    context.putNumber("timeout", vine.getMessageTimeout());
    return new VineContext(context);
  }

  @Override
  public void update(JsonObject context) {
    this.context = context;
    if (updateHandler != null) {
      updateHandler.handle(this);
    }
  }

  @Override
  public void updateHandler(Handler<VineContext> handler) {
    updateHandler = handler;
  }

  public String getAddress() {
    return context.getString("address");
  }

  public VineContext setAddress(String address) {
    context.putString("address", address);
    return this;
  }

  public Collection<SeedContext> getSeedContexts() {
    JsonObject seeds = context.getObject("seeds");
    ArrayList<SeedContext> contexts = new ArrayList<SeedContext>();
    Iterator<String> iter = seeds.getFieldNames().iterator();
    while (iter.hasNext()) {
      contexts.add(new SeedContext(seeds.getObject(iter.next()), this));
    }
    return contexts;
  }

  public SeedContext getSeedContext(String name) {
    JsonObject seeds = context.getObject("seeds");
    if (seeds == null) {
      return null;
    }
    JsonObject seedContext = seeds.getObject(name);
    if (seedContext == null) {
      return null;
    }
    return new SeedContext(seedContext);
  }

  public VineDefinition getDefinition() {
    JsonObject definition = context.getObject("definition");
    if (definition != null) {
      return new VineDefinition(definition);
    }
    return new VineDefinition();
  }

  public void register(EventBus eventBus) {
    eventBus.registerHandler(String.format("vertx.context.%s", getAddress()), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        update(message.body());
      }
    });
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
