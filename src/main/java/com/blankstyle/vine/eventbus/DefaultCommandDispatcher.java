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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.impl.DefaultFutureResult;

import com.blankstyle.vine.Context;

/**
 * A default remote command dispatcher implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultCommandDispatcher implements CommandDispatcher {

  private EventBus eventBus;

  private Context<?> context;

  private Map<String, Class<? extends Action<?>>> actions = new HashMap<String, Class<? extends Action<?>>>();

  public DefaultCommandDispatcher() {
  }

  public DefaultCommandDispatcher(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public DefaultCommandDispatcher(EventBus eventBus, Context<?> context) {
    this.eventBus = eventBus;
    this.context = context;
  }

  @Override
  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public void setContext(Context<?> context) {
    this.context = context;
  }

  @Override
  public Context<?> getContext() {
    return context;
  }

  @Override
  public void registerAction(String name, Class<? extends Action<?>> action) {
    actions.put(name, action);
  }

  @Override
  public void unregisterAction(String name) {
    if (actions.containsKey(name)) {
      actions.remove(name);
    }
  }

  @Override
  public void dispatch(Command command, Handler<AsyncResult<Object>> resultHandler) {
    dispatch(command.getAction(), command.getArguments(), resultHandler);
  }

  @Override
  public void dispatch(String actionName, Object[] args, Handler<AsyncResult<Object>> resultHandler) {
    if (actions.containsKey(actionName)) {
      if (actions.containsKey(actionName)) {
        Class<? extends Action<?>> actionClass = actions.get(actionName);
        if (actionClass != null) {
          Action<?> action;
          try {
            action = actionClass.getConstructor(new Class[]{EventBus.class, Context.class}).newInstance(eventBus, context);

            // If this is a synchronous action then get the result and invoke the handler.
            if (action instanceof SynchronousAction) {
              Future<Object> future = new DefaultFutureResult<Object>().setHandler(resultHandler);
              try {
                Object result = ((SynchronousAction<?>) action).execute(args);
                future.setResult(result);
              }
              catch (Exception e) {
                future.setFailure(e);
              }
            }
            // Otherwise, pass the handler to the asynchronous action.
            else if (action instanceof AsynchronousAction) {
              ((AsynchronousAction<?>) action).execute(args, resultHandler);
            }
          } catch (InstantiationException | IllegalAccessException
              | IllegalArgumentException | InvocationTargetException
              | NoSuchMethodException | SecurityException e) {
            new DefaultFutureResult<Object>().setHandler(resultHandler).setFailure(e);
          }
        }
      }
    }
  }

}
