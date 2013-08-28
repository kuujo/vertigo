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

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Context;
import com.blankstyle.vine.Serializeable;

/**
 * A JSON object-based stem context.
 *
 * @author Jordan Halterman
 */
public class StemContext implements Context<StemContext>, Serializeable<JsonObject> {

  private JsonObject context = new JsonObject();

  private Handler<StemContext> updateHandler;

  public StemContext() {
  }

  public StemContext(String name) {
    context.putString("name", name);
  }

  public StemContext(JsonObject json) {
    context = json;
  }

  public String getAddress() {
    return context.getString("address");
  }

  public StemContext setAddress(String address) {
    context.putString("address", address);
    return this;
  }

  @Override
  public void update(JsonObject data) {
    context = data;
    if (updateHandler != null) {
      updateHandler.handle(this);
    }
  }

  @Override
  public void updateHandler(Handler<StemContext> handler) {
    this.updateHandler = handler;
  }

  @Override
  public JsonObject serialize() {
    return context;
  }

}
