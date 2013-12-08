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
  private MessageId createNewId(String auditor) {
    return new DefaultMessageId(new JsonObject()
        .putNumber(DefaultMessageId.CODE, generateCode())
        .putString(DefaultMessageId.ID, nextId())
        .putString(DefaultMessageId.OWNER, address)
        .putString(DefaultMessageId.AUDITOR, auditor));
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
  private MessageId createChildId(MessageId parentId) {
    if (parentId.isRoot()) {
      return new DefaultMessageId(new JsonObject()
          .putNumber(DefaultMessageId.CODE, generateCode())
          .putString(DefaultMessageId.ID, nextId())
          .putString(DefaultMessageId.PARENT, parentId.correlationId())
          .putString(DefaultMessageId.ROOT, parentId.correlationId())
          .putString(DefaultMessageId.OWNER, address)
          .putString(DefaultMessageId.AUDITOR, parentId.auditor()));
    }
    else {
      return new DefaultMessageId(new JsonObject()
          .putNumber(DefaultMessageId.CODE, generateCode())
          .putString(DefaultMessageId.ID, nextId())
          .putString(DefaultMessageId.PARENT, parentId.correlationId())
          .putString(DefaultMessageId.ROOT, parentId.root())
          .putString(DefaultMessageId.OWNER, address)
          .putString(DefaultMessageId.AUDITOR, parentId.auditor()));
    }
  }

  /**
   * Creates a copy of the message with a new ID.
   */
  public JsonMessageStruct createCopy(JsonMessage sibling) {
    return new JsonMessageStruct(sibling.toJson().copy())
        .setMessageId(createSiblingId(sibling.messageId()));
  }

  /**
   * Creates a sibling message ID.
   */
  private MessageId createSiblingId(MessageId siblingId) {
    return new DefaultMessageId(siblingId.toJson().copy()
        .putNumber(DefaultMessageId.CODE, generateCode())
        .putString(DefaultMessageId.ID, nextId()));
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
    private final JsonObject structure;

    public JsonMessageStruct() {
      this.structure = new JsonObject();
    }

    public JsonMessageStruct(JsonObject structure) {
      this.structure = structure;
    }

    /**
     * Sets the message ID.
     *
     * @param messageId
     *   The message ID.
     * @return
     *   The called structure.
     */
    public JsonMessageStruct setMessageId(MessageId messageId) {
      structure.putObject(DefaultJsonMessage.ID, messageId.toJson());
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
      structure.putObject(DefaultJsonMessage.BODY, body);
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
      structure.putString(DefaultJsonMessage.STREAM, stream);
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
      structure.putString(DefaultJsonMessage.SOURCE, source);
      return this;
    }

    /**
     * Returns a new JSON message from the built message structure.
     *
     * @return
     *   A new {@link JsonMessage} instance.
     */
    public JsonMessage toMessage() {
      return DefaultJsonMessage.fromJson(structure);
    }
  }

}
