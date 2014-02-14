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

import org.vertx.java.core.Handler;

/**
 * A heartbeat monitor.
 *
 * @author Jordan Halterman
 */
public interface HeartbeatMonitor {

  /**
   * Sets the heartbeat monitor address.
   *
   * @param address
   *   The address at which to monitor for heartbeats.
   * @return
   *   The called monitor instance.
   */
  HeartbeatMonitor setAddress(String address);

  /**
   * Gets the heartbeat monitor address.
   *
   * @return
   *   The heartbeat monitor address.
   */
  String getAddress();

  /**
   * Sets the required heartbeat interval.
   *
   * @param interval
   *   The required heartbeat interval. If this interval is reached between any
   *   heartbeat then the heartbeat will be considered failed.
   * @return
   *   The called monitor instance.
   */
  HeartbeatMonitor setRequiredInterval(long interval);

  /**
   * Returns the required heartbeat interval.
   *
   * @return
   *   The required heartbeat interval.
   */
  long getRequiredInterval();

  /**
   * Starts monitoring heartbeats at the given address.
   *
   * @param failHandler
   *   A handler to be invoked if heartbeats fail.
   */
  void listen(Handler<String> failHandler);

  /**
   * Stops monitoring heartbeats at the given address.
   */
  void unlisten();

}