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
package net.kuujo.vertigo.worker;

import net.kuujo.vertigo.component.ComponentBase;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A basic worker implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultBasicWorker extends ComponentBase<BasicWorker> implements BasicWorker {
  protected Handler<JsonMessage> messageHandler;

  public DefaultBasicWorker(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
  }

  @Override
  public BasicWorker messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    input.messageHandler(messageHandler);
    return this;
  }

  @Override
  public MessageId emit(JsonObject data) {
    return output.emit(data);
  }

  @Override
  public MessageId emit(JsonObject data, String tag) {
    return output.emit(data, tag);
  }

  @Override
  public MessageId emit(JsonObject data, JsonMessage parent) {
    return output.emit(data, parent);
  }

  @Override
  public MessageId emit(JsonObject data, String tag, JsonMessage parent) {
    return output.emit(data, tag, parent);
  }

  @Override
  public MessageId emit(JsonMessage message) {
    return output.emit(message);
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
