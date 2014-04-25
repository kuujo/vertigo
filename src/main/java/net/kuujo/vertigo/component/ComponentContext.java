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

import java.util.List;

import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.component.impl.DefaultModuleContext;
import net.kuujo.vertigo.component.impl.DefaultVerticleContext;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.network.NetworkContext;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A component context which contains information regarding each component instance within
 * a single network component.<p>
 *
 * Contexts are immutable as they are constructed once a network has been deployed.
 * The component context is not actually used by any Vertigo object, but is a
 * wrapper around multiple {@link InstanceContext} instances, with each instance
 * representing an instance of the component - a Vert.x verticle or module.<p>
 *
 * See {@link ModuleContext} and {@link VerticleContext} for module and verticle
 * specific configuration options respectively.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.NAME,
  include=JsonTypeInfo.As.PROPERTY,
  property="type"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value=DefaultModuleContext.class, name="module"),
  @JsonSubTypes.Type(value=DefaultVerticleContext.class, name="verticle")
})
public interface ComponentContext<T extends ComponentContext<T>> extends Context<T> {

  /**
   * Reurns the component name.
   *
   * @return The component name.
   */
  String name();

  /**
   * Gets the unique component address.
   * 
   * @return The component address.
   */
  String address();

  /**
   * Returns the component status address.
   *
   * @return The component status address.
   */
  String status();

  /**
   * Returns a boolean indicating whether the component is a module.
   * 
   * @return Indicates whether the component is a module.
   */
  boolean isModule();

  /**
   * Returns a boolean indicating whether the component is a verticle.
   * 
   * @return Indicates whether the component is a verticle.
   */
  boolean isVerticle();

  /**
   * Gets the component configuration.
   * 
   * @return The component configuration.
   */
  JsonObject config();

  /**
   * Gets a list of all component instance contexts.
   * 
   * @return A list of component instance contexts.
   */
  List<InstanceContext> instances();

  /**
   * Returns the number of component instances.
   * 
   * @return The number of component instances.
   */
  int numInstances();

  /**
   * Gets a component instance context by instance ID.
   * 
   * @param id The instance ID.
   * @return A component instance or <code>null</code> if the instance doesn't exist.
   */
  InstanceContext instance(int instanceNumber);

  /**
   * Gets a component instance context by instance address.
   * 
   * @param address The instance address.
   * @return A component instance or <code>null</code> if the instance doesn't exist.
   */
  InstanceContext instance(String address);

  /**
   * Returns the component deployment group.
   * 
   * @return The component HA group.
   */
  String group();

  /**
   * Returns a list of component hooks.
   *
   * @return A list of component hooks.
   */
  List<ComponentHook> hooks();

  /**
   * Returns the component context as a module context.
   *
   * @return A module context.
   */
  ModuleContext asModule();

  /**
   * Returns the component context as a verticle context.
   *
   * @return A verticle context.
   */
  VerticleContext asVerticle();

  /**
   * Returns the parent network context.
   * 
   * @return The parent network context.
   */
  NetworkContext network();

}
