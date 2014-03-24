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
package net.kuujo.vertigo.util;

import java.util.HashSet;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.InstanceContext;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Context utilities.
 *
 * @author Jordan Halterman
 */
public final class Config {

  /**
   * Builds a verticle configuration.
   *
   * @param context The verticle context.
   * @param cluster The verticle cluster.
   * @return A verticle configuration.
   */
  public static JsonObject buildConfig(InstanceContext context, VertigoCluster cluster) {
    JsonObject config = new JsonObject();
    config.putString("network", context.component().network().address());
    config.putString("address", context.address());
    config.putString("cluster", cluster.getClass().getName());
    config.putObject("config", context.component().config());
    return config;
  }

  /**
   * Parses a cluster client instance from a configuration object.
   *
   * @param config The configuration object.
   * @return A cluster client.
   */
  @SuppressWarnings("unchecked")
  public static VertigoCluster parseCluster(JsonObject config, Vertx vertx, Container container) {
    String clusterType = config.getString("cluster");
    if (clusterType == null) {
      throw new IllegalArgumentException("No cluster class specified.");
    }
    Class<? extends VertigoCluster> clusterClass;
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try {
      clusterClass = (Class<? extends VertigoCluster>) loader.loadClass(clusterType);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Error instantiating serializer factory.");
    }
    return Factories.createObject(clusterClass, vertx, container);
  }

  /**
   * Parses a network address.
   *
   * @param config The Json configuration object.
   * @return A network address.
   */
  public static String parseNetwork(JsonObject config) {
    if (config != null && config.containsField("network")) {
      return config.getString("network");
    }
    return null;
  }

  /**
   * Parses an instance address.
   *
   * @param config The Json configuration object.
   * @return An instance address.
   */
  public static String parseAddress(JsonObject config) {
    if (config != null && config.containsField("address")) {
      return config.getString("address");
    }
    return null;
  }

  /**
   * Populates a configuration with the instance configuration.
   *
   * @param config The current configuration.
   * @param context The component instance context.
   * @return The updated configuration.
   */
  public static JsonObject populateConfig(JsonObject config) {
    if (config != null && config.containsField("config")) {
      JsonObject realConfig = config.getObject("config");
      for (String fieldName : new HashSet<String>(config.getFieldNames())) {
        config.removeField(fieldName);
      }
      if (realConfig != null) {
        for (String fieldName : realConfig.getFieldNames()) {
          config.putValue(fieldName, realConfig.getValue(fieldName));
        }
      }
    }
    return config;
  }

}
