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

import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.stream.OutputStreamInfo;

import java.util.Collection;
import java.util.List;

/**
 * Output port context represents a set of output connections for a single
 * port within a single instance of a component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface OutputPortInfo extends PortInfo<OutputPortInfo> {

  /**
   * Returns the parent output context.
   *
   * @return The parent output context.
   */
  OutputInfo output();

  /**
   * Returns a collection of port streams.
   *
   * @return A collection of streams in the port.
   */
  Collection<OutputStreamInfo> streams();

  /**
   * Returns a list of output hooks.
   *
   * @return A list of port output hooks.
   */
  List<OutputHook> hooks();

}
