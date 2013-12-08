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
package net.kuujo.vertigo.output.impl;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.output.Connection;

import org.vertx.java.core.eventbus.EventBus;

/**
 * A default connection.
 *
 * @author Jordan Halterman
 */
public class DefaultConnection implements Connection {
  protected String address;
  protected EventBus eventBus;

  public DefaultConnection(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public MessageId write(JsonMessage message) {
    eventBus.send(address, message.toJson());
    return message.messageId();
  }

}
