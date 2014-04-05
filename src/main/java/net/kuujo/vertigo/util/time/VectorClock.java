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
package net.kuujo.vertigo.util.time;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple Vector clock.
 *
 * @author Jordan Halterman
 */
public class VectorClock {
  private final Map<String, Long> value = new HashMap<>();

  /**
   * Creates a globally unique timestamp from a vector clock.
   *
   * @param clock The vector clock from which to construct the time.
   * @return A unique timestamp.
   */
  public static long createTimestamp(VectorClock clock) {
    long time = 0;
    for (long value : clock.value.values()) {
      time += value;
    }
    return time;
  }

  /**
   * Returns a map of node names to clock values.
   *
   * @return A map of nodes and values.
   */
  public Map<String, Long> value() {
    return value;
  }

  /**
   * Returns the current value for a node.
   *
   * @param node The node name.
   * @return The node value.
   */
  public long value(String node) {
    return value.get(node);
  }

  /**
   * Increments the value of a node in the clock.
   *
   * @param node The name of the node to increment.
   */
  public void increment(String node) {
    this.value.put(node, ((long) this.value.get(node))+1);
  }

  /**
   * Updates the clock with another clock.
   *
   * @param value The clock with which to merge.
   * @return The updated vector clock.
   */
  public VectorClock update(VectorClock value) {
    for (Map.Entry<String, Long> entry : value.value.entrySet()) {
      this.value.put(entry.getKey(), Math.max(this.value.containsKey(entry.getKey()) ? this.value.get(entry.getKey()) : 0, entry.getValue()) + 1);
    }
    return this;
  }

}
