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

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.serializer.Serializers;

/**
 * Context utilities.
 *
 * @author Jordan Halterman
 */
public final class Context {

  /**
   * Stores a context in a configuration object.
   *
   * @param context
   *   The context to store.
   * @param config
   *   The configuration in which to store the context.
   * @return
   *   The updated configuration object.
   */
  public static JsonObject storeContext(InstanceContext<?> context, JsonObject config) {
    JsonObject serialized = Serializers.getDefault().serialize(context);
    return config.putObject("__context__", serialized);
  }

  /**
   * Parses an instance context from a configuration object.
   *
   * @param config
   *   The Json configuration object.
   * @return
   *   An instance context.
   */
  public static InstanceContext<?> parseContext(JsonObject config) {
    if (config != null && config.getFieldNames().contains("__context__")) {
      JsonObject contextInfo = config.getObject("__context__");
      if (contextInfo != null) {
        config.removeField("__context__");
        return InstanceContext.fromJson(contextInfo);
      }
    }
    return null;
  }

}
