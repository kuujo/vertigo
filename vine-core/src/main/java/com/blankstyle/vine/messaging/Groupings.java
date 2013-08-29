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
package com.blankstyle.vine.messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for resolving dispatchers via string grouping names.
 *
 * @author Jordan Halterman
 */
public final class Groupings {

  public static final String ROUND_ROBIN = "round";

  public static final String RANDOM = "random";

  public static final String FIELDS = "fields";

  @SuppressWarnings("serial")
  private static Map<String, Class<? extends Dispatcher>> groupingMap = new HashMap<String, Class<? extends Dispatcher>>() {{
    put(ROUND_ROBIN, RoundRobinDispatcher.class);
    put(RANDOM, RandomDispatcher.class);
    put(FIELDS, FieldsDispatcher.class);
  }};

  /**
   * Returns a dispatcher class from a grouping name.
   *
   * @param name
   *   The grouping name.
   * @return
   *   A registered dispatcher class.
   */
  public static Class<? extends Dispatcher> get(String name) {
    return groupingMap.get(name);
  }

}
