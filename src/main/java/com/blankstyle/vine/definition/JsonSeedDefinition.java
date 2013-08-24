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
 * A default seed context implementation.
 *
 * @author Jordan Halterman
 */
public class JsonSeedDefinition implements SeedDefinition {

  private JsonObject definition = new JsonObject();

  public JsonSeedDefinition() {
  }

  public JsonSeedDefinition(JsonObject json) {
    definition = json;
  }

  @Override
  public String getName() {
    return definition.getString("name");
  }

  @Override
  public SeedDefinition setName(String name) {
    definition.putString("name", name);
    return this;
  }

  @Override
  public String getMain() {
    return definition.getString("main");
  }

  @Override
  public SeedDefinition setMain(String main) {
    definition.putString("main", main);
    return this;
  }

  @Override
  public SeedDefinition setWorkers(int workers) {
    definition.putNumber("workers", workers);
    return this;
  }

  @Override
  public int getWorkers() {
    return definition.getInteger("workers");
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
  public SeedDefinition to(SeedDefinition definition) {
    return addDefinition(definition);
  }

  @Override
  public SeedDefinition to(String name) {
    return to(name, null, 1);
  }

  @Override
  public SeedDefinition to(String name, String main) {
    return to(name, main, 1);
  }

  @Override
  public SeedDefinition to(String name, String main, int workers) {
    return addDefinition(new JsonSeedDefinition().setName(name).setMain(main).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

}
