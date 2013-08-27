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
package com.blankstyle.vine.heartbeat;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * A hearbeat emitter.
 *
 * @author Jordan Halterman
 */
public interface HeartBeatEmitter {

  /**
   * Sets the heartbeat address.
   *
   * @param address
   *   The heartbeat address.
   * @return
   *   The called emitter instance.
   */
  public HeartBeatEmitter setAddress(String address);

  /**
   * Gets the heartbeat address.
   *
   * @return
   *   The heartbeat address.
   */
  public String getAddress();

  /**
   * Sets the emitter vertx instance.
   *
   * @param vertx
   *   A vertx instance.
   * @return
   *   The called emitter instance.
   */
  public HeartBeatEmitter setVertx(Vertx vertx);

  /**
   * Returns the monitor vertx instance.
   *
   * @return
   *   The monitor vertx instance.
   */
  public Vertx getVertx();

  /**
   * Sets the heartbeat emitter eventbus.
   *
   * @param eventBus
   *   The heartbeat emitter eventbus.
   * @return
   *   The called hearbeat emitter instance.
   */
  public HeartBeatEmitter setEventBus(EventBus eventBus);

  /**
   * Gets the heartbeat emitter eventbus.
   *
   * @return
   *   The heartbeat emitter eventbus.
   */
  public EventBus getEventBus();

  /**
   * Sets the required heartbeat interval.
   *
   * @param interval
   *   The required heartbeat interval.
   * @return
   *   The called heartbeat emitter instance.
   */
  public HeartBeatEmitter setInterval(long interval);

  /**
   * Gets the required heartbeat interval.
   *
   * @return
   *   The required heartbeat interval.
   */
  public long getInterval();

  /**
   * Starts emitting heartbeats to the heartbeat address.
   */
  public void start();

  /**
   * Stops emitting heartbeats to the heartbeat address.
   */
  public void stop();

}
