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

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.impl.DefaultCluster;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.component.impl.DefaultComponentFactory;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.impl.ModuleIdentifier;

/**
 * Component helper methods.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Components {

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
    } catch (IllegalArgumentException e) {
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

  /**
   * Creates a component instance for the current Vert.x instance.
   */
  public static Component createComponent(Vertx vertx, Container container) {
    InstanceContext context = parseContext(container.config());
    return new DefaultComponentFactory().setVertx(vertx).setContainer(container).createComponent(context, new DefaultCluster(context.component().network().cluster(), vertx, container));
  }

  /**
   * Builds a verticle configuration.
   *
   * @param context The verticle context.
   * @return A verticle configuration.
   */
  public static JsonObject buildConfig(InstanceContext context, Cluster cluster) {
    JsonObject config = context.component().config().copy();
    return config.putObject("__context__", Contexts.serialize(context));
  }

  /**
   * Parses an instance context from configuration.
   *
   * @param config The verticle configuration.
   * @return The verticle context.
   */
  private static InstanceContext parseContext(JsonObject config) {
    JsonObject context = config.getObject("__context__");
    if (context == null) {
      throw new IllegalArgumentException("No component context found.");
    }
    config.removeField("__context__");
    return Contexts.deserialize(context);
  }

}
