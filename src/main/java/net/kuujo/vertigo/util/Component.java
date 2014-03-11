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
package net.kuujo.vertigo.util;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.worker.Worker;

import org.vertx.java.platform.impl.ModuleIdentifier;

/**
 * Component helper methods.
 *
 * @author Jordan Halterman
 */
public final class Component {

  /**
   * Indicates whether the given name is a module name.
   *
   * This validation is performed by using the core Vert.x module name validation
   * contained in the {@link ModuleIdentifier} class.
   *
   * @param moduleName
   *   The name to check.
   * @return
   *   Indicates whether the name is a module name.
   */
  public static boolean isModuleName(String moduleName) {
    try {
      new ModuleIdentifier(moduleName);
    }
    catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  /**
   * Indicates whether the given name is a verticle main.
   *
   * This validation is performed by using the core Vert.x module name validation
   * contained in the {@link ModuleIdentifier} class.
   *
   * @param verticleMain
   *   The name to check.
   * @return
   *   Indicates whether the name is a verticle main.
   */
  public static boolean isVerticleMain(String verticleMain) {
    return !isModuleName(verticleMain);
  }

  @SuppressWarnings("serial")
  private static Map<String, Class<?>> typeMap = new HashMap<String, Class<?>>() {{
    put("feeder", Feeder.class);
    put("worker", Worker.class);
  }};

  @SuppressWarnings("serial")
  private static Map<Class<?>, String> reverseTypeMap = new HashMap<Class<?>, String>() {{
    put(Feeder.class, "feeder");
    put(Worker.class, "worker");
  }};

  /**
   * Serializes a component type to a string.
   *
   * @param type
   *   The component type.
   * @return
   *   A string representation of the component type.
   */
  public static String serializeType(Class<?> type) {
    return reverseTypeMap.get(type);
  }

  /**
   * Deserializes a component type from a string.
   *
   * @param type
   *   The string representation of the component type.
   * @return
   *   The component type.
   */
  public static Class<?> deserializeType(String type) {
    return typeMap.get(type);
  }

}
