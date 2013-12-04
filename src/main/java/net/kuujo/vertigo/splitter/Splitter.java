/*
 * Copyright 2013 the original author or authors.
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
package net.kuujo.vertigo.splitter;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.function.Function;

/**
 * A message splitter.
 *
 * @author Jordan Halterman
 */
public interface Splitter extends Component<Splitter> {

  /**
   * Sets the split function on the splitter.
   *
   * @param function
   *   The split function.
   * @return
   *   The called splitter instance.
   */
  Splitter splitFunction(Function<JsonObject, Iterable<JsonObject>> function);

}
