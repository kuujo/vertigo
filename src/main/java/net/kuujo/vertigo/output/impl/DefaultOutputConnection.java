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
package net.kuujo.vertigo.output.impl;

import net.kuujo.vertigo.context.ConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * Default output connection implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputConnection implements OutputConnection {
  private static final Serializer serializer = SerializerFactory.getSerializer(JsonMessage.class);
  private final String address;
  private final EventBus eventBus;

  public DefaultOutputConnection(String address, Vertx vertx) {
    this.address = address;
    this.eventBus = vertx.eventBus();
  }

  public DefaultOutputConnection(Vertx vertx, ConnectionContext context) {
    this.eventBus = vertx.eventBus();
    this.address = context.address();
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public MessageId write(JsonMessage message) {
    eventBus.send(address, serializer.serializeToString(message));
    return message.messageId();
  }

}
