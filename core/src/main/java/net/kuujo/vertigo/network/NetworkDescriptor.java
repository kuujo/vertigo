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

import net.kuujo.vertigo.component.ComponentDescriptor;
import net.kuujo.vertigo.io.connection.ConnectionDescriptor;

import java.util.Collection;

/**
 * Network descriptor.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface NetworkDescriptor {

  /**
   * <code>name</code> is a string indicating the unique network name. This is the
   * address at which the network will monitor network components. This field is required.
   */
  static final String NETWORK_NAME = "name";

  /**
   * <code>components</code> is an object defining network component configurations. Each
   * item in the object must be keyed by the unique component address, with each item
   * being an object containing the component configuration. See the
   * {@link net.kuujo.vertigo.component.ComponentDefinition} interface for configuration options.
   */
  static final String NETWORK_COMPONENTS = "components";

  /**
   * <code>connections</code> is an array defining network connection configurations. Each
   * item in the array must be an object defining a <code>source</code> and <code>target</code>
   * configuration.
   */
  static final String NETWORK_CONNECTIONS = "connections";

  /**
   * Returns the unique network ID.
   *
   * @return The unique network ID.
   */
  String id();

  /**
   * Returns a collection of component descriptors.
   *
   * @return A collection of component descriptors.
   */
  Collection<ComponentDescriptor> components();

  /**
   * Returns a collection of connection descriptors.
   *
   * @return A collection of connection descriptors.
   */
  Collection<ConnectionDescriptor> connections();

}
