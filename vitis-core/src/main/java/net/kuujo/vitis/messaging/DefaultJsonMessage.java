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

import java.util.UUID;

import org.vertx.java.core.json.JsonObject;

/**
 * A default JSON message implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultJsonMessage implements JsonMessage {

  private JsonObject body;

  private static String createUniqueId() {
    return UUID.randomUUID().toString();
  }

  public DefaultJsonMessage(JsonObject body) {
    this.body = body;
  }

  public static JsonMessage create(JsonObject body) {
    return new DefaultJsonMessage(new JsonObject().putString("id", createUniqueId()).putObject("body", body));
  }

  public static JsonMessage create(JsonObject body, String tag) {
    return new DefaultJsonMessage(new JsonObject().putString("id", createUniqueId()).putObject("body", body).putString("tag", tag));
  }

  public static JsonMessage create(String id, JsonObject body) {
    return new DefaultJsonMessage(new JsonObject().putString("id", id).putObject("body", body));
  }

  public static JsonMessage create(String id, JsonObject body, String tag) {
    return new DefaultJsonMessage(new JsonObject().putString("id", id).putObject("body", body).putString("tag", tag));
  }

  @Override
  public String id() {
    return body.getString("id");
  }

  @Override
  public String source() {
    return body.getString("source");
  }

  @Override
  public String parent() {
    return body.getString("parent");
  }

  @Override
  public JsonObject body() {
    return body.getObject("body");
  }

  @Override
  public String tag() {
    return body.getString("tag");
  }

  @Override
  public JsonMessage createChild(JsonObject body) {
    return new DefaultJsonMessage(
        this.body.copy()
        .putString("parent", body.getString("id"))
        .putString("id", createUniqueId())
        .putObject("body", body)
    );
  }

  @Override
  public JsonMessage createChild(JsonObject body, String tag) {
    return new DefaultJsonMessage(
        this.body.copy()
        .putString("parent", body.getString("id"))
        .putString("id", createUniqueId())
        .putObject("body", body)
        .putString("tag", tag)
    );
  }

  @Override
  public JsonMessage createChild(String id, JsonObject body) {
    return new DefaultJsonMessage(
        this.body.copy()
        .putString("parent", body.getString("id"))
        .putString("id", id)
        .putObject("body", body)
    );
  }

  @Override
  public JsonMessage createChild(String id, JsonObject body, String tag) {
    return new DefaultJsonMessage(
        this.body.copy()
        .putString("parent", body.getString("id"))
        .putString("id", id)
        .putObject("body", body)
        .putString("tag", tag)
    );
  }

  @Override
  public JsonMessage copy() {
    return new DefaultJsonMessage(body.copy().putString("id", createUniqueId()));
  }

  @Override
  public JsonObject serialize() {
    return body;
  }

}
