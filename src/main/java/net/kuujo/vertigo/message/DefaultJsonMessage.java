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

import org.vertx.java.core.json.JsonObject;

/**
 * A default JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultJsonMessage implements JsonMessage {
  private JsonObject body;
  public static final String ID = "id";
  public static final String SOURCE = "source";
  public static final String PARENT = "parent";
  public static final String ROOT = "root";
  public static final String AUDITOR = "auditor";
  public static final String BODY = "body";
  public static final String TAG = "tag";

  private static String createUniqueId() {
    return UUID.randomUUID().toString();
  }

  public DefaultJsonMessage() {
  }

  private DefaultJsonMessage(JsonObject body) {
    this.body = body;
  }

  @Override
  public String id() {
    return body.getString(ID);
  }

  @Override
  public String source() {
    return body.getString(SOURCE);
  }

  @Override
  public String parent() {
    return body.getString(PARENT);
  }

  @Override
  public String root() {
    return body.getString(ROOT);
  }

  @Override
  public String auditor() {
    return body.getString(AUDITOR);
  }

  @Override
  public JsonObject body() {
    return body.getObject(BODY);
  }

  @Override
  public String tag() {
    return body.getString(TAG);
  }

  @Override
  public JsonMessage createChild() {
    JsonObject newMessage = this.body.copy();
    if (!newMessage.getFieldNames().contains(ROOT)) {
      newMessage.putString(ROOT, this.body.getString(ID));
    }
    newMessage.putString(PARENT, this.body.getString(ID));
    newMessage.putString(ID, createUniqueId());
    return new DefaultJsonMessage(newMessage);
  }

  @Override
  public JsonMessage createChild(JsonObject body) {
    JsonObject newMessage = this.body.copy();
    if (!newMessage.getFieldNames().contains(ROOT)) {
      newMessage.putString(ROOT, this.body.getString(ID));
    }
    newMessage.putString(PARENT, this.body.getString(ID));
    newMessage.putString(ID, createUniqueId())
      .putObject(BODY, body);
    return new DefaultJsonMessage(newMessage);
  }

  @Override
  public JsonMessage createChild(JsonObject body, String tag) {
    JsonObject newMessage = this.body.copy();
    if (!newMessage.getFieldNames().contains(ROOT)) {
      newMessage.putString(ROOT, this.body.getString(ID));
    }
    newMessage.putString(PARENT, this.body.getString(ID));
    newMessage.putString(ID, createUniqueId())
      .putObject(BODY, body)
      .putString(TAG, tag);
    return new DefaultJsonMessage(newMessage);
  }

  @Override
  public JsonMessage copy() {
    return new DefaultJsonMessage(body.copy().putString(ID, createUniqueId()));
  }

  @Override
  public void setState(JsonObject state) {
    body = state;
  }

  @Override
  public JsonObject getState() {
    return body;
  }

}
