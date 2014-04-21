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
package net.kuujo.vertigo.io.group.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.io.OutputSerializer;
import net.kuujo.vertigo.io.connection.impl.DefaultOutputConnection;
import net.kuujo.vertigo.io.group.OutputGroup;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Connection output group implementation.<p>
 *
 * This is the output group that handles actually sending grouped messages on the
 * connection.
 *
 * @author Jordan Halterman
 */
public class ConnectionOutputGroup implements OutputGroup {
  private final String id;
  private final String parent;
  private final String name;
  private final Vertx vertx;
  private final DefaultOutputConnection connection;
  private final String address;
  private final OutputSerializer serializer;
  private Handler<Void> endHandler;
  private final Map<String, ConnectionOutputGroup> groups = new HashMap<>();
  private boolean ended;
  private boolean closed;

  public ConnectionOutputGroup(String id, String name, Vertx vertx, DefaultOutputConnection connection, OutputSerializer serializer) {
    this.id = id;
    this.name = name;
    this.parent = null;
    this.vertx = vertx;
    this.connection = connection;
    this.address = connection.context().address();
    this.serializer = serializer;
  }

  public ConnectionOutputGroup(String id, String name, String parent, Vertx vertx, DefaultOutputConnection connection, OutputSerializer serializer) {
    this.id = id;
    this.name = name;
    this.parent = parent;
    this.vertx = vertx;
    this.connection = connection;
    this.address = connection.context().address();
    this.serializer = serializer;
  }

  /**
   * Checks whether the group is complete.
   */
  private void checkEnd() {
    if (ended && !closed && groups.isEmpty()) {
      closed = true;
      connection.doSend(String.format("%s.end", address), new JsonObject().putString("group", id));
      if (endHandler != null) {
        endHandler.handle((Void) null);
      }
    }
  }

  public ConnectionOutputGroup endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    checkEnd();
    return this;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  /**
   * Starts the output group.
   */
  public OutputGroup start(final Handler<OutputGroup> doneHandler) {
    connection.doSend(String.format("%s.start", address), new JsonObject().putString("group", id).putString("name", name).putString("parent", parent));
    doneHandler.handle(this);
    return this;
  }

  @Override
  public OutputGroup setSendQueueMaxSize(int maxSize) {
    connection.setSendQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return connection.getSendQueueMaxSize();
  }

  @Override
  public int size() {
    return connection.size();
  }

  @Override
  public boolean sendQueueFull() {
    return connection.sendQueueFull();
  }

  @Override
  public OutputGroup drainHandler(Handler<Void> handler) {
    connection.drainHandler(handler);
    return this;
  }

  @Override
  public ConnectionOutputGroup group(final String name, final Handler<OutputGroup> handler) {
    final ConnectionOutputGroup group = new ConnectionOutputGroup(UUID.randomUUID().toString(), name, this.id, vertx, connection, serializer);

    // Set an end handler on the group that will remove the group
    // from the groups list and complete the group set.
    group.endHandler(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        groups.remove(name);
        checkEnd();
      }
    });

    // If there's already a group with this name in the groups list
    // then set the previous group's end handler to start this group.
    final ConnectionOutputGroup lastGroup = groups.get(name);
    if (lastGroup != null) {
      lastGroup.endHandler(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          groups.put(name, group);
          group.start(handler);
        }
      });
    } else {
      groups.put(name, group);
      group.start(handler);
    }
    return this;
  }

  /**
   * Sends a message.
   */
  private OutputGroup doSend(final Object value) {
    if (!ended) {
      connection.doSend(String.format("%s.group", address), serializer.serialize(value).putString("group", id));
    }
    return this;
  }

  @Override
  public OutputGroup send(final Object message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(String message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Boolean message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Character message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Short message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Integer message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Long message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Double message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Float message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Buffer message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(JsonObject message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(JsonArray message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(Byte message) {
    return doSend(message);
  }

  @Override
  public OutputGroup send(byte[] message) {
    return doSend(message);
  }

  @Override
  public OutputGroup end() {
    ended = true;
    checkEnd();
    return this;
  }

}
