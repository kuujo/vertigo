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
package com.blankstyle.vine.definition;

import org.vertx.java.core.json.JsonObject;


/**
 * A default vine context implementation.
 *
 * @author Jordan Halterman
 */
public class JsonVineDefinition implements VineDefinition {

  private JsonObject definition = new JsonObject();

  public JsonVineDefinition() {
  }

  public JsonVineDefinition(JsonObject json) {
    definition = json;
  }

  public String getAddress() {
    return definition.getString("address");
  }

  @Override
  public VineDefinition setAddress(String address) {
    definition.putString("address", address);
    return this;
  }

  private SeedDefinition addDefinition(SeedDefinition definition) {
    JsonObject connections = this.definition.getObject("connections");
    if (connections == null) {
      connections = new JsonObject();
      this.definition.putObject("connections", connections);
    }
    if (!connections.getFieldNames().contains(definition.getName())) {
      connections.putObject(definition.getName(), definition.serialize());
    }
    return definition;
  }

  @Override
  public SeedDefinition feed(SeedDefinition definition) {
    return addDefinition(definition);
  }

  @Override
  public SeedDefinition feed(String name) {
    return feed(name, null, 1);
  }

  @Override
  public SeedDefinition feed(String name, String main) {
    return feed(name, main, 1);
  }

  @Override
  public SeedDefinition feed(String name, String main, int workers) {
    return addDefinition(new JsonSeedDefinition().setName(name).setMain(main).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

}
