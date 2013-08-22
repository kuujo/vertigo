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
package com.blankstyle.vine;

import org.vertx.java.core.json.JsonObject;

/**
 * A seed configuration.
 *
 * @author Jordan Halterman
 */
public class SeedConfig {

  private JsonObject config;

  public SeedConfig() {
    this.config = new JsonObject();
  }

  public SeedConfig(JsonObject config) {
    this.config = config;
  }

  public String getAddress() {
    return config.getString("address");
  }

  private String createAddress(String base) {
    return String.format("vine:%s", base);
  }

  public SeedConfig setType(String type) {
    config.putString("type", type);
    return this;
  }

  public String getType() {
    return config.getString("type");
  }

  public SeedConfig setModule(String moduleName) {
    config.putString("module", moduleName);
    config.putString("address", createAddress(moduleName));
    return this;
  }

  public String getModule() {
    return config.getString("module");
  }

  public SeedConfig setMain(String main) {
    config.putString("main", main);
    config.putString("address", createAddress(main));
    return this;
  }

  public String getMain() {
    return config.getString("main");
  }

  public SeedConfig setWorkers(int workers) {
    config.putNumber("workers", workers);
    return this;
  }

  public Number getWorkers() {
    return config.getNumber("workers");
  }

  public SeedConfig connectTo(String address) {
    config.getArray("connections").add(address);
    return this;
  }

  public SeedConfig connectTo(SeedConfig seed) {
    config.getArray("connections").add(seed.getAddress());
    return this;
  }

  public JsonObject toObject() {
    return config;
  }

}
