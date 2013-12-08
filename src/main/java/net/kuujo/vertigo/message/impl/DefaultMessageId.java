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

import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.json.JsonObject;

/**
 * A default message identifier implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultMessageId implements MessageId {
  private JsonObject data;
  public static final String CODE = "code";
  public static final String ID = "id";
  public static final String PARENT = "parent";
  public static final String ROOT = "root";
  public static final String OWNER = "owner";
  public static final String AUDITOR = "auditor";

  DefaultMessageId(JsonObject messageId) {
    this.data = messageId;
  }

  /**
   * Creates a message ID from JSON.
   *
   * @param messageId
   *   A JSON representation of the message ID.
   * @return
   *   A new MessageId instance.
   */
  public static MessageId fromJson(JsonObject messageId) {
    return new DefaultMessageId(messageId);
  }

  @Override
  public String owner() {
    return data.getString(OWNER);
  }

  @Override
  public long ackCode() {
    return data.getLong(CODE);
  }

  @Override
  public String correlationId() {
    return data.getString(ID);
  }

  @Override
  public boolean hasParent() {
    return data.getFieldNames().contains(PARENT);
  }

  @Override
  public String parent() {
    return data.getString(PARENT);
  }

  @Override
  public boolean hasRoot() {
    return data.getFieldNames().contains(ROOT);
  }

  @Override
  public boolean isRoot() {
    return !hasRoot();
  }

  @Override
  public String root() {
    return data.getString(ROOT);
  }

  @Override
  public String auditor() {
    return data.getString(AUDITOR);
  }

  @Override
  public JsonObject toJson() {
    return data;
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof MessageId && ((MessageId) object).correlationId().equals(correlationId());
  }

}
