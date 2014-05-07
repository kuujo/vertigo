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

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.ClusterFactory;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.component.impl.DefaultInstanceContext;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Context utilities.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Config {

  /**
   * Parses a cluster from configuration.
   *
   * @param config The verticle configuration.
   * @return The verticle's cluster.
   */
  static Cluster parseCluster(JsonObject config, Vertx vertx, Container container) {
    if (!config.containsField("__cluster__")) {
      throw new IllegalArgumentException("No component cluster found.");
    }
    String cluster = config.getString("__cluster__");
    config.removeField("__cluster__");
    return ClusterFactory.getCluster(cluster, vertx, container);
  }

  /**
   * Builds a verticle configuration.
   *
   * @param context The verticle context.
   * @return A verticle configuration.
   */
  public static JsonObject buildConfig(InstanceContext context, Cluster cluster) {
    JsonObject config = context.component().config().copy();
    return config.putObject("__context__", DefaultInstanceContext.toJson(context))
        .putString("__cluster__", cluster.address());
  }

  /**
   * Parses an instance context from configuration.
   *
   * @param config The verticle configuration.
   * @return The verticle context.
   */
  static InstanceContext parseContext(JsonObject config) {
    JsonObject context = config.getObject("__context__");
    if (context == null) {
      throw new IllegalArgumentException("No component context found.");
    }
    config.removeField("__context__");
    return DefaultInstanceContext.fromJson(context);
  }

}
