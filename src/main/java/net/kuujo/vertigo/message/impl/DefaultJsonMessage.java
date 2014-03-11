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
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.json.JsonObject;

/**
 * A default JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultJsonMessage implements JsonMessage {
  private MessageId id;
  private Map<String, Object> body;
  private String stream;
  private String source;

  private DefaultJsonMessage() {
  }

  @Override
  public MessageId messageId() {
    return id;
  }

  @Override
  public JsonObject body() {
    return new JsonObject(body);
  }

  @Override
  public String stream() {
    return stream;
  }

  @Override
  public String source() {
    return source;
  }

  @Override
  public JsonMessage copy() {
    return Builder.newBuilder()
        .setMessageId(id.copy())
        .setBody(body)
        .setSource(source)
        .setStream(stream)
        .build();
  }

  /**
   * Json message builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder {
    private DefaultJsonMessage message = new DefaultJsonMessage();

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
    public Builder setMessageId(MessageId messageId) {
      message.id = messageId;
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
     * Sets the message stream.
     *
     * @param stream The message stream.
     * @return The message builder.
     */
    public Builder setStream(String stream) {
      message.stream = stream;
      return this;
    }

    /**
     * Sets the message source.
     *
     * @param source The message source.
     * @return The message builder.
     */
    public Builder setSource(String source) {
      message.source = source;
      return this;
    }

    /**
     * Builds the message.
     *
     * @return A new message.
     */
    public DefaultJsonMessage build() {
      return message;
    }

  }

}
