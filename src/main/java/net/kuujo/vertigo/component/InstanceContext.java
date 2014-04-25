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
package net.kuujo.vertigo.component;

import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.component.impl.DefaultInstanceContext;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A component instance context which contains information regarding a specific component
 * (module or verticle) instance within a network.<p>
 *
 * The instance context is the primary immutable configuration used by
 * each instance of a component verticle or module to construct its input
 * and output ports/connections. Note that the context is observable, and
 * component instance observe the context for changes within the cluster
 * in order to support runtime network configuration changes by automatically
 * updating their input and output connections when the context changes.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultInstanceContext.class
)
public interface InstanceContext extends Context<InstanceContext> {

  /**
   * Returns the instance number.
   * 
   * @return The instance number.
   */
  int number();

  /**
   * Returns the instance address.<p>
   *
   * This is simply a unique identifier used to identify the component
   * across all networks.
   *
   * @return The instance address.
   */
  String address();

  /**
   * Returns the instance status address.<p>
   *
   * The status address is used by the component instance to notify
   * the network and other components within the network of its current
   * status. This is used by the network manager to coordinate startup
   * of components across a network.
   *
   * @return The instance status address.
   */
  String status();

  /**
   * Returns the instance input context.
   *
   * @return The instance input context.
   */
  InputContext input();

  /**
   * Returns the instance output context.
   *
   * @return The instance output context.
   */
  OutputContext output();

  /**
   * Returns the parent component context.
   * 
   * @return The parent component context.
   */
  @SuppressWarnings("rawtypes")
  <T extends ComponentContext> T component();

}
