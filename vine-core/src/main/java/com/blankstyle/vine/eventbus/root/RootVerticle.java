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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import com.blankstyle.vine.Stem;
import com.blankstyle.vine.context.RootContext;
import com.blankstyle.vine.context.StemContext;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;
import com.blankstyle.vine.heartbeat.DefaultHeartBeatMonitor;
import com.blankstyle.vine.heartbeat.HeartBeatMonitor;
import com.blankstyle.vine.remote.RemoteStem;
import com.blankstyle.vine.scheduler.Scheduler;

/**
 * A Vine root verticle.
 *
 * @author Jordan Halterman
 */
public class RootVerticle extends BusModBase implements Handler<Message<JsonObject>> {

  /**
   * The root address.
   */
  private String address;

  private static final String DEFAULT_ADDRESS = "vine.root";

  /**
   * A root context.
   */
  private RootContext context;

  /**
   * A Vert.x logger.
   */
  private Logger log;

  /**
   * A worker scheduler.
   */
  private Scheduler scheduler;

  /**
   * A map of stem addresses to heartbeat address.
   */
  private Map<String, String> heartbeatMap = new HashMap<String, String>();

  /**
   * A private counter for creating unique heartbeat addresses.
   */
  private int heartbeatCounter;

  /**
   * A heartbeat monitor for tracking whether stems are alive.
   */
  private HeartBeatMonitor heartbeatMonitor = new DefaultHeartBeatMonitor();

  /**
   * A map of stem addresses to stem contexts.
   */
  private Map<String, StemContext> stems = new HashMap<String, StemContext>();

  /**
   * A reliable eventbus.
   */
  private ReliableEventBus eventBus;

  @Override
  public void setVertx(Vertx vertx) {
    super.setVertx(vertx);
    EventBus eventBus = vertx.eventBus();
    if (eventBus instanceof ReliableEventBus) {
      this.eventBus = (ReliableEventBus) eventBus;
    }
    else {
      this.eventBus = new WrappedReliableEventBus(eventBus, vertx);
    }
    heartbeatMonitor.setVertx(vertx).setEventBus(vertx.eventBus());
  }

  @Override
  public void start() {
    config = container.config();
    log = container.logger();
    context = new RootContext(config);
    address = getOptionalStringConfig("address", DEFAULT_ADDRESS);
    log.info(String.format("Starting stem at %s.", address));
    String schedulerClass = getOptionalStringConfig("scheduler", "com.blankstyle.vine.scheduler.DefaultScheduler");
    try {
      scheduler = (Scheduler) Class.forName(schedulerClass).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      container.logger().error(String.format("Invalid scheduler class %s.", schedulerClass));
    }
    vertx.eventBus().registerHandler(address, this);
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
   * Creates a list of stems.
   */
  private List<Stem> createStemList() {
    List<Stem> stems = new ArrayList<Stem>();
    Set<String> keys = this.stems.keySet();
    Iterator<String> addresses = keys.iterator();
    while (addresses.hasNext()) {
      stems.add(new RemoteStem(addresses.next(), vertx, container));
    }
    return stems;
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
