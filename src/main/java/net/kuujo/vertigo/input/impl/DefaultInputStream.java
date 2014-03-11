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
package net.kuujo.vertigo.input.impl;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

import net.kuujo.vertigo.context.InputStreamContext;
import net.kuujo.vertigo.input.InputConnection;
import net.kuujo.vertigo.input.InputStream;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * Default input stream implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputStream implements InputStream {
  private final InputStreamContext context;
  private InputConnection connection;

  public DefaultInputStream(Vertx vertx, InputStreamContext context) {
    this.context = context;
    connection = new DefaultInputConnection(context.connection().address(), vertx);
  }

  @Override
  public InputStreamContext context() {
    return context;
  }

  @Override
  public InputStream messageHandler(Handler<JsonMessage> handler) {
    connection.messageHandler(handler);
    return this;
  }

  @Override
  public InputStream start() {
    connection.open();
    return this;
  }

  @Override
  public InputStream start(Handler<AsyncResult<Void>> doneHandler) {
    connection.open(doneHandler);
    return this;
  }

  @Override
  public void stop() {
    connection.close();
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    connection.close(doneHandler);
  }

}
