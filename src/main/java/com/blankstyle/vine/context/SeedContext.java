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

import com.blankstyle.vine.Context;
import com.blankstyle.vine.definition.SeedDefinition;

/**
 * A Seed context.
 *
 * @author Jordan Halterman
 */
public interface SeedContext extends Context<SeedContext> {

  /**
   * Returns a collection of seed worker contexts.
   *
   * @return
   *   A collection of seed worker contexts.
   */
  public Collection<WorkerContext> getWorkerContexts();

  /**
   * Returns the seed definition.
   *
   * @return
   *   The seed definition.
   */
  public SeedDefinition getDefinition();

  /**
   * Returns the parent vine context.
   *
   * @return
   *   The parent vine context.
   */
  public VineContext getContext();

  /**
   * Sets the parent vine context.
   *
   * @param context
   *   The parent vine context.
   * @return
   *   The called context instance.
   */
  public SeedContext setContext(VineContext context);

}
