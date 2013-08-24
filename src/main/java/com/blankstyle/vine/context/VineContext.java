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

import com.blankstyle.vine.Observable;
import com.blankstyle.vine.definition.VineDefinition;

/**
 * A Vine context.
 *
 * @author Jordan Halterman
 */
public interface VineContext<T> extends Observable<T, VineContext<T>> {

  public String getName();

  public String getAddress();

  public VineContext<T> setAddress(String address);

  public Collection<SeedContext<T>> getSeedContexts();

  public SeedContext<T> getSeedContext(String address);

  public VineDefinition getDefinition();

}
