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
package net.kuujo.vertigo.network;

import org.vertx.java.core.json.JsonObject;

/**
 * Component container.
 *
 * @author Jordan Halterman
 */
public interface ComponentConfigurable {

  /**
   * Adds a component to the network.
   * 
   * @param component The component to add.
   * @return The added component configuration.
   */
  @SuppressWarnings("rawtypes")
  <T extends ComponentConfig> T addComponent(T component);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param instances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, int instances);

  /**
   * Adds a module or verticle component to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleOrMain The component module name or verticle main.
   * @param config The component configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param instances The number of component instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   */
  <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config, int instances);

  /**
   * Removes a component from the network.
   *
   * @param component The component to remove.
   * @return The removed component configuration.
   */
  <T extends ComponentConfig<T>> T removeComponent(T component);

  /**
   * Removes a component from the network.
   *
   * @param name The component name.
   * @return The removed component configuration.
   */
  <T extends ComponentConfig<T>> T removeComponent(String name);

  /**
   * Adds a module to the network.
   * 
   * @param module The module to add.
   * @return The added module component configuration.
   */
  ModuleConfig addModule(ModuleConfig module);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName, JsonObject config);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param instances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName, int instances);

  /**
   * Adds a module to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param moduleName The module name.
   * @param config The module configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param instances The number of module instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new module configuration.
   * @throws IllegalArgumentException If the module name is not a valid module identifier.
   */
  ModuleConfig addModule(String name, String moduleName, JsonObject config, int instances);

  /**
   * Removes a module from the network.
   *
   * @param module The module component.
   * @return The removed module configuration.
   */
  ModuleConfig removeModule(ModuleConfig module);

  /**
   * Removes a module from the network.
   *
   * @param name The module component name.
   * @return The removed module configuration.
   */
  ModuleConfig removeModule(String name);

  /**
   * Adds a verticle to the network.
   * 
   * @param verticle The verticle to add.
   * @return The added verticle component configuration.
   */
  VerticleConfig addVerticle(VerticleConfig verticle);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param config The verticle configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main, JsonObject config);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param instances The number of verticle instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main, int instances);

  /**
   * Adds a verticle to the network.
   * 
   * @param name The component name. This will be used as the basis for internal
   *          component addresses.
   * @param main The verticle main.
   * @param config The verticle configuration. This configuration will be made
   *          available as the verticle configuration within deployed module instances.
   * @param instances The number of verticle instances. If multiple instances are
   *          defined, groupings will be used to determine how messages are distributed
   *          between multiple component instances.
   * @return The new verticle configuration.
   */
  VerticleConfig addVerticle(String name, String main, JsonObject config, int instances);

  /**
   * Removes a verticle configuration from the network.
   *
   * @param verticle The verticle component.
   * @return The removed verticle configuration.
   */
  VerticleConfig removeVerticle(VerticleConfig verticle);

  /**
   * Removes a verticle configuration from the network.
   *
   * @param name The verticle component name.
   * @return The removed verticle configuration.
   */
  VerticleConfig removeVerticle(String name);

}
