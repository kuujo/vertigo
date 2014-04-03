/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.message.impl;

import java.util.Map;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageAcker;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A default JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class ReliableJsonMessage implements JsonMessage {
  @JsonIgnore
  private MessageAcker acker;
  private String id;
  private Map<String, Object> body;
  private String parent;
  private String root;

  private ReliableJsonMessage() {
  }

  public ReliableJsonMessage setAcker(MessageAcker acker) {
    this.acker = acker;
    return this;
  }

  public void anchor(JsonMessage child) {
    acker.anchor(child);
  }

  public void ack() {
    acker.ack();
  }

  public void timeout() {
    acker.timeout();
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public JsonObject body() {
    return new JsonObject(body);
  }

  @Override
  public String parent() {
    return parent;
  }

  @Override
  public String root() {
    return root;
  }

  @Override
  public ReliableJsonMessage copy(String id) {
    return Builder.newBuilder()
        .setId(id)
        .setBody(body)
        .setParent(parent)
        .setRoot(root)
        .build();
  }

  /**
   * Json message builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder {
    private ReliableJsonMessage message = new ReliableJsonMessage();

    /**
     * Creates a new message builder.
     *
     * @return A new message builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Sets the message iD.
     *
     * @param messageId The message ID.
     * @return The message builder.
     */
    public Builder setId(String id) {
      message.id = id;
      return this;
    }

    /**
     * Sets the message body.
     *
     * @param body The message body.
     * @return The message builder.
     */
    public Builder setBody(JsonObject body) {
      message.body = body.toMap();
      return this;
    }

    /**
     * Sets the message body.
     *
     * @param body The message body.
     * @return The message builder.
     */
    public Builder setBody(Map<String, Object> body) {
      message.body = body;
      return this;
    }

    /**
     * Sets the parent message ID.
     *
     * @param parent The parent message ID.
     * @return The message builder.
     */
    public Builder setParent(String parent) {
      message.parent = parent;
      return this;
    }

    /**
     * Sets the root message ID.
     *
     * @param root The root message ID.
     * @return The message builder.
     */
    public Builder setRoot(String root) {
      message.root = root;
      return this;
    }

    /**
     * Builds the message.
     *
     * @return A new message.
     */
    public ReliableJsonMessage build() {
      return message;
    }

  }

}
