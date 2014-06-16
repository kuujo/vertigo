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
package net.kuujo.vertigo.component.impl;

import net.kuujo.vertigo.component.ModuleConfig;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.util.Args;

import org.vertx.java.platform.impl.ModuleIdentifier;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Default module configuration implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultModuleConfig extends DefaultComponentConfig<ModuleConfig> implements ModuleConfig {
  private String module;

  public DefaultModuleConfig() {
    super();
  }

  public DefaultModuleConfig(String name, String moduleName, NetworkConfig network) {
    setName(name);
    setModule(moduleName);
  }

  @Override
  public Type getType() {
    return Type.MODULE;
  }

  @Override
  @JsonSetter("module")
  public ModuleConfig setModule(String moduleName) {
    Args.checkNotNull(moduleName, "module name cannot be null");
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
