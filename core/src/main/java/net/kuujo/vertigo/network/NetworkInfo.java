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

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.network.impl.NetworkInfoImpl;

import java.util.Collection;

/**
 * A network context which contains information regarding the complete structure of a
 * deployed network.<p>
 *
 * Network contexts are immutable as they are constructed after a network
 * is deployed.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface NetworkInfo extends TypeInfo<NetworkInfo> {

  /**
   * Returns a network info builder.
   *
   * @return A new network info builder.
   */
  static TypeInfo.Builder<NetworkInfo> builder() {
    return new NetworkInfoImpl.Builder();
  }

  /**
   * Returns a new network info builder.
   *
   * @param network An existing network info object to wrap.
   * @return A network info builder wrapper
   */
  static TypeInfo.Builder<NetworkInfo> builder(NetworkInfo network) {
    return new NetworkInfoImpl.Builder((NetworkInfoImpl) network);
  }

  /**
   * Returns the network name.<p>
   *
   * The network name is unique to the network within a Vertigo cluster.
   *
   * @return The network name.
   */
  String name();

  /**
   * Returns the network version. Contexts may be updated through network
   * configuration changes, so the version can be used to track updates to
   * the network.
   *
   * @return The network version.
   */
  String version();

  /**
   * Returns the network configuration from which the context was built.
   *
   * @return The network configuration.
   */
  Network config();

  /**
   * Returns a collection of network component contexts.
   * 
   * @return A collection of network component contexts.
   */
  Collection<ComponentInfo> components();

  /**
   * Returns a boolean indicating whether the component exists.
   *
   * @param name The name of the component to check.
   * @return Indicates whether a component with that name exists.
   */
  boolean hasComponent(String name);

  /**
   * Returns a component context by name.
   * 
   * @param name The component name.
   * @return A component context.
   * @throws IllegalArgumentException If a component does not exist at the given name.
   */
  ComponentInfo component(String name);

}
