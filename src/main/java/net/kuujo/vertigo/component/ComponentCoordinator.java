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

import net.kuujo.vertigo.context.InstanceContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * The component coordinator handles coordination of component startup and
 * shutdown between multiple components within a network. This is necessary
 * to ensure that messages are not lost by components sending or receiving
 * messages prior to completion of startup.
 *
 * @author Jordan Halterman
 */
public interface ComponentCoordinator {

  /**
   * Returns the instance address.
   *
   * @return The component instance address.
   */
  String address();

  /**
   * Starts the coordinator.<p>
   *
   * When the coordinator is started the instance context is retrieved from
   * the fault-tolerant cluster. Prior to setup, the component only knows its
   * own address, thus starting the coordinator is essential to component setup.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   *                    The handler will be called with the current instance context.
   * @return The coordinator.
   */
  ComponentCoordinator start(Handler<AsyncResult<InstanceContext>> doneHandler);

  /**
   * Notifies the network that the component is ready to resume.
   *
   * @return The component coordinator.
   */
  ComponentCoordinator resume();

  /**
   * Notifies the network that the component is ready to resume.
   *
   * @param doneHandler An asynchronous handler to be called once the network has been notified.
   * @return The component coordinator.
   */
  ComponentCoordinator resume(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Notifies the network that the component is not ready for operation.
   *
   * @return The component coordinator.
   */
  ComponentCoordinator pause();

  /**
   * Notifies the network that the component is not ready for operation.
   *
   * @param doneHandler An asynchronous handler to be called once the network has been notified.
   * @return The component coordinator.
   */
  ComponentCoordinator pause(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sets a resume handler on the coordinator.
   *
   * @param handler A handler to be called when the component should resume operation.
   * @return The component coordinator.
   */
  ComponentCoordinator resumeHandler(Handler<Void> handler);

  /**
   * Sets a pause hadler on the coordinator.
   *
   * @param handler A handler to be called when the component should pause operation.
   * @return The component coordinator.
   */
  ComponentCoordinator pauseHandler(Handler<Void> handler);

  /**
   * Stops the coordinator.
   *
   * @param doneHandler
   *   An asynchronous handler to be called once complete.
   */
  void stop(Handler<AsyncResult<Void>> doneHandler);

}
