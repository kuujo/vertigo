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
package net.kuujo.vertigo.io.connection;

import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.io.stream.OutputStreamInfo;

import java.util.List;

/**
 * Output connection context represents a single instance's output to
 * an instance of another component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface OutputConnectionInfo extends ConnectionInfo<OutputConnectionDescriptor> {

  /**
   * Returns the parent output stream context.
   *
   * @return The parent stream context.
   */
  OutputStreamInfo stream();

  /**
   * Returns a list of output hooks.
   *
   * @return A list of output hooks.
   */
  List<OutputHook> hooks();

}
