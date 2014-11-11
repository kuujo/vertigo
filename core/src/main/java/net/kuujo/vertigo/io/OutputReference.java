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

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.io.port.OutputPortReference;

/**
 * Reference to component output.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface OutputReference {

  /**
   * Returns a reference to an output port.
   *
   * @param name The name of the output port to reference.
   * @param <T> The output port type.
   * @return The output port reference.
   */
  <T> OutputPortReference<T> port(String name);

}
