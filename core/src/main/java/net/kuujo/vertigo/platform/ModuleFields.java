/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.platform;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Module configuration fields.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ModuleFields extends org.vertx.java.platform.impl.ModuleFields {
  private final JsonObject config;

  public ModuleFields(JsonObject config) {
    super(config);
    this.config = config;
  }

  /**
   * Returns a list of input ports defined by the module.
   *
   * @return A list of input ports defined by the module.
   */
  public List<String> getInPorts() {
    List<String> ports = new ArrayList<>();
    JsonObject jsonPorts = config.getObject("ports");
    if (jsonPorts == null) {
      return ports;
    }
    JsonArray jsonInPorts = jsonPorts.getArray("in");
    if (jsonInPorts == null) {
      return ports;
    }
    for (Object jsonInPort : jsonInPorts) {
      ports.add((String) jsonInPort);
    }
    return ports;
  }

  /**
   * Returns a list of output ports defined by the module.
   *
   * @return A list of output ports defined by the module.
   */
  public List<String> getOutPorts() {
    List<String> ports = new ArrayList<>();
    JsonObject jsonPorts = config.getObject("ports");
    if (jsonPorts == null) {
      return ports;
    }
    JsonArray jsonOutPorts = jsonPorts.getArray("out");
    if (jsonOutPorts == null) {
      return ports;
    }
    for(Object jsonOutPort : jsonOutPorts) {
      ports.add((String) jsonOutPort);
    }
    return ports;
  }

}
