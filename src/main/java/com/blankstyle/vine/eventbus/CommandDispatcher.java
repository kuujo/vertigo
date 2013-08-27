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

import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

import com.blankstyle.vine.Context;

/**
 * An remote command dispatcher.
 *
 * @author Jordan Halterman
 */
public interface CommandDispatcher {

  /**
   * Sets the dispatcher vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   */
  public void setVertx(Vertx vertx);

  /**
   * Gets the dispatcher vertx instance.
   *
   * @return
   *   The dispatcher vertx instance.
   */
  public Vertx getVertx();

  /**
   * Sets the dispatcher eventbus.
   *
   * @param eventBus
   *   The dispatcher eventbus.
   */
  public void setEventBus(EventBus eventBus);

  /**
   * Gets the dispatcher eventbus.
   *
   * @return
   *   The dispatcher eventbus.
   */
  public EventBus getEventBus();

  /**
   * Sets the dispatcher context.
   *
   * @param context
   *   The dispatcher context.
   */
  public void setContext(Context<?> context);

  /**
   * Gets the dispatcher context.
   *
   * @return
   *   The dispatcher context.
   */
  public Context<?> getContext();

  /**
   * Registers a new command action.
   *
   * @param name
   *   The name of the action.
   * @param action
   *   The action to register.
   */
  public void registerAction(String name, Class<? extends Action<?>> action);

  /**
   * Unregisters a command action.
   *
   * @param name
   *   The name of the action to unregister.
   */
  public void unregisterAction(String name);

  /**
   * Dispatches a command.
   *
   * @param command
   *   The command to dispatch.
   * @param resultHandler
   *   A result handler to be invoked with the dispatched command result.
   */
  public void dispatch(Command command, Handler<AsyncResult<Object>> resultHandler);

  /**
   * Dispatches a command by action name and arguments.
   *
   * @param actionName
   *   The action name.
   * @param args
   *   An array of action arguments.
   * @param resultHandler
   *   A result handler to be invoked with the dispatched command result.
   */
  public void dispatch(String actionName, Map<String, Object> args, Handler<AsyncResult<Object>> resultHandler);

}
