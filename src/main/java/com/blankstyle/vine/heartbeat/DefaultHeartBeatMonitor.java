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

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

/**
 * A default heartbeat monitor implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultHeartBeatMonitor implements HeartBeatMonitor {

  private Vertx vertx;

  private EventBus eventBus;

  private long interval;

  private Map<String, AddressMonitor> monitors = new HashMap<String, AddressMonitor>();

  public DefaultHeartBeatMonitor() {
  }

  public DefaultHeartBeatMonitor(Vertx vertx) {
    this.vertx = vertx;
  }

  public DefaultHeartBeatMonitor(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public DefaultHeartBeatMonitor(Vertx vertx, EventBus eventBus) {
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  @Override
  public HeartBeatMonitor setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public HeartBeatMonitor setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    return this;
  }

  @Override
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public HeartBeatMonitor setInterval(long interval) {
    this.interval = interval;
    return this;
  }

  @Override
  public long getInterval() {
    return interval;
  }

  @Override
  public void monitor(String address, final Handler<String> failHandler) {
    AddressMonitor monitor = new AddressMonitor(address, failHandler);
    monitor.start();
    monitors.put(address, monitor);
  }

  @Override
  public void unmonitor(String address) {
    if (monitors.containsKey(address)) {
      AddressMonitor monitor = monitors.get(address);
      monitor.stop();
      monitors.remove(address);
    }
  }

  /**
   * A monitor on a single address.
   */
  private class AddressMonitor {

    private String address;

    private Handler<String> failHandler;

    private long timerID;

    private Handler<Message<Void>> handler = new Handler<Message<Void>>() {
      @Override
      public void handle(Message<Void> event) {
        resetTimer();
      }
    };

    public AddressMonitor(String address, Handler<String> failHandler) {
      this.address = address;
      this.failHandler = failHandler;
    }

    /**
     * Starts the monitor.
     */
    public void start() {
      eventBus.registerHandler(address, handler);
    }

    /**
     * Stops the monitor.
     */
    public void stop() {
      eventBus.unregisterHandler(address, handler);
      if (timerID != 0) {
        vertx.cancelTimer(timerID);
      }
    }

    /**
     * Resets the monitor timer.
     */
    private void resetTimer() {
      // First, cancel the old timer.
      if (timerID != 0) {
        vertx.cancelTimer(timerID);
      }
      // Then, create a new timer that triggers the failHandler if called.
      timerID = vertx.setTimer(interval, new Handler<Long>() {
        @Override
        public void handle(Long event) {
          eventBus.unregisterHandler(address, handler);
          failHandler.handle(address);
        }
      });
    }
  }

}
