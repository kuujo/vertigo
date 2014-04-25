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
package net.kuujo.vertigo.component;

import net.kuujo.vertigo.component.impl.DefaultModuleConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Module component configuration.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultModuleConfig.class
)
public interface ModuleConfig extends ComponentConfig<ModuleConfig> {

  /**
   * <code>module</code> is a string indicating the module name. This field is required
   * for all module components.
   */
  public static final String MODULE_NAME = "module";

  /**
   * Sets the module name.
   * 
   * @param moduleName The module name.
   * @return The module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig setModule(String moduleName);

  /**
   * Gets the module name.
   * 
   * @return The module name.
   */
  String getModule();

}
