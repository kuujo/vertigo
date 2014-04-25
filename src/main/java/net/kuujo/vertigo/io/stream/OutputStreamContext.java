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
package net.kuujo.vertigo.io.stream;

import java.util.List;

import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.selector.Selector;
import net.kuujo.vertigo.io.stream.impl.DefaultOutputStreamContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The output stream context represents a set of output connections
 * from one component instance to all instances of another component.
 * The context contains information about how to dispatch messages
 * between the group of target component instances.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultOutputStreamContext.class
)
public interface OutputStreamContext extends Context<OutputStreamContext> {

  /**
   * Returns the parent output port context.
   *
   * @return The parent port context.
   */
  OutputPortContext port();

  /**
   * Returns the stream connection selector.
   *
   * @return The stream connection selector.
   */
  Selector selector();

  /**
   * Returns a list of output connections.
   *
   * @return A list of output connections.
   */
  List<OutputConnectionContext> connections();

}
