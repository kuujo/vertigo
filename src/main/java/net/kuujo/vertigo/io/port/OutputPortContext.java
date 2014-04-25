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

import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.port.impl.DefaultOutputPortContext;
import net.kuujo.vertigo.io.stream.OutputStreamContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Output port context represents a set of output connections for a single
 * port within a single instance of a component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultOutputPortContext.class
)
public interface OutputPortContext extends PortContext<OutputPortContext> {

  /**
   * Returns the parent output context.
   *
   * @return The parent output context.
   */
  OutputContext output();

  /**
   * Returns a collection of port streams.
   *
   * @return A collection of streams in the port.
   */
  Collection<OutputStreamContext> streams();

  /**
   * Returns a list of output hooks.
   *
   * @return A list of port output hooks.
   */
  List<OutputHook> hooks();

}
