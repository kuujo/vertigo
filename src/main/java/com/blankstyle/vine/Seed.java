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
package com.blankstyle.vine;

import com.blankstyle.vine.context.DefaultSeedContext;
import com.blankstyle.vine.context.SeedContext;

/**
 * An abstract seed.
 *
 * @author Jordan Halterman
 */
public abstract class Seed implements Deployable {

  /**
   * Creates a new seed context.
   *
   * @return
   *   A new seed context.
   */
  public SeedContext createContext() {
    return new DefaultSeedContext();
  }

  /**
   * Creates a new seed context.
   *
   * @param address
   *   The context address.
   * @return
   *   A new seed context.
   */
  public SeedContext createContext(String address) {
    return new DefaultSeedContext().setAddress(address);
  }

  /**
   * Creates a new seed context.
   *
   * @param address
   *   The context address.
   * @param main
   *   The seed main.
   * @return
   *   A new seed context.
   */
  public SeedContext createContext(String address, String main) {
    return new DefaultSeedContext().setAddress(address).setMain(main);
  }

  /**
   * Creates a new seed context.
   *
   * @param address
   *   The context address.
   * @param main
   *   The seed main.
   * @param workers
   *   The number of seed workers.
   * @return
   *   A new seed context.
   */
  public SeedContext createContext(String address, String main, int workers) {
    return new DefaultSeedContext().setAddress(address).setMain(main).setWorkers(workers);
  }

}
