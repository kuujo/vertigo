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

import net.kuujo.vertigo.message.impl.DefaultJsonMessage;
import net.kuujo.vertigo.serializer.Serializable;

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
    defaultImpl=DefaultJsonMessage.class
)
public interface JsonMessage extends Serializable {

  /**
   * Returns the message ID.
   *
   * @return
   *   The message ID.
   */
  MessageId messageId();

  /**
   * Returns the message body.
   *
   * @return
   *   The message body.
   */
  JsonObject body();

  /**
   * Returns the message stream.
   *
   * @return
   *   The stream on which the message was emitted from a component.
   */
  String stream();

  /**
   * Returns the message source. This is the event bus address of the
   * component from which this message tree originated.
   *
   * @return
   *   The source address.
   */
  String source();

  /**
   * Returns a JSON representation of the message.
   *
   * @return
   *   A JSON representation of the message.
   */
  JsonObject toJson();

}
