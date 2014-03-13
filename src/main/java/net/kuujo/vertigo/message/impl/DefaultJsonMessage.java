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

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.json.JsonObject;

/**
 * A default JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultJsonMessage implements JsonMessage {
  DefaultMessageId id;
  Map<String, Object> body;
  String stream;
  String source;

  DefaultJsonMessage() {
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
  public JsonObject toJson() {
    return new JsonObject()
        .putObject("id", id.toJson())
        .putObject("body", body())
        .putString("stream", stream)
        .putString("source", source);
  }

}
