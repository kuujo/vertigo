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

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * A default heartbeat emitter.
 *
 * @author Jordan Halterman
 */
public class DefaultHeartBeatEmitter implements HeartBeatEmitter {

  private String address;

  private Vertx vertx;

  private EventBus eventBus;

  private long interval;

  private long timerID;

  public DefaultHeartBeatEmitter() {
  }

  public DefaultHeartBeatEmitter(String address) {
    this.address = address;
  }

  public DefaultHeartBeatEmitter(String address, Vertx vertx) {
    this.address = address;
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultHeartBeatEmitter(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public DefaultHeartBeatEmitter(Vertx vertx, EventBus eventBus) {
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  public DefaultHeartBeatEmitter(String address, Vertx vertx, EventBus eventBus) {
    this.address = address;
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  @Override
  public HeartBeatEmitter setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public HeartBeatEmitter setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public HeartBeatEmitter setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    return this;
  }

  @Override
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public HeartBeatEmitter setInterval(long interval) {
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
