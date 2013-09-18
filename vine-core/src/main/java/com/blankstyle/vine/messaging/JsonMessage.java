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

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Serializeable;

/**
 * A JSON-based eventbus message.
 *
 * @author Jordan Halterman
 */
public class JsonMessage implements Serializeable<JsonObject> {

  private Message<JsonObject> message;

  private JsonObject data;

  private boolean ready;

  private boolean acked;

  private boolean ack;

  private boolean responded;

  public JsonMessage(Message<JsonObject> message) {
    this.message = message;
    this.data = message.body();
  }

  public JsonMessage(JsonObject body) {
    this.data = body;
  }

  /**
   * Sets the unique message identifier.
   *
   * @param id
   *   A message identifier.
   * @return
   *   The called message instance.
   */
  public JsonMessage setIdentifier(long id) {
    data.putNumber("id", id);
    return this;
  }

  /**
   * Gets the unique message identifier.
   *
   * @return
   *   A unique message identifier.
   */
  public long getIdentifier() {
    return data.getInteger("id");
  }

  /**
   * Returns the message body.
   *
   * @return
   *   A JSON message body.
   */
  public JsonObject body() {
    JsonObject body = data.getObject("body");
    if (body == null) {
      body = new JsonObject();
      data.putObject("body", body);
    }
    return body;
  }

  /**
   * Sets the message body.
   *
   * @param body
   *   A JSON message body.
   * @return
   *   The called message instance.
   */
  public JsonMessage setBody(JsonObject body) {
    data.putObject("body", body);
    return this;
  }

  /**
   * Sets the message to ready state.
   */
  public void ready() {
    ready = true;
    checkAck();
  }

  /**
   * Acknowledges that the message has been processed.
   */
  public void ack() {
    acked = true; ack = true;
    checkAck();
  }

  /**
   * Indicates failure to process the message.
   */
  public void fail() {
    acked = true; ack = false;
    checkAck();
  }

  /**
   * Checks whether the message has completed processing and acks or fails
   * if necessary.
   */
  private void checkAck() {
    if (!isLocked()) {
      if (ready && acked && message != null) {
        message.reply(ack);
        lock();
      }
      else if (acked && !ack) {
        message.reply(false);
        lock();
      }
    }
  }

  /**
   * Locks the message, preventing multiple ack responses.
   */
  private void lock() {
    responded = true;
  }

  /**
   * Indicates whether the message has been responded to.
   */
  private boolean isLocked() {
    return responded;
  }

  /**
   * Creates a child message.
   *
   * @param data
   *   The data to apply to the child message.
   * @return
   *   A child message instance.
   */
  public JsonMessage createChild(JsonObject data) {
    return new JsonMessage(data).setIdentifier(getIdentifier());
  }

  /**
   * Copies the message.
   *
   * @return
   *   A new message instance.
   */
  public JsonMessage copy() {
    return new JsonMessage(data.copy());
  }

  @Override
  public JsonObject serialize() {
    return data;
  }

}
