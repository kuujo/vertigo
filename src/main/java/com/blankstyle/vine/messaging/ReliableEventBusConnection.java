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

import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.eventbus.Message;

import com.blankstyle.vine.eventbus.ReliableEventBus;

/**
 * A reliable eventbus connection.
 *
 * @author Jordan Halterman
 */
public class ReliableEventBusConnection extends EventBusConnection implements ReliableConnection {

  private ReliableEventBus eventBus;

  public ReliableEventBusConnection(String address, ReliableEventBus eventBus) {
    super(address);
    this.eventBus = eventBus;
  }

  @Override
  public Connection send(JsonMessage message, AsyncResultHandler<Message<Void>> replyHandler) {
    eventBus.send(address, message.serialize(), replyHandler);
    return this;
  }

  @Override
  public Connection send(JsonMessage message, long timeout, AsyncResultHandler<Message<Void>> replyHandler) {
    eventBus.send(address, message.serialize(), timeout, replyHandler);
    return this;
  }

  @Override
  public Connection send(JsonMessage message, long timeout, boolean retry, AsyncResultHandler<Message<Void>> replyHandler) {
    eventBus.send(address, message.serialize(), timeout, retry, replyHandler);
    return this;
  }

  @Override
  public Connection send(JsonMessage message, long timeout, boolean retry, int attempts,
      AsyncResultHandler<Message<Void>> replyHandler) {
    eventBus.send(address, message.serialize(), timeout, retry, attempts, replyHandler);
    return this;
  }

}
