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
package com.blankstyle.vine.test.integration.heartbeat;

import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import com.blankstyle.vine.heartbeat.DefaultHeartBeatEmitter;
import com.blankstyle.vine.heartbeat.DefaultHeartBeatMonitor;
import com.blankstyle.vine.heartbeat.HeartBeatEmitter;
import com.blankstyle.vine.heartbeat.HeartBeatMonitor;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A heartbeat monitor/emitter test.
 *
 * @author Jordan Halterman
 */
public class HeartbeatTest extends TestVerticle {

  private boolean isDead;

  @Before
  public void setUp() {
    isDead = false;
  }

  @Test
  public void testHeartbeat() {
    // Monitor heartbeats at at least two second intervals.
    HeartBeatMonitor monitor = new DefaultHeartBeatMonitor(vertx, vertx.eventBus()).setInterval(2000);

    // Emit heartbeats at one second intervals.
    final HeartBeatEmitter emitter = new DefaultHeartBeatEmitter("test.a", vertx, vertx.eventBus()).setInterval(1000);
    emitter.start();

    monitor.monitor("test.a", new Handler<String>() {
      @Override
      public void handle(String event) {
        if (!isDead) {
          assertTrue(false);
        }
        else {
          assertTrue(true);
        }
        testComplete();
      }
    });

    // Allow the heartbeat to die in five seconds.
    vertx.setTimer(5000, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        emitter.stop();
        isDead = true;
      }
    });
  }

}
