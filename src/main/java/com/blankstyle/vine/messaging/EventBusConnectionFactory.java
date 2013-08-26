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

import org.vertx.java.core.Vertx;

/**
 * An EventBusConnection factory.
 *
 * @author Jordan Halterman
 */
public class EventBusConnectionFactory implements ConnectionFactory<EventBusConnection> {

  @Override
  public EventBusConnection createConnection(String address, Vertx vertx) {
    return new EventBusConnection(address, vertx.eventBus());
  }

}
