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
package net.kuujo.vitis.messaging;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A default JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultJsonMessage implements JsonMessage {

  private JsonObject body;

  public DefaultJsonMessage(JsonObject body) {
    this.body = body;
  }

  public static JsonMessage create(JsonObject body) {
    return new DefaultJsonMessage(new JsonObject().putObject("body", body));
  }

  public static JsonMessage create(JsonObject body, JsonArray tags) {
    return new DefaultJsonMessage(new JsonObject().putObject("body", body).putArray("tags", tags));
  }

  public static JsonMessage create(String id, JsonObject body) {
    return new DefaultJsonMessage(new JsonObject().putString("id", id).putObject("body", body));
  }

  public static JsonMessage create(String id, JsonObject body, JsonArray tags) {
    return new DefaultJsonMessage(new JsonObject().putString("id", id).putObject("body", body).putArray("tags", tags));
  }

  @Override
  public String tree() {
    return body.getString("id");
  }

  @Override
  public String source() {
    return body.getString("source");
  }

  @Override
  public JsonObject body() {
    return body.getObject("body");
  }

  @Override
  public JsonArray tags() {
    JsonArray tags = body.getArray("tags");
    if (tags == null) {
      tags = new JsonArray();
      body.putArray("tags", tags);
    }
    return tags;
  }

  @Override
  public JsonMessage createChild(JsonObject body) {
    return DefaultJsonMessage.create(this.body.copy());
  }

  @Override
  public JsonMessage createChild(JsonObject body, JsonArray tags) {
    return DefaultJsonMessage.create(this.body.copy(), tags);
  }

  @Override
  public JsonMessage copy() {
    return new DefaultJsonMessage(body.copy());
  }

  @Override
  public JsonObject serialize() {
    return body;
  }

}
