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

import java.util.Collection;

import net.kuujo.vertigo.context.impl.DefaultInputContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Input context represents output information between a
 * source component and a target component. This information
 * is used to indicate where the component should listen
 * for messages.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultInputContext.class
)
public interface InputContext extends IOContext<InputContext> {

  /**
   * Returns the input's port contexts.
   *
   * @return A collection of input port contexts.
   */
  Collection<InputPortContext> ports();

}
