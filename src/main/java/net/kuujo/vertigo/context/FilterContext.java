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
package net.kuujo.vertigo.context;

import net.kuujo.vertigo.filter.Condition;
import net.kuujo.vertigo.filter.Filter;

import org.vertx.java.core.json.JsonObject;

/**
 * A filter context.
 *
 * @author Jordan Halterman
 */
public class FilterContext {

  private JsonObject context;

  public FilterContext(JsonObject context) {
    this.context = context;
  }

  /**
   * Creates a filter condition.
   *
   * @return
   *   A filter condition instance.
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ClassNotFoundException
   */
  public Condition createCondition() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    Filter filter = (Filter) Class.forName(context.getString("filter")).newInstance();
    return filter.initialize(context.getObject("definition"));
  }

}
