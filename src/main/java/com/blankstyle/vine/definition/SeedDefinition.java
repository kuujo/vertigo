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

import com.blankstyle.vine.Serializeable;


/**
 * A default seed context implementation.
 *
 * @author Jordan Halterman
 */
public class SeedDefinition implements Serializeable<JsonObject> {

  private JsonObject definition = new JsonObject();

  public SeedDefinition() {
  }

  public SeedDefinition(JsonObject json) {
    definition = json;
  }

  public String getName() {
    return definition.getString("name");
  }

  public SeedDefinition setName(String name) {
    definition.putString("name", name);
    return this;
  }

  public String getMain() {
    return definition.getString("main");
  }

  public SeedDefinition setMain(String main) {
    definition.putString("main", main);
    return this;
  }

  public SeedDefinition setGrouping(String grouping) {
    definition.putString("grouping", grouping);
    return this;
  }

  public String getGrouping() {
    return definition.getString("grouping");
  }

  public SeedDefinition setWorkers(int workers) {
    definition.putNumber("workers", workers);
    return this;
  }

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

  public SeedDefinition to(SeedDefinition definition) {
    return addDefinition(definition);
  }

  public SeedDefinition to(String name) {
    return to(name, null, 1);
  }

  public SeedDefinition to(String name, String main) {
    return to(name, main, 1);
  }

  public SeedDefinition to(String name, String main, int workers) {
    return addDefinition(new SeedDefinition().setName(name).setMain(main).setWorkers(workers));
  }

  public JsonObject serialize() {
    return definition;
  }

}
