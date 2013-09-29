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
package net.kuujo.vitis.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.kuujo.vitis.messaging.Channel;
import net.kuujo.vitis.messaging.JsonMessage;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/**
 * A linear output collector implementation.
 *
 * @author Jordan Halterman
 */
public class LinearOutputCollector implements OutputCollector {

  private String auditAddress;

  private EventBus eventBus;

  private List<Channel> channels = new ArrayList<Channel>();

  public LinearOutputCollector(String auditAddress, EventBus eventBus) {
    this.auditAddress = auditAddress;
    this.eventBus = eventBus;
  }

  @Override
  public OutputCollector addChannel(Channel channel) {
    if (!channels.contains(channel)) {
      channels.add(channel);
    }
    return this;
  }

  @Override
  public OutputCollector removeChannel(Channel channel) {
    if (channels.contains(channel)) {
      channels.remove(channel);
    }
    return this;
  }

  @Override
  public int size() {
    return channels.size();
  }

  @Override
  public OutputCollector emit(JsonMessage message) {
    Iterator<Channel> iter = channels.iterator();
    String parent = message.parent();
    boolean hasParent = parent != null;
    while (iter.hasNext()) {
      // If a parent exists then send a fork message to the acker task.
      if (hasParent) {
        eventBus.publish(auditAddress, createForkAction(message.id(), parent));
      }
      // Otherwise, send a new action to the acker task.
      else {
        eventBus.publish(auditAddress, createNewAction(message.id()));
      }

      // Write the message to the channel.
      iter.next().write(message);

      // Create a new copy of the message if another iteration remains.
      if (iter.hasNext()) {
        message = message.copy();
      }
    }
    return this;
  }

  @Override
  public OutputCollector emit(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      emit(message);
    }
    return this;
  }

  @Override
  public OutputCollector ack(JsonMessage message) {
    eventBus.publish(auditAddress, createAckAction(message.id()));
    return this;
  }

  @Override
  public OutputCollector ack(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      ack(message);
    }
    return this;
  }

  @Override
  public OutputCollector fail(JsonMessage message) {
    eventBus.publish(auditAddress, createFailAction(message.id()));
    return this;
  }

  @Override
  public OutputCollector fail(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      fail(message);
    }
    return this;
  }

  /**
   * Creates a new message tree action.
   */
  private static final JsonObject createNewAction(String id) {
    return new JsonObject().putString("action", "create").putString("id", id);
  }

  /**
   * Creates a forked message tree action.
   */
  private static final JsonObject createForkAction(String id, String parent) {
    return new JsonObject().putString("action", "fork").putString("id", id).putString("parent", parent);
  }

  /**
   * Creates an ack message action.
   */
  private static final JsonObject createAckAction(String id) {
    return new JsonObject().putString("action", "ack").putString("id", id);
  }

  /**
   * Creates a fail message action.
   */
  private static final JsonObject createFailAction(String id) {
    return new JsonObject().putString("action", "fail").putString("id", id);
  }

}
