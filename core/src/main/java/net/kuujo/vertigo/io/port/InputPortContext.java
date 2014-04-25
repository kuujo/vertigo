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
package net.kuujo.vertigo.io.port;

import java.util.Collection;
import java.util.List;

import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.port.impl.DefaultInputPortContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Input port context represents a set of input connections for a single
 * port within a single instance of a component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultInputPortContext.class
)
public interface InputPortContext extends PortContext<InputPortContext> {

  /**
   * Returns the parent input context.
   *
   * @return The parent input context.
   */
  InputContext input();

  /**
   * Returns a collection of input port connections.
   *
   * @return A list of input connections.
   */
  Collection<InputConnectionContext> connections();

  /**
   * Returns a list of input hooks.
   *
   * @return A list of port input hooks.
   */
  List<InputHook> hooks();

}
