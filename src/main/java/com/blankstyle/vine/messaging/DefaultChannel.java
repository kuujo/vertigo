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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A default channel implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultChannel implements ReliableChannel {

  protected Dispatcher dispatcher;

  protected ConnectionPool connections = new DefaultConnectionPool();

  public DefaultChannel(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public void setDispatcher(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public Dispatcher getDispatcher() {
    return dispatcher;
  }

  @Override
  public void addConnection(Connection connection) {
    if (!connections.contains(connection)) {
      connections.add(connection);
    }
    dispatcher.init(connections);
  }

  @Override
  public void removeConnection(Connection connection) {
    if (connections.contains(connection)) {
      connections.remove(connection);
    }
    dispatcher.init(connections);
  }

  @Override
  public void publish(Object message) {
    dispatcher.dispatch(new JsonMessage<Object>().setBody(message));
  }

  @Override
  public void publish(JsonObject message) {
    dispatcher.dispatch(new JsonMessage<JsonObject>().setBody(message));
  }

  @Override
  public void publish(JsonArray message) {
    dispatcher.dispatch(new JsonMessage<JsonArray>().setBody(message));
  }

  @Override
  public void publish(Buffer message) {
    dispatcher.dispatch(new JsonMessage<Buffer>().setBody(message));
  }

  @Override
  public void publish(byte[] message) {
    dispatcher.dispatch(new JsonMessage<byte[]>().setBody(message));
  }

  @Override
  public void publish(String message) {
    dispatcher.dispatch(new JsonMessage<String>().setBody(message));
  }

  @Override
  public void publish(Integer message) {
    dispatcher.dispatch(new JsonMessage<Integer>().setBody(message));
  }

  @Override
  public void publish(Long message) {
    dispatcher.dispatch(new JsonMessage<Long>().setBody(message));
  }

  @Override
  public void publish(Float message) {
    dispatcher.dispatch(new JsonMessage<Float>().setBody(message));
  }

  @Override
  public void publish(Double message) {
    dispatcher.dispatch(new JsonMessage<Double>().setBody(message));
  }

  @Override
  public void publish(Boolean message) {
    dispatcher.dispatch(new JsonMessage<Boolean>().setBody(message));
  }

  @Override
  public void publish(Short message) {
    dispatcher.dispatch(new JsonMessage<Short>().setBody(message));
  }

  @Override
  public void publish(Character message) {
    dispatcher.dispatch(new JsonMessage<Character>().setBody(message));
  }

  @Override
  public void publish(Byte message) {
    dispatcher.dispatch(new JsonMessage<Byte>().setBody(message));
  }

  @Override
  public void publish(Object message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Object>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Object message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Object>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Object message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Object>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Object message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Object>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(JsonObject message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<JsonObject>().setBody(message), resultHandler);
  }

  @Override
  public void publish(JsonObject message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<JsonObject>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(JsonObject message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<JsonObject>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(JsonObject message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<JsonObject>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(JsonArray message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<JsonArray>().setBody(message), resultHandler);
  }

  @Override
  public void publish(JsonArray message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<JsonArray>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(JsonArray message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<JsonArray>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(JsonArray message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<JsonArray>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Buffer message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Buffer>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Buffer message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Buffer>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Buffer message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Buffer>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Buffer message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Buffer>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(byte[] message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<byte[]>().setBody(message), resultHandler);
  }

  @Override
  public void publish(byte[] message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<byte[]>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(byte[] message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<byte[]>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(byte[] message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<byte[]>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(String message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<String>().setBody(message), resultHandler);
  }

  @Override
  public void publish(String message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<String>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(String message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<String>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(String message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<String>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Integer message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Integer>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Integer message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Integer>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Integer message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Integer>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Integer message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Integer>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Long message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Long>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Long message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Long>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Long message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Long>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Long message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Long>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Float message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Float>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Float message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Float>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Float message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Float>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Float message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Float>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Double message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Double>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Double message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Double>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Double message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Double>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Double message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Double>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Boolean message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Boolean>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Boolean message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Boolean>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Boolean message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Boolean>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Boolean message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Boolean>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Short message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Short>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Short message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Short>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Short message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Short>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Short message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Short>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Character message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Character>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Character message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Character>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Character message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Character>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Character message, long timeout, boolean retry, int attempts,
      Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Character>().setBody(message), timeout, retry, attempts, resultHandler);
  }

  @Override
  public void publish(Byte message, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Byte>().setBody(message), resultHandler);
  }

  @Override
  public void publish(Byte message, long timeout, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Byte>().setBody(message), timeout, resultHandler);
  }

  @Override
  public void publish(Byte message, long timeout, boolean retry, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Byte>().setBody(message), timeout, retry, resultHandler);
  }

  @Override
  public void publish(Byte message, long timeout, boolean retry, int attempts, Handler<AsyncResult<Void>> resultHandler) {
    dispatcher.dispatch(new JsonMessage<Byte>().setBody(message), timeout, retry, attempts, resultHandler);
  }

}
