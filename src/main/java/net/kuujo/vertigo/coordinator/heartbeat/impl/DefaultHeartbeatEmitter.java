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

import net.kuujo.vertigo.coordinator.heartbeat.HeartbeatEmitter;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * Default heartbeat emitter implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultHeartbeatEmitter implements HeartbeatEmitter {
  private String address;
  private Vertx vertx;
  private EventBus eventBus;
  private long interval = 1000;
  private long timerID;

  public DefaultHeartbeatEmitter(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultHeartbeatEmitter(String address, Vertx vertx) {
    this.address = address;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  @Override
  public HeartbeatEmitter setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public HeartbeatEmitter setInterval(long interval) {
    this.interval = interval;
    return this;
  }

  @Override
  public long getInterval() {
    return interval;
  }

  @Override
  public void start() {
    timerID = vertx.setPeriodic(interval, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        eventBus.send(address, true);
      }
    });
  }

  @Override
  public void stop() {
    if (timerID != 0) {
      vertx.cancelTimer(timerID);
    }
  }

}