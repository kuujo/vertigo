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
import net.kuujo.vertigo.util.serializer.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A message identifier.
 * 
 * @author Jordan Halterman
 */
@JsonTypeInfo(
    use=JsonTypeInfo.Id.CLASS,
    include=JsonTypeInfo.As.PROPERTY,
    property="type",
    defaultImpl=DefaultJsonMessage.class
)
public interface MessageId extends Serializable {

  /**
   * Gets the message correlation ID.
   * 
   * @return A globally unique correlation identifier.
   */
  String correlationId();

  /**
   * Gets the root message ID.
   * 
   * @return The root message correlationID.
   */
  String tree();

  /**
   * Gets the random message code.
   * 
   * @return A random message code.
   */
  int ackCode();

  /**
   * Gets the message auditor address.
   * 
   * The auditor address is the event bus address to the auditor that tracks messages
   * within this message's tree. Thus, messages always inherit the auditor address of
   * their parent so that all messages within a tree are always tracked by the same
   * auditor. Once the auditor considers a message tree fully processed, it will notify
   * the message source via the acker address.
   * 
   * @return The message auditor.
   */
  String auditor();

  /**
   * Copies the message ID.
   *
   * @return A copy of the message ID.
   */
  MessageId copy();

}
