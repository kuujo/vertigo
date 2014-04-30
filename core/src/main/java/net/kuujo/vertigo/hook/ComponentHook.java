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
package net.kuujo.vertigo.hook;

import net.kuujo.vertigo.component.Component;

/**
 * Hook type for component events.<p>
 *
 * Component hooks can be added to specific components through the
 * network configuration API. All component hooks are serializable
 * using the default Jackson-based json serializer.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ComponentHook extends IOHook {

  /**
   * Called when the component has started.
   *
   * @param component The component that was started.
   */
  void handleStart(Component component);

  /**
   * Called when the component has stopped.
   *
   * @param component The component that was stopped.
   */
  void handleStop(Component component);

}
