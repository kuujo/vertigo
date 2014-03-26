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
package net.kuujo.vertigo.network.impl;

import org.vertx.java.platform.impl.ModuleIdentifier;

import net.kuujo.vertigo.network.ModuleConfig;

/**
 * Default module configuration implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultModuleConfig extends AbstractComponentConfig<ModuleConfig> implements ModuleConfig {

  /**
   * <code>module</code> is a string indicating the module name. This field is required
   * for all module components.
   */
  public static final String MODULE_NAME = "module";

  private String module;

  public DefaultModuleConfig() {
    super();
  }

  public DefaultModuleConfig(String name, String moduleName) {
    super(name);
    setModule(moduleName);
  }

  @Override
  public Type getType() {
    return Type.MODULE;
  }

  @Override
  public ModuleConfig setModule(String moduleName) {
    // Instantiate a module identifier to force it to validate the module name.
    // If the module name is invalid then an IllegalArgumentException will be thrown.
    new ModuleIdentifier(moduleName);
    this.module = moduleName;
    return this;
  }

  @Override
  public String getModule() {
    return module;
  }

}
