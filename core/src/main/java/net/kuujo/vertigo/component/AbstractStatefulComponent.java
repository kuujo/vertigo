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

import io.vertx.core.json.JsonObject;

/**
 * Abstract stateful component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class AbstractStatefulComponent extends AbstractComponent {

  /**
   * Called when a component checkpoint is performed.
   *
   * @param state The checkpoint state to populate.
   */
  protected abstract void checkpoint(JsonObject state);

  /**
   * Called when the component is recovering from a previous checkpoint.
   *
   * @param state The checkpoint state from which to recover.
   */
  protected abstract void recover(JsonObject state);

}
