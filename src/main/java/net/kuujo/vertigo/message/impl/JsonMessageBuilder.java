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
package net.kuujo.vertigo.message.impl;

import java.util.Map;
import java.util.Random;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.json.JsonObject;

/**
 * A JSON message builder.
 *
 * @author Jordan Halterman
 */
public final class JsonMessageBuilder {
  private final String address;
  private final String prefix;
  private long counter;
  private final Random random = new Random();

  public JsonMessageBuilder(String address) {
    this.address = address;
    this.prefix = address + ":";
  }

  /**
   * Creates a new message.
   */
  public JsonMessageStruct createNew(String auditor) {
    return new JsonMessageStruct().setMessageId(createNewId(auditor));
  }

  /**
   * Creates a new message ID.
   */
  private DefaultMessageId createNewId(String auditor) {
    DefaultMessageId messageId = new DefaultMessageId();
    messageId.code = generateCode();
    messageId.id = nextId();
    messageId.owner = address;
    messageId.auditor = auditor;
    return messageId;
  }

  /**
   * Creates a child message.
   */
  public JsonMessageStruct createChild(JsonMessage parent) {
    return new JsonMessageStruct().setMessageId(createChildId(parent.messageId()))
        .setSource(parent.source());
  }

  /**
   * Creates a child message ID.
   */
  private DefaultMessageId createChildId(MessageId parentId) {
    DefaultMessageId messageId = new DefaultMessageId();
    messageId.code = generateCode();
    messageId.id = nextId();
    messageId.parent = parentId.correlationId();
    messageId.root = parentId.root();
    messageId.owner = address;
    messageId.auditor = parentId.auditor();
    return messageId;
  }

  /**
   * Creates a copy of the message with a new ID.
   */
  public JsonMessageStruct createCopy(JsonMessage sibling) {
    DefaultJsonMessage message = (DefaultJsonMessage) sibling;
    return new JsonMessageStruct()
        .setMessageId(createSiblingId(sibling.messageId()))
        .setBody(message.body)
        .setStream(message.stream)
        .setSource(message.source);
  }

  /**
   * Creates a sibling message ID.
   */
  private DefaultMessageId createSiblingId(MessageId siblingId) {
    DefaultMessageId messageId = new DefaultMessageId();
    messageId.code = generateCode();
    messageId.id = nextId();
    messageId.parent = siblingId.parent();
    messageId.root = siblingId.root();
    messageId.owner = address;
    messageId.auditor = siblingId.auditor();
    return messageId;
  }

  /**
   * Generates a message ack code.
   */
  private int generateCode() {
    return random.nextInt();
  }

  /**
   * Generates a unique message ID.
   */
  private String nextId() {
    return prefix + ++counter;
  }

  /**
   * A Json message structure.
   */
  public static final class JsonMessageStruct {
    private final DefaultJsonMessage message;

    public JsonMessageStruct() {
      this.message = new DefaultJsonMessage();
    }

    public JsonMessageStruct(DefaultJsonMessage message) {
      this.message = message;
    }

    /**
     * Sets the message ID.
     *
     * @param messageId
     *   The message ID.
     * @return
     *   The called structure.
     */
    public JsonMessageStruct setMessageId(DefaultMessageId messageId) {
      message.id = messageId;
      return this;
    }

    /**
     * Sets the message body.
     *
     * @param body
     *   The message body.
     * @return
     *   The called structure.
     */
    public JsonMessageStruct setBody(JsonObject body) {
      message.body = body.toMap();
      return this;
    }

    /**
     * Sets the message body.
     *
     * @param body
     *   The message body.
     * @return
     *   The called structure.
     */
    public JsonMessageStruct setBody(Map<String, Object> body) {
      message.body = body;
      return this;
    }

    /**
     * Sets the message stream.
     *
     * @param stream
     *   The message stream.
     * @return
     *   The called structure.
     */
    public JsonMessageStruct setStream(String stream) {
      message.stream = stream;
      return this;
    }

    /**
     * Sets the message source.
     *
     * @param source
     *   The message source.
     * @return
     *   The called structure.
     */
    public JsonMessageStruct setSource(String source) {
      message.source = source;
      return this;
    }

    /**
     * Returns a new JSON message from the built message structure.
     *
     * @return
     *   A new {@link JsonMessage} instance.
     */
    public JsonMessage toMessage() {
      return message;
    }
  }

}
