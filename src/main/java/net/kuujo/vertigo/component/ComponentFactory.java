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

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.rpc.Executor;
import net.kuujo.vertigo.worker.Worker;

/**
 * A component instance factory.
 *
 * @author Jordan Halterman
 */
public interface ComponentFactory {

  /**
   * Sets the factory Vertx instance.
   *
   * @param vertx
   *   A Vertx instance.
   * @return
   *   The called factory instance.
   */
  ComponentFactory setVertx(Vertx vertx);

  /**
   * Sets the factory Container instance.
   *
   * @param container
   *   A Vert.x container.
   * @return
   *   The called factory instance.
   */
  ComponentFactory setContainer(Container container);

  /**
   * Creates a component.
   *
   * @param context
   *   The component context.
   * @return
   *   A new component instance.
   */
  <T extends Component<T>> Component<T> createComponent(InstanceContext<T> context);

  /**
   * Creates a feeder.
   *
   * @param context
   *   The feeder instance context.
   * @return
   *   A new feeder instance.
   */
  Feeder createFeeder(InstanceContext<Feeder> context);

  /**
   * Creates an executor.
   *
   * @param context
   *   The executor instance context.
   * @return
   *   A new executor instance.
   */
  Executor createExecutor(InstanceContext<Executor> context);

  /**
   * Creates a worker.
   *
   * @param context
   *   The worker instance context.
   * @return
   *   A new worker instance.
   */
  Worker createWorker(InstanceContext<Worker> context);

}
