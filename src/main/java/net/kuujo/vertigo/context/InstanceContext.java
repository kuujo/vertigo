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
package net.kuujo.vertigo.context;

import net.kuujo.vertigo.context.impl.DefaultInstanceContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A component instance context which contains information regarding a specific component
 * (module or verticle) instance within a network.
 * 
 * @author Jordan Halterman
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
  public int number();

  /**
   * Returns the instance address.
   *
   * @return The instance address.
   */
  public String address();

  /**
   * Returns the instance status address.
   *
   * @return The instance status address.
   */
  public String status();

  /**
   * Returns the instance input context.
   *
   * @return The instance input context.
   */
  public InputContext input();

  /**
   * Returns the instance output context.
   *
   * @return The instance output context.
   */
  public OutputContext output();

  /**
   * Returns the parent component context.
   * 
   * @return The parent component context.
   */
  @SuppressWarnings("rawtypes")
  public <T extends ComponentContext> T component();

}
