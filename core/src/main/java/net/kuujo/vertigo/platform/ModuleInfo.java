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

import org.vertx.java.core.json.JsonObject;

/**
 * Module configuration information.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ModuleInfo {
  private final ModuleIdentifier id;
  private final ModuleFields fields;

  public ModuleInfo(String id, JsonObject config) {
    this(new ModuleIdentifier(id), new ModuleFields(config));
  }

  public ModuleInfo(ModuleIdentifier id, ModuleFields fields) {
    this.id = id;
    this.fields = fields;
  }

  /**
   * Returns the module identifier.
   *
   * @return The module identifier.
   */
  public ModuleIdentifier id() {
    return id;
  }

  /**
   * Returns the module configuration fields.
   *
   * @return The module configuration fields defined in <code>mod.json</code>.
   */
  public ModuleFields fields() {
    return fields;
  }

}
