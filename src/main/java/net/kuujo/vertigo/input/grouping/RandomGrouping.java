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
import net.kuujo.vertigo.network.Input;

/**
 * The <code>random</code> grouping dispatches messages to component workers randomly.<p>
 *
 * Note that users should use the <code>randomGrouping</code> {@link Input} method
 * rather than constructing this grouping directly.
 *
 * @author Jordan Halterman
 */
public class RandomGrouping implements Grouping {

  @Override
  public Selector createSelector() {
    return new RandomSelector();
  }

}
