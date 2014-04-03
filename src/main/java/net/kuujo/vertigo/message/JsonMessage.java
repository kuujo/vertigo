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

import net.kuujo.vertigo.message.impl.ReliableJsonMessage;
import net.kuujo.vertigo.util.serializer.Serializable;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A Vertigo message.
 * 
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="type",
  defaultImpl=ReliableJsonMessage.class
)
public interface JsonMessage extends Serializable {

  /**
   * Returns the unique message ID.
   * 
   * @return The unique message ID.
   */
  String id();

  /**
   * Returns the message body.
   * 
   * @return The message body.
   */
  JsonObject body();

  /**
   * Returns the parent message ID.
   *
   * @return The parent message ID.
   */
  String parent();

  /**
   * Returns the root message ID in the message tree.
   *
   * @return The root message ID in the message tree.
   */
  String root();

  /**
   * Copies the message.
   *
   * @return A new copy of the message.
   */
  JsonMessage copy(String id);

}
