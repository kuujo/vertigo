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
package com.blankstyle.vine.impl;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Message;
import com.blankstyle.vine.Serializeable;

/**
 * A JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class JsonMessage<T> implements Message<T>, Serializeable<JsonObject> {

  protected String id;

  protected T body;

  /**
   * Creates a JSON message from a JSON object.
   *
   * @param json
   *   The JSON object from which to create the message.
   * @return
   *   A JSON message instance.
   */
  public static <T> JsonMessage<T> fromJsonObject(JsonObject json) {
    JsonMessage<T> message = new JsonMessage<T>();
    message.setIdentifier(json.getString("id"));
    message.setBody(json.<T>getValue("body"));
    return message;
  }

  @Override
  public Message<T> setIdentifier(String id) {
    this.id = id;
    return this;
  }

  @Override
  public String getIdentifier() {
    return id;
  }

  @Override
  public Message<T> setBody(T body) {
    this.body = body;
    return this;
  }

  @Override
  public T getBody() {
    return body;
  }

  @Override
  public JsonObject toJsonObject() {
    JsonObject message = new JsonObject();
    message.putString("id", id);
    message.putValue("body", getBody());
    return message;
  }

}
