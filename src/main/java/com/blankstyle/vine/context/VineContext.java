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
package com.blankstyle.vine.context;

import java.util.Collection;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Context;
import com.blankstyle.vine.Serializeable;
import com.blankstyle.vine.definition.VineDefinition;

/**
 * A Vine context.
 *
 * @author Jordan Halterman
 */
public interface VineContext extends Context<VineContext>, Serializeable<JsonObject> {

  /**
   * Gets the vine address.
   *
   * @return
   *   The vine address.
   */
  public String getAddress();

  /**
   * Sets the vine address.
   *
   * @param address
   *   The vine address.
   * @return
   *   The called vine instance.
   */
  public VineContext setAddress(String address);

  /**
   * Returns all vine seed contexts.
   *
   * @return
   *   A collection of vine seed contexts.
   */
  public Collection<SeedContext> getSeedContexts();

  /**
   * Returns a vine seed context.
   *
   * @param name
   *   The seed name.
   * @return
   *   A vine seed context.
   */
  public SeedContext getSeedContext(String name);

  /**
   * Returns the vine definition.
   *
   * @return
   *   The context vine definition.
   */
  public VineDefinition getDefinition();

  /**
   * Registers the context on the eventbus.
   *
   * @param eventBus
   *   An eventbus instance.
   */
  public void register(EventBus eventBus);

}
