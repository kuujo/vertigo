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
package net.kuujo.vertigo.component;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.output.OutputCollector;

/**
 * A network component.
 */
public interface Component<T extends Component<T>> {

  /**
   * Gets the component Vertx instance.
   *
   * @return
   *   The component Vertx instance.
   */
  Vertx vertx();

  /**
   * Gets the component container instance.
   *
   * @return
   *   The component container instance.
   */
  Container container();

  /**
   * Returns the component's {@link InputCollector}. This is the element of the
   * component which subscribes to and receives input from other components.
   *
   * @return
   *   The components {@link InputCollector}.
   */
  InputCollector input();

  /**
   * Returns the component's {@link OutputCollector}. This is the element of the
   * component which receives subscriptions and emits messages to other components.
   *
   * @return
   *   The component's {@link OutputCollector}.
   */
  OutputCollector output();

  /**
   * Returns the component instance context.
   *
   * The instance context can be used to retrieve useful information about an
   * entire network.
   *
   * @return
   *   The instance context.
   */
  InstanceContext context();

  /**
   * Returns the instance logger.
   *
   * @return
   *   The logger for the component instance.
   */
  Logger logger();

  /**
   * Adds a hook to the component.
   *
   * @param hook
   *   The hook to add.
   * @return
   *   The called component instance.
   */
  T addHook(ComponentHook hook);

  /**
   * Starts the component.
   *
   * @return
   *   The called component instance.
   */
  T start();

  /**
   * Starts the component.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the component is started.
   * @return
   *   The called component instance.
   */
  T start(Handler<AsyncResult<T>> doneHandler);

}
