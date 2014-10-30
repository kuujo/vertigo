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

import net.kuujo.vertigo.io.port.InputPortInfo;

import java.util.Collection;

/**
 * Input context is a wrapper around input port information for
 * a single component instance.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface InputInfo extends IOInfo<InputInfo> {

  /**
   * Returns the input's port contexts.
   *
   * @return A collection of input port contexts.
   */
  Collection<InputPortInfo> ports();

  /**
   * Returns the input port context for a given port.
   *
   * @param name The name of the port to return.
   * @return The input port context.
   */
  InputPortInfo port(String name);

}
