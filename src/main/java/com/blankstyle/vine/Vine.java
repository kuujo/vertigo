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

import java.util.Iterator;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A vine.
 *
 * @author Jordan Halterman
 */
public class Vine  {

  private JsonObject config;

  public Vine() {
    this.config = new JsonObject();
  }

  public Vine(JsonObject config) {
    this.config = config;
  }

  public SeedConfig addVerticle(String main) {
    SeedConfig seed = new SeedConfig();
    seed.setType("verticle").setMain(main);
    config.getObject("seeds").putObject(main, seed.toObject());
    return seed;
  }

  public SeedConfig addModule(String moduleName) {
    SeedConfig seed = new SeedConfig();
    seed.setType("module").setModule(moduleName);
    config.getObject("seeds").putObject(moduleName, seed.toObject());
    return seed;
  }

  public void deploy(Handler<Feeder> feedHandler) {
    JsonObject seeds = config.getObject("seeds");
    Iterator<String> fields = seeds.getFieldNames().iterator();
    while (fields.hasNext()) {
      SeedConfig seed = new SeedConfig(seeds.getObject(fields.next()));
      if (seed.getType() == "module") {
        
      }
      else if (seed.getType() == "verticle") {
        
      }
    }
  }

  public JsonObject toObject() {
    return config;
  }

}
