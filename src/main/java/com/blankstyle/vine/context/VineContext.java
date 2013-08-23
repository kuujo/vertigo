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

import com.blankstyle.vine.Context;

/**
 * A Vine context.
 *
 * @author Jordan Halterman
 */
public interface VineContext extends Context<VineContext> {

  /**
   * Adds a new seed to the vine.
   *
   * @param context
   *   The seed context.
   * @return
   *   The seed context.
   */
  public SeedContext feed(SeedContext context);

  /**
   * Adds a new seed to the vine.
   *
   * @param address
   *   The seed address.
   * @return
   *   The seed context.
   */
  public SeedContext feed(String address);

  /**
   * Adds a new seed to the vine.
   *
   * @param address
   *   The seed address.
   * @param main
   *   The seed main.
   * @return
   *   The seed context.
   */
  public SeedContext feed(String address, String main);

  /**
   * Adds a new seed to the vine.
   *
   * @param address
   *   The seed address.
   * @param main
   *   The seed main.
   * @param workers
   *   The number of seed workers.
   * @return
   *   The seed context.
   */
  public SeedContext feed(String address, String main, int workers);

}
