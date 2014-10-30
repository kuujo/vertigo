/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.log;

/**
 * Component event message entry.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface MessageEntry<T> extends Entry {

  /**
   * Returns the connection on which the message was received.
   *
   * @return The connection on which the message was received.
   */
  String connection();

  /**
   * Returns the port on which the message was received.
   *
   * @return The port on which the message was received.
   */
  String port();

  /**
   * Returns the entry message.
   *
   * @return The entry message.
   */
  T message();

}
