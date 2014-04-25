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
package net.kuujo.vertigo;

import net.kuujo.vertigo.util.Observable;
import net.kuujo.vertigo.util.serialization.JsonSerializable;

/**
 * Immutable configuration information for Vertigo types.<p>
 *
 * Contexts are immutable network/component/instance configurations that
 * are used by Vertigo to construct networks, components, and their connections.
 * All contexts are ultimately derived from a {@link net.kuujo.vertigo.network.NetworkConfig}
 * instance. Contexts are observable through Vertigo's clustering mechanisms,
 * allowing networks and components to monitor configurations for changes
 * internally and update their state at runtime.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Context<T extends Context<T>> extends Observable<T>, JsonSerializable {

  /**
   * Returns the context address.
   *
   * @return The context address.
   */
  String address();

}
