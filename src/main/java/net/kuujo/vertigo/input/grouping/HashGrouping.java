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

import net.kuujo.vertigo.network.ConnectionConfig;
import net.kuujo.vertigo.output.selector.HashSelector;
import net.kuujo.vertigo.output.selector.Selector;

/**
 * The <code>fields</code> grouping is a hashing based grouping. Given a set of
 * fields on which to hash, this grouping guarantees that workers will always
 * receive messages with the same field values.<p>
 *
 * Note that users should use the <code>fieldsGrouping</code> {@link ConnectionConfig} method
 * rather than constructing this grouping directly.
 *
 * @author Jordan Halterman
 */
public class HashGrouping implements Grouping {

  @Override
  public Selector createSelector() {
    return new HashSelector();
  }

}
