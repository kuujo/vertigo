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
package net.kuujo.vertigo.grouping;

import net.kuujo.vertigo.dispatcher.AllDispatcher;
import net.kuujo.vertigo.dispatcher.Dispatcher;

import org.vertx.java.core.json.JsonObject;

/**
 * An *all* grouping.
 *
 * The *all* grouping dispatches messages to all instances of a component.
 * Thus, if a component has four instances, all four component workers will
 * receive every message emitted to that component.
 *
 * @author Jordan Halterman
 */
public class AllGrouping implements Grouping {

  private JsonObject definition = new JsonObject();

  @Override
  public JsonObject serialize() {
    return definition;
  }

  @Override
  public Dispatcher initialize(JsonObject data) {
    return new AllDispatcher();
  }

}
