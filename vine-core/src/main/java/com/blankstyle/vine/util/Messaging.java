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
package com.blankstyle.vine.util;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.VineException;

/**
 * Messaging related utility functions.
 *
 * @author Jordan Halterman
 */
public final class Messaging {

  /**
   * Checks an asynchronous eventbus response for errors.
   *
   * @param message
   *   The response message.
   * @param resultHandler
   *   An asynchronous result handler.
   */
  public static <T> void checkResponse(Message<JsonObject> message, Handler<AsyncResult<T>> resultHandler) {
    Future<T> future = new DefaultFutureResult<T>().setHandler(resultHandler);
    JsonObject body = message.body();
    String error = body.getString("error");
    if (error != null) {
      future.setFailure(new VineException(error));
    }
    else {
      future.setResult(null);
    }
  }

  /**
   * Checks an asynchronous eventbus response for errors.
   *
   * @param message
   *   The response message.
   * @param resultHandler
   *   An asynchronous result handler.
   * @param result
   *   A response result.
   */
  public static <T> void checkResponse(Message<JsonObject> message, Handler<AsyncResult<T>> resultHandler, T result) {
    Future<T> future = new DefaultFutureResult<T>().setHandler(resultHandler);
    JsonObject body = message.body();
    String error = body.getString("error");
    if (error != null) {
      future.setFailure(new VineException(error));
    }
    else {
      future.setResult(result);
    }
  }

  /**
   * Checks an asynchronous eventbus response for errors, invoking a handler
   * if no errors occurred.
   *
   * @param message
   *   The response message.
   * @param resultHandler
   *   An asynchronous result handler.
   * @param validHandler
   *   A handler to be invoked if no errors occurred.
   */
  public static <T> void checkResponse(Message<JsonObject> message, Handler<AsyncResult<T>> resultHandler, Handler<Void> validHandler) {
    String error = message.body().getString("error");
    if (error != null) {
      new DefaultFutureResult<T>().setHandler(resultHandler).setFailure(new VineException(error));
    }
    else {
      validHandler.handle(null);
    }
  }

  /**
   * Checks an asynchronous eventbus response for errors.
   *
   * @param response
   *   An asynchronous message response.
   * @param resultHandler
   *   An asynchronous result handler.
   */
  public static <T> void checkResponse(AsyncResult<Message<JsonObject>> response, Handler<AsyncResult<T>> resultHandler) {
    Future<T> future = new DefaultFutureResult<T>().setHandler(resultHandler);
    if (response.failed()) {
      future.setFailure(response.cause());
    }
    else {
      JsonObject body = response.result().body();
      String error = body.getString("error");
      if (error != null) {
        future.setFailure(new VineException(error));
      }
      else {
        future.setResult(null);
      }
    }
  }

  /**
   * Checks an asynchronous eventbus response for errors.
   *
   * @param response
   *   An asynchronous message response.
   * @param resultHandler
   *   An asynchronous result handler.
   * @param result
   *   A response result.
   */
  public static <T> void checkResponse(AsyncResult<Message<JsonObject>> response, Handler<AsyncResult<T>> resultHandler, T result) {
    Future<T> future = new DefaultFutureResult<T>().setHandler(resultHandler);
    if (response.failed()) {
      future.setFailure(response.cause());
    }
    else {
      JsonObject body = response.result().body();
      String error = body.getString("error");
      if (error != null) {
        future.setFailure(new VineException(error));
      }
      else {
        future.setResult(result);
      }
    }
  }

  /**
   * Checks an asynchronous eventbus response for errors, invoking a handler
   * if no errors occurred.
   *
   * @param response
   *   An asynchronous message response.
   * @param resultHandler
   *   An asynchronous result handler.
   * @param validHandler
   *   A handler to be invoked if no errors occurred.
   */
  public static <T> void checkResponse(AsyncResult<Message<JsonObject>> response, Handler<AsyncResult<T>> resultHandler, Handler<Void> validHandler) {
    if (response.failed()) {
      new DefaultFutureResult<T>().setHandler(resultHandler).setFailure(response.cause());
    }
    else {
      String error = response.result().body().getString("error");
      if (error != null) {
        new DefaultFutureResult<T>().setHandler(resultHandler).setFailure(new VineException(error));
      }
      else {
        validHandler.handle(null);
      }
    }
  }

}
