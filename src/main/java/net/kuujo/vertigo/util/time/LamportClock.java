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

/**
 * Simple Lamport clock.
 *
 * @author Jordan Halterman
 */
public class LamportClock {
  private long value = 1;

  /**
   * Returns the current clock value.
   *
   * @return The current clock value.
   */
  public long value() {
    return value;
  }

  /**
   * Increments the current clock value.
   */
  public void increment() {
    value++;
  }

  /**
   * Updates the current clock value.
   *
   * @param value The clock with which to update the value.
   * @return The updated clock.
   */
  public LamportClock update(LamportClock value) {
    this.value = Math.max(this.value, value.value) + 1;
    return this;
  }

}
