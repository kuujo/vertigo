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

/**
 * Context utilities.
 *
 * @author Jordan Halterman
 */
public final class Context {

  /**
   * Parses an instance context from a configuration object.
   *
   * @param config
   *   The Json configuration object.
   * @return
   *   An instance context.
   */
  public static InstanceContext<?> parseContext(JsonObject config) {
    if (config != null && !config.getFieldNames().isEmpty()) {
      InstanceContext<?> context = InstanceContext.fromJson(config);
      for (String fieldName : config.getFieldNames()) {
        config.removeField(fieldName);
      }
      JsonObject realConfig = context.component().config();
      for (String fieldName : realConfig.getFieldNames()) {
        config.putValue(fieldName, realConfig.getValue(fieldName));
      }
      return context;
    }
    return null;
  }

}
