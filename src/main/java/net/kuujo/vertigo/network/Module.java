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
package net.kuujo.vertigo.network;

import static net.kuujo.vertigo.util.Component.isModuleName;

/**
 * A module component.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("rawtypes")
public class Module<T extends net.kuujo.vertigo.component.Component> extends ComponentType<Module<T>, T> {

  /**
   * <code>module</code> is a string indicating the module name. This field is
   * required for all module components.
   */
  public static final String MODULE_NAME = "module";

  private String module;

  public Module() {
  }

  public Module(Class<T> type, String address) {
    super(type, address);
  }

  public Module(Class<T> type, String address, String moduleName) {
    this(type, address);
    setModule(moduleName);
  }

  @Override
  protected String getDeploymentType() {
    return Component.COMPONENT_DEPLOYMENT_MODULE;
  }

  @Override
  public boolean isModule() {
    return true;
  }

  /**
   * Sets the module name.
   *
   * @param moduleName
   *   The module name.
   * @return
   *   The module configuration.
   */
  public Module<T> setModule(String moduleName) {
    if (!isModuleName(moduleName)) {
      throw new IllegalArgumentException(moduleName + " is not a valid module name.");
    }
    module = moduleName;
    return this;
  }

  /**
   * Gets the module name.
   *
   * @return
   *   The module name.
   */
  public String getModule() {
    return module;
  }

}
