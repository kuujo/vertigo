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
package net.kuujo.vertigo.coordinator.heartbeat;

/**
 * A heartbeat emitter.
 *
 * @author Jordan Halterman
 */
public interface HeartbeatEmitter {

  /**
   * Sets the heartbeat emitter address.
   *
   * @param address
   *   The address to which to emit heartbeats.
   * @return
   *   The heartbeat emitter instance.
   */
  HeartbeatEmitter setAddress(String address);

  /**
   * Gets the heartbeat address.
   *
   * @return
   *   The heartbeat address.
   */
  String getAddress();

  /**
   * Sets the heartbeat interval.
   *
   * @param interval
   *   The heartbeat interval. This is the interval at which heartbeats will
   *   be emitted.
   * @return
   *   The heartbeat emitter instance.
   */
  HeartbeatEmitter setInterval(long interval);

  /**
   * Gets the heartbeat interval.
   *
   * @return
   *   The heartbeat interval.
   */
  long getInterval();

  /**
   * Starts emitting heartbeats at an address.
   */
  void start();

  /**
   * Stops emitting heartbeats to an address.
   */
  void stop();

}