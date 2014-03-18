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
package net.kuujo.vertigo.context;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Registers contexts with the cluster.
 *
 * @author Jordan Halterman
 */
public interface ContextRegistry {

  /**
   * Registers a network context.
   *
   * @param network The network context to register.
   * @param doneHandler An asynchronous handler to be called once the context has been registered.
   * @return The context registry.
   */
  ContextRegistry registerContext(NetworkContext network, Handler<AsyncResult<NetworkContext>> doneHandler);

  /**
   * Unregisters a network context.
   *
   * @param network The network context to unregister.
   * @param doneHandler An asynchronous handler to be called once the context has been unregistered.
   * @return The context registry.
   */
  ContextRegistry unregisterContext(NetworkContext network, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Registers a component context.
   *
   * @param component The component context to register.
   * @param doneHandler An asynchronous handler to be called once the context has been registered.
   * @return The context registry.
   */
  <T extends ComponentContext<T>> ContextRegistry registerContext(T component, Handler<AsyncResult<T>> doneHandler);

  /**
   * Unregisters a component context.
   *
   * @param component The component context to unregister.
   * @param doneHandler An asynchronous handler to be called once the context has been unregistered.
   * @return The context registry.
   */
  <T extends ComponentContext<T>> ContextRegistry unregisterContext(T component, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Registers an instance context.
   *
   * @param instance The instance context to register.
   * @param doneHandler An asynchronous handler to be called once the context has been registered.
   * @return The context registry.
   */
  ContextRegistry registerContext(InstanceContext instance, Handler<AsyncResult<InstanceContext>> doneHandler);

  /**
   * Unregisters an instance context.
   *
   * @param instance The instance context to unregister.
   * @param doneHandler An asynchronous handler to be called once the context has been unregistered.
   * @return The context registry.
   */
  ContextRegistry unregisterContext(InstanceContext instance, Handler<AsyncResult<Void>> doneHandler);

}
