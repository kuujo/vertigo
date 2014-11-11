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
package net.kuujo.vertigo.component;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.io.InputReference;
import net.kuujo.vertigo.io.OutputReference;

/**
 * Component reference.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface ComponentReference {

  /**
   * Returns a reference to a component's partition.
   *
   * @param partition The partition to reference.
   * @return The partition reference.
   */
  PartitionReference partition(int partition);

  /**
   * Returns a reference to the component's input.
   *
   * @return A reference to the component's input.
   */
  InputReference input();

  /**
   * Returns a reference to the component's output.
   *
   * @return A reference to the component's output.
   */
  OutputReference output();

}
