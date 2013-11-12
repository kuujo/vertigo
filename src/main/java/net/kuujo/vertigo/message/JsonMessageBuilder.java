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
package net.kuujo.vertigo.message;

import java.util.UUID;

import net.kuujo.vertigo.serializer.SerializationException;

import org.vertx.java.core.json.JsonObject;

/**
 * A JSON message builder.
 *
 * @author Jordan Halterman
 */
public class JsonMessageBuilder {
  private JsonObject structure = new JsonObject();

  public JsonMessageBuilder() {
  }

  public JsonMessageBuilder(JsonObject body) {
    setBody(body);
  }

  public JsonMessageBuilder(JsonObject body, String tag) {
    setBody(body).setTag(tag);
  }

  /**
   * Creates a new message builder.
   *
   * @param id
   *   The message identifier.
   * @return
   *   A new message builder.
   */
  public static JsonMessageBuilder create(String id) {
    return new JsonMessageBuilder().setId(id);
  }

  /**
   * Creates a new message builder.
   *
   * @param id
   *   The message identifier.
   * @param body
   *   The message body.
   * @return
   *   A new message builder.
   */
  public static JsonMessageBuilder create(String id, JsonObject body) {
    return new JsonMessageBuilder(body).setId(id);
  }

  /**
   * Creates a new message builder.
   *
   * @param id
   *   The message identifier.
   * @param body
   *   The message body.
   * @param tag
   *   The message tag.
   * @return
   *   A new message builder.
   */
  public static JsonMessageBuilder create(String id, JsonObject body, String tag) {
    return new JsonMessageBuilder(body, tag).setId(id);
  }

  /**
   * Creates a new message builder.
   *
   * @param body
   *   The message body.
   * @return
   *   A new message builder.
   */
  public static JsonMessageBuilder create(JsonObject body) {
    return new JsonMessageBuilder(body).setId(generateRandomId());
  }

  /**
   * Creates a new message builder.
   *
   * @param body
   *   The message body.
   * @param tag
   *   The message tag.
   * @return
   *   A new message builder.
   */
  public static JsonMessageBuilder create(JsonObject body, String tag) {
    return new JsonMessageBuilder(body, tag).setId(generateRandomId());
  }

  /**
   * Generates a random message ID.
   *
   * @return
   *   A random unique message identifier.
   */
  public static String generateRandomId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Sets the unique message identifier.
   *
   * @param id
   *   The unique message identifier.
   * @return
   *   The called message builder.
   */
  public JsonMessageBuilder setId(String id) {
    structure.putString(DefaultJsonMessage.ID, id);
    return this;
  }

  /**
   * Sets the message body.
   *
   * @param body
   *   The message body.
   * @return
   *   The called message builder.
   */
  public JsonMessageBuilder setBody(JsonObject body) {
    structure.putObject(DefaultJsonMessage.BODY, body);
    return this;
  }

  /**
   * Sets the message tag.
   *
   * @param tag
   *   The message tag.
   * @return
   *   The called message builder.
   */
  public JsonMessageBuilder setTag(String tag) {
    structure.putString(DefaultJsonMessage.TAG, tag);
    return this;
  }

  /**
   * Sets the message source.
   *
   * @param source
   *   The message source.
   * @return
   *   The called message builder.
   */
  public JsonMessageBuilder setSource(String source) {
    structure.putString(DefaultJsonMessage.SOURCE, source);
    return this;
  }

  /**
   * Sets the message parent.
   *
   * @param parent
   *   The message parent.
   * @return
   *   The called message builder.
   */
  public JsonMessageBuilder setParent(String parent) {
    structure.putString(DefaultJsonMessage.PARENT, parent);
    return this;
  }

  /**
   * Sets the message root.
   *
   * @param root
   *   The message root.
   * @return
   *   The called message builder.
   */
  public JsonMessageBuilder setRoot(String root) {
    structure.putString(DefaultJsonMessage.ROOT, root);
    return this;
  }

  /**
   * Sets the message auditor.
   *
   * @param auditor
   *   The message auditor.
   * @return
   *   The called message builder.
   */
  public JsonMessageBuilder setAuditor(String auditor) {
    structure.putString(DefaultJsonMessage.AUDITOR, auditor);
    return this;
  }

  /**
   * Returns a new JSON message from the built message structure.
   *
   * @return
   *   A new {@link JsonMessage} instance.
   */
  public JsonMessage toMessage() {
    JsonMessage message = new DefaultJsonMessage();
    try {
      message.setState(structure);
    }
    catch (SerializationException e) {
      return null;
    }
    return message;
  }

}
