/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.worker.impl;

import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.component.impl.AbstractComponent;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.worker.Worker;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A basic worker implementation.
 *
 * @author Jordan Halterman
 */
public class BasicWorker extends AbstractComponent<Worker> implements Worker {

  @Factory
  public static BasicWorker factory(String network, String address, Vertx vertx, Container container, VertigoCluster cluster) {
    return new BasicWorker(network, address, vertx, container, cluster);
  }

  protected Handler<JsonMessage> messageHandler;

  public BasicWorker(String network, String address, Vertx vertx, Container container, VertigoCluster cluster) {
    super(network, address, vertx, container, cluster);
  }

  @Override
  public Worker start(final Handler<AsyncResult<Worker>> doneHandler) {
    return super.start(new Handler<AsyncResult<Worker>>() {
      @Override
      public void handle(AsyncResult<Worker> result) {
        if (result.failed()) {
          new DefaultFutureResult<Worker>(result.cause()).setHandler(doneHandler);
        }
        else {
          input.messageHandler(messageHandler);
          new DefaultFutureResult<Worker>(BasicWorker.this).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public Worker messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    if (input != null) {
      input.messageHandler(messageHandler);
    }
    return this;
  }

  @Override
  public String emit(JsonObject data) {
    return output.emit(data);
  }

  @Override
  public String emit(JsonObject data, JsonMessage parent) {
    return output.emit(data, parent);
  }

  @Override
  public String emit(JsonMessage message) {
    return output.emit(message);
  }

  @Override
  public String emit(String stream, JsonObject data) {
    return output.emitTo(stream, data);
  }

  @Override
  public String emit(String stream, JsonObject data, JsonMessage parent) {
    return output.emitTo(stream, data, parent);
  }

  @Override
  public String emit(String stream, JsonMessage message) {
    return output.emitTo(stream, message);
  }

  @Override
  public void ack(JsonMessage message) {
    input.ack(message);
  }

  @Override
  public void fail(JsonMessage message) {
    input.fail(message);
  }

}
