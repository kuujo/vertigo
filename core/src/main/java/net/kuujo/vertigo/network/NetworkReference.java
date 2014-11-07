/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.network;

import net.kuujo.vertigo.component.ComponentReference;

/**
 * Network reference.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface NetworkReference {

  /**
   * Returns a reference to a component in the network.
   *
   * @param name The name of the component to reference.
   * @return The component reference.
   */
  ComponentReference component(String name);

}
