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
package net.kuujo.vine.messaging;

import org.vertx.java.core.Handler;

/**
 * An eventbus-based connection.
 *
 * @author Jordan Halterman
 */
public class MessageBusConnection implements Connection {

  protected String address;

  protected MessageBus messageBus;

  public MessageBusConnection(String address) {
    this.address = address;
  }

  public MessageBusConnection(String address, MessageBus messageBus) {
    this.address = address;
    this.messageBus = messageBus;
  }

  public void setMessageBus(MessageBus messageBus) {
    this.messageBus = messageBus;
  }

  public MessageBus getMessageBus() {
    return messageBus;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public Connection send(JsonMessage message) {
    messageBus.send(address, message);
    return this;
  }

  @Override
  public Connection send(JsonMessage message, Handler<Boolean> ackHandler) {
    messageBus.send(address, message, ackHandler);
    return this;
  }

}
