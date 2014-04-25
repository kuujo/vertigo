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
package net.kuujo.vertigo.io;

import java.util.Collection;

import net.kuujo.vertigo.io.impl.DefaultOutputContext;
import net.kuujo.vertigo.io.port.OutputPortContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Output context is a wrapper around output port information for
 * a single component instance.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultOutputContext.class
)
public interface OutputContext extends IOContext<OutputContext> {

  /**
   * Returns the output's port contexts.
   *
   * @return A collection of output port contexts.
   */
  Collection<OutputPortContext> ports();

}
