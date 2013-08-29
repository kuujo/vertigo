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
package com.blankstyle.vine.eventbus.root;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.context.RootContext;
import com.blankstyle.vine.context.StemContext;
import com.blankstyle.vine.heartbeat.DefaultHeartBeatMonitor;
import com.blankstyle.vine.heartbeat.HeartBeatMonitor;
import com.blankstyle.vine.scheduler.Scheduler;

/**
 * A Vine root verticle.
 *
 * @author Jordan Halterman
 */
public class RootVerticle extends BusModBase implements Handler<Message<JsonObject>> {

  private RootContext context;

  private Scheduler scheduler;

  private Map<String, String> heartbeatMap = new HashMap<String, String>();

  private int heartbeatCounter;

  private HeartBeatMonitor heartbeatMonitor = new DefaultHeartBeatMonitor();

  private Map<String, StemContext> stems = new HashMap<String, StemContext>();

  @Override
  public void setVertx(Vertx vertx) {
    super.setVertx(vertx);
    heartbeatMonitor.setVertx(vertx).setEventBus(vertx.eventBus());
  }

  @Override
  public void start() {
    context = new RootContext(config);
    String schedulerClass = getOptionalStringConfig("scheduler", "com.blankstyle.vine.scheduler.DefaultScheduler");
    try {
      scheduler = (Scheduler) Class.forName(schedulerClass).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      container.logger().error(String.format("Invalid scheduler class %s.", schedulerClass));
    }
    vertx.eventBus().registerHandler(getMandatoryStringConfig("address"), this);
  }

  @Override
  public void handle(final Message<JsonObject> message) {
    String action = message.body().getString("action");

    if (action == null) {
      sendError(message, "An action must be specified.");
    }

    switch (action) {
      case "register":
        doRegister(message);
        break;
      case "deploy":
        doDeploy(message);
        break;
      case "undeploy":
        doUndeploy(message);
        break;
      case "load":
        doLoadContext(message);
        break;
      case "update":
        doUpdateContext(message);
        break;
      case "delete":
        doDeleteContext(message);
        break;
      default:
        sendError(message, String.format("Invalid action %s.", action));
    }
  }

  /**
   * Registers a stem with the root.
   *
   * Replies with a string representing the stem heartbeat address.
   */
  private void doRegister(final Message<JsonObject> message) {
    final StemContext stemContext = new StemContext(message.body());
    final String address = getMandatoryString("address", message);
    String heartbeatAddress = nextHeartBeatAddress();
    heartbeatMap.put(address, heartbeatAddress);
    heartbeatMonitor.monitor(heartbeatAddress, new Handler<String>() {
      @Override
      public void handle(String hbAddress) {
        unregisterStem(stemContext);
      }
    });
    registerStem(stemContext);
    message.reply(heartbeatAddress);
  }

  /**
   * Deploys a vine definition.
   */
  private void doDeploy(final Message<JsonObject> message) {
    
  }

  /**
   * Undeploys a vine definition.
   */
  private void doUndeploy(final Message<JsonObject> message) {
    
  }

  /**
   * Loads a vine context.
   */
  private void doLoadContext(final Message<JsonObject> message) {
    
  }

  /**
   * Updates a vine context.
   */
  private void doUpdateContext(final Message<JsonObject> message) {
    
  }

  /**
   * Deletes a vine context.
   */
  private void doDeleteContext(final Message<JsonObject> message) {
    
  }

  /**
   * Registers a stem context.
   *
   * @param context
   *   The stem context.
   */
  private void registerStem(StemContext context) {
    stems.put(context.getAddress(), context);
  }

  /**
   * Unregisters a stem context.
   *
   * @param context
   *   The stem context.
   */
  private void unregisterStem(StemContext context) {
    if (stems.containsKey(context.getAddress())) {
      stems.remove(context.getAddress());
    }
  }

  /**
   * Returns the next heartbeat address.
   */
  private String nextHeartBeatAddress() {
    heartbeatCounter++;
    return String.format("%s.heartbeat.%s", context.getAddress(), heartbeatCounter);
  }

}
