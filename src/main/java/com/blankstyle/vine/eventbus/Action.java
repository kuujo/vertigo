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
package com.blankstyle.vine.eventbus;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

import com.blankstyle.vine.Context;

/**
 * An event bus command.
 *
 * @author Jordan Halterman
 */
public abstract class Action<T extends Context<T>> {

  protected Vertx vertx;

  protected EventBus eventBus;

  protected T context;

  public Action() {
  }

  public Action(Vertx vertx, EventBus eventBus, T context) {
    this.vertx = vertx;
    this.eventBus = eventBus;
    this.context = context;
  }

  /**
   * Sets the action vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   */
  public Action<T> setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  /**
   * Gets the action vertx instance.
   *
   * @return
   *   The action vertx instance.
   */
  public Vertx getVertx() {
    return vertx;
  }

  /**
   * Sets the action eventbus.
   *
   * @param eventBus
   *   The action event bus.
   * @return
   *   The called action instance.
   */
  public Action<T> setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    return this;
  }

  /**
   * Gets the action event bus.
   *
   * @return
   *   The action eventbus.
   */
  public EventBus getEventBus() {
    return eventBus;
  }

  /**
   * Sets the action context.
   *
   * @param context
   *   The action context.
   * @return
   *   The called action instance.
   */
  public Action<T> setContext(T context) {
    this.context = context;
    return this;
  }

  /**
   * Gets the action context.
   *
   * @return
   *   The action context.
   */
  public Context<T> getContext() {
    return context;
  }

  /**
   * Returns an action arguments definition.
   *
   * @return
   *   An action arguments definition.
   */
  public abstract ArgumentsDefinition getArgumentsDefinition();

}
