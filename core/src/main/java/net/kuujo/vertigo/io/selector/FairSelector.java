/*
 * Copyright 2014 the original author or authors.
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

import java.util.Arrays;
import java.util.List;

import net.kuujo.vertigo.io.connection.Connection;

/**
 * Selector that sends messages to the connection with the smallest queue.<p>
 *
 * The fair selector selects connections based on their current output queue
 * sizes. This means the if one connection's queue is backed up, the fair
 * selector will evenly dispatch messages to connections with shorter queues.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class FairSelector implements Selector {

  @Override
  @SuppressWarnings("rawtypes")
  public <T extends Connection> List<T> select(Object message, List<T> connections) {
    T lowest = null;
    for (T connection : connections) {
      if (lowest == null || connection.size() < lowest.size()) {
        lowest = connection;
      }
    }
    return Arrays.asList(lowest);
  }

}
