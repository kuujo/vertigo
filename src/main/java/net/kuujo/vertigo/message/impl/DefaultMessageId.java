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
  int code;
  String id;
  String parent;
  String root;
  String owner;
  String auditor;

  DefaultMessageId() {
  }

  @Override
  public String owner() {
    return owner;
  }

  @Override
  public long ackCode() {
    return code;
  }

  @Override
  public String correlationId() {
    return id;
  }

  @Override
  public boolean hasParent() {
    return parent != null;
  }

  @Override
  public String parent() {
    return parent;
  }

  @Override
  public boolean hasRoot() {
    return root != null;
  }

  @Override
  public boolean isRoot() {
    return root == null;
  }

  @Override
  public String root() {
    return root != null ? root : id;
  }

  @Override
  public String auditor() {
    return auditor;
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .putString("id", id)
        .putNumber("code", code)
        .putString("owner", owner)
        .putString("parent", parent)
        .putString("root", root)
        .putString("auditor", auditor);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof MessageId && ((MessageId) object).correlationId().equals(correlationId());
  }

}
