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
package net.kuujo.vertigo.io.connection.impl;

import java.util.UUID;

import net.kuujo.vertigo.io.connection.ConnectionOutputGroup;
import net.kuujo.vertigo.io.group.OutputGroup;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Connection level output group.<p>
 *
 * This is the output group that handles actually sending grouped messages on the
 * connection
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultConnectionOutputGroup implements ConnectionOutputGroup {
  private final String id;
  private final String parent;
  private final String name;
  private final DefaultOutputConnection connection;
  private Handler<OutputGroup> startHandler;
  private Handler<Void> endHandler;
  private int children;
  private boolean started;
  private boolean ended;
  private boolean closed;

  public DefaultConnectionOutputGroup(String id, String name, DefaultOutputConnection connection) {
    this.id = id;
    this.name = name;
    this.parent = null;
    this.connection = connection;
  }

  public DefaultConnectionOutputGroup(String id, String name, String parent, DefaultOutputConnection connection) {
    this.id = id;
    this.name = name;
    this.parent = parent;
    this.connection = connection;
  }

  /**
   * Checks whether the group is complete.
   */
  private void checkEnd() {
    if (ended && !closed && children == 0) {
      closed = true;
      connection.doGroupEnd(id);
      if (endHandler != null) {
        endHandler.handle((Void) null);
      }
    }
  }

  void endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    checkEnd();
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Vertx vertx() {
    return connection.vertx();
  }

  /**
   * Starts the output group.
   */
  void start(final Handler<OutputGroup> startHandler) {
    connection.doGroupStart(id, name, parent);
    this.startHandler = startHandler;
  }

  /**
   * Called when the output connection receives a ready message from the
   * group at the other side of the connection. The ready message will be
   * sent once a message handler has been registered on the target group.
   */
  void handleStart() {
    if (!started && startHandler != null) {
      startHandler.handle(this);
      started = true;
    }
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
  public DefaultConnectionOutputGroup group(Handler<OutputGroup> handler) {
    return group(UUID.randomUUID().toString(), handler);
  }

  @Override
  public DefaultConnectionOutputGroup group(final String name, final Handler<OutputGroup> handler) {
    DefaultConnectionOutputGroup group = connection.group(name, id, handler);
    children++;
    group.endHandler(new VoidHandler() {
      @Override
      protected void handle() {
        children--;
        checkEnd();
      }
    });
    return this;
  }

  /**
   * Sends a message.
   */
  private OutputGroup doSend(final Object value) {
    if (!ended) {
      connection.doGroupSend(id, value);
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
