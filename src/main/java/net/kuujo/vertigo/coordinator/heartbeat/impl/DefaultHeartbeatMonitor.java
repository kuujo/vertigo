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
package net.kuujo.vertigo.coordinator.heartbeat.impl;

import net.kuujo.vertigo.coordinator.heartbeat.HeartbeatMonitor;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

/**
 * Default heartbeat monitor implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultHeartbeatMonitor implements HeartbeatMonitor {
  private String address;
  private Vertx vertx;
  private EventBus eventBus;
  private long interval = 5000;
  private AddressMonitor currentMonitor;

  public DefaultHeartbeatMonitor(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultHeartbeatMonitor(String address, Vertx vertx) {
    this.address = address;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  @Override
  public HeartbeatMonitor setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public HeartbeatMonitor setRequiredInterval(long interval) {
    this.interval = interval;
    return this;
  }

  @Override
  public long getRequiredInterval() {
    return interval;
  }

  @Override
  public void listen(Handler<String> timeoutHandler) {
    if (currentMonitor != null) {
      currentMonitor.stop();
    }
    currentMonitor = new AddressMonitor(address, timeoutHandler);
    currentMonitor.start();
  }

  @Override
  public void unlisten() {
    if (currentMonitor != null) {
      currentMonitor.stop();
      currentMonitor = null;
    }
  }

  /**
  * A monitor on a single address.
  */
  private class AddressMonitor {
    private String address;
    private Handler<String> timeoutHandler;
    private long timerID;

    private Handler<Message<Boolean>> handler = new Handler<Message<Boolean>>() {
      @Override
      public void handle(Message<Boolean> message) {
        resetTimer();
      }
    };

    public AddressMonitor(String address, Handler<String> timeoutHandler) {
      this.address = address;
      this.timeoutHandler = timeoutHandler;
    }

    /**
    * Starts the monitor.
    */
    private void start() {
      eventBus.registerHandler(address, handler);
    }

    /**
    * Stops the monitor.
    */
    private void stop() {
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
      // Then, create a new timer that triggers the timeoutHandler if called.
      timerID = vertx.setTimer(interval, new Handler<Long>() {
        @Override
        public void handle(Long event) {
          eventBus.unregisterHandler(address, handler);
          timeoutHandler.handle(address);
        }
      });
    }
  }

}
