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

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.json.JsonObject;

/**
 * A default JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultJsonMessage implements JsonMessage {
  private JsonObject body;
  public static final String ID = "id";
  public static final String BODY = "body";
  public static final String STREAM = "stream";
  public static final String SOURCE = "source";

  public DefaultJsonMessage() {
  }

  DefaultJsonMessage(JsonObject body) {
    this.body = body;
  }

  /**
   * Returns a Json message from JSON.
   *
   * @param json
   *   A JSON representation of the message.
   * @return
   *   A new Json message object.
   */
  public static JsonMessage fromJson(JsonObject json) {
    return new DefaultJsonMessage(json);
  }

  @Override
  public MessageId messageId() {
    return DefaultMessageId.fromJson(body.getObject(ID));
  }

  @Override
  public JsonObject body() {
    return body.getObject(BODY);
  }

  @Override
  public String stream() {
    return body.getString(STREAM);
  }

  @Override
  public String source() {
    return body.getString(SOURCE);
  }

  @Override
  public JsonObject toJson() {
    return body;
  }

}
