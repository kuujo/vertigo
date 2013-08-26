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
package com.blankstyle.vine.messaging;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class JsonMessage<T> implements Message<T> {

  /**
   * Creates a JSON message from a JSON object.
   *
   * @param json
   *   The JSON object from which to create the message.
   * @return
   *   A JSON message instance.
   */
  public static <T> JsonMessage<T> fromJsonObject(JsonObject json) {
    return new JsonMessage<T>(json);
  }

  protected JsonObject json;

  public JsonMessage() {
    this.json = new JsonObject();
  }

  public JsonMessage(JsonObject json) {
    this.json = json;
  }

  @Override
  public Message<T> setIdentifier(String id) {
    json.putString("id", id);
    return this;
  }

  @Override
  public String getIdentifier() {
    return json.getString("id");
  }

  @Override
  public Message<T> addTag(String tag) {
    JsonArray tags = json.getArray("tags");
    if (tags == null) {
      tags = new JsonArray();
      json.putArray("tags", tags);
    }
    tags.add(tag);
    return this;
  }

  @Override
  public String[] getTags() {
    JsonArray tags = json.getArray("tags");
    if (tags != null) {
      return (String[]) tags.toArray();
    }
    return null;
  }

  @Override
  public Message<T> setBody(T body) {
    json.putValue("body", body);
    return this;
  }

  @Override
  public T getBody() {
    return json.getValue("body");
  }

  @Override
  public JsonObject serialize() {
    return json;
  }

}
