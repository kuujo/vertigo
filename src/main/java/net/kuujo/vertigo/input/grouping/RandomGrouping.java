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
package net.kuujo.vertigo.input.grouping;

import net.kuujo.vertigo.output.selector.RandomSelector;
import net.kuujo.vertigo.output.selector.Selector;

import org.vertx.java.core.json.JsonObject;

/**
 * A *random* selector.
 *
 * The *random* selector dispatches messages to component workers randomly.
 *
 * @author Jordan Halterman
 */
public class RandomGrouping extends AbstractGrouping {
  private JsonObject definition = new JsonObject();

  @Override
  public JsonObject getState() {
    return definition;
  }

  @Override
  public void setState(JsonObject state) {
    definition = state;
  }

  @Override
  public Selector createSelector() {
    return new RandomSelector(id());
  }

}
