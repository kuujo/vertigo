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
package net.kuujo.vine.heartbeat;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * A worker heartbeat monitor.
 *
 * @author Jordan Halterman
 */
public interface HeartBeatMonitor {

  /**
   * Sets the monitor vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   * @return
   *   The called monitor instance.
   */
  public HeartBeatMonitor setVertx(Vertx vertx);

  /**
   * Returns the monitor vertx instance.
   *
   * @return
   *   The monitor vertx instance.
   */
  public Vertx getVertx();

  /**
   * Sets the heartbeat monitor eventbus.
   *
   * @param eventBus
   *   The heartbeat monitor eventbus.
   * @return
   *   The called hearbeat monitor instance.
   */
  public HeartBeatMonitor setEventBus(EventBus eventBus);

  /**
   * Gets the heartbeat monitor eventbus.
   *
   * @return
   *   The heartbeat monitor eventbus.
   */
  public EventBus getEventBus();

  /**
   * Sets the required heartbeat interval.
   *
   * @param interval
   *   The required heartbeat interval.
   * @return
   *   The called heartbeat monitor instance.
   */
  public HeartBeatMonitor setInterval(long interval);

  /**
   * Gets the required heartbeat interval.
   *
   * @return
   *   The required heartbeat interval.
   */
  public long getInterval();

  /**
   * Registers a monitor at an address.
   *
   * @param address
   *   The address at which to monitor for heartbeats.
   * @param failHandler
   *   A handler to be invoked if the heartbeat fails.
   */
  public void monitor(String address, Handler<String> failHandler);

  /**
   * Unregisters a monitor from an address.
   *
   * @param address
   *   The address at which to stop monitoring for heartbeats.
   */
  public void unmonitor(String address);

}
