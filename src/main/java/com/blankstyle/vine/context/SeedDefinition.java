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

import com.blankstyle.vine.Definition;

/**
 * A seed context.
 *
 * @author Jordan Halterman
 */
public interface SeedDefinition extends Definition<SeedDefinition> {

  /**
   * Sets the seed main.
   *
   * @param main
   *   The seed main.
   * @return
   *   The seed context.
   */
  public SeedDefinition setMain(String main);

  /**
   * Gets the seed main.
   *
   * @return
   *   The seed main.
   */
  public String getMain();

  /**
   * Sets the number of seed workers.
   *
   * @param workers
   *   The number of seed workers.
   * @return
   *   The seed context.
   */
  public SeedDefinition setWorkers(int workers);

  /**
   * Gets the number of seed workers.
   *
   * @return
   *   The number of seed workers.
   */
  public int getWorkers();

  /**
   * Sets a new output channel on the seed.
   *
   * @param context
   *   The seed to which the channel connects.
   * @return
   *   The new seed context.
   */
  public SeedDefinition to(SeedDefinition context);

  /**
   * Sets a new output channel on the seed.
   *
   * @param address
   *   The new seed address.
   * @return
   *   The new seed context.
   */
  public SeedDefinition to(String address);

  /**
   * Sets a new output channel on the seed.
   *
   * @param address
   *   The new seed address.
   * @param main
   *   The new seed main.
   * @return
   *   The new seed context.
   */
  public SeedDefinition to(String address, String main);

  /**
   * Sets a new output channel on the seed.
   *
   * @param address
   *   The new seed address.
   * @param main
   *   The new seed main.
   * @param workers
   *   The number of new seed workers.
   * @return
   *   The new seed context.
   */
  public SeedDefinition to(String address, String main, int workers);

}
