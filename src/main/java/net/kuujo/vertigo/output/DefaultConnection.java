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
package net.kuujo.vertigo.output;

import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/**
 * An eventbus-based connection.
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
  public Connection write(JsonMessage message) {
    eventBus.send(address, Serializer.serialize(audit(message)));
    return this;
  }

  /**
   * Sends audit info to the message auditor.
   */
  protected JsonMessage audit(JsonMessage message) {
    String auditor = message.auditor();
    if (auditor != null) {
      String parent = message.parent();
      if (parent != null) {
        eventBus.send(auditor, new JsonObject().putString("action", "fork")
            .putString("id", message.id()).putString("parent", parent));
      }
    }
    return message;
  }

}
