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
package net.kuujo.vertigo.output.selector;

import java.util.Arrays;
import java.util.List;

import net.kuujo.vertigo.output.OutputConnection;

/**
 * a *fair* selector.
 *
 * The fair selector selects connections based on their current output queue
 * sizes. This means the if one connection's queue is backed up, the fair
 * selector will evenly dispatch messages to connections with shorter queues.
 *
 * @author Jordan Halterman
 */
public class FairSelector implements Selector {

  @Override
  public List<OutputConnection> select(Object message, List<OutputConnection> connections) {
    OutputConnection lowest = null;
    for (OutputConnection connection : connections) {
      if (lowest == null || connection.getSendQueueSize() < lowest.getSendQueueSize()) {
        lowest = connection;
      }
    }
    return Arrays.asList(lowest);
  }

}
