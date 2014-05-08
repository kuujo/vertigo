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
package net.kuujo.vertigo.io.selector;

import java.util.List;

import net.kuujo.vertigo.io.connection.Connection;

/**
 * Selector that sends messages based on a simple mod-hash algorithm.<p>
 *
 * The hash selector is a consistent-hashing based grouping. Given
 * a value on which to hash, this grouping guarantees that workers will
 * always receive messages with the same values.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class HashSelector implements Selector {

  @Override
  @SuppressWarnings("rawtypes")
  public <T extends Connection> List<T> select(Object message, List<T> connections) {
    int index = Math.abs(message.hashCode() % connections.size());
    return connections.subList(index, index+1);
  }

}
