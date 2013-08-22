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
package com.blankstyle.vine.impl;

import com.blankstyle.vine.SeedContext;

/**
 * A default seed context implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultSeedContext extends AbstractContext<SeedContext> implements SeedContext {

  private String main;

  private int workers = 1;

  @Override
  public SeedContext setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public SeedContext setMain(String main) {
    this.main = main;
    return this;
  }

  @Override
  public String getMain() {
    return main;
  }

  @Override
  public SeedContext setWorkers(int workers) {
    this.workers = workers;
    return this;
  }

  @Override
  public int getWorkers() {
    return workers;
  }

  @Override
  public SeedContext to(SeedContext context) {
    if (!connections.contains(context)) {
      connections.add(context);
    }
    return context;
  }

  @Override
  public SeedContext to(String address) {
    return to(address, null, 1);
  }

  @Override
  public SeedContext to(String address, String main) {
    return to(address, main, 1);
  }

  @Override
  public SeedContext to(String address, String main, int workers) {
    return (SeedContext) addConnection(new DefaultSeedContext().setAddress(address).setMain(main).setWorkers(workers));
  }

}
