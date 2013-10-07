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
package net.kuujo.vertigo.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Observes component message trees and manages ack/fail.
 *
 * @author Jordan Halterman
 */
public class Auditor extends BusModBase implements Handler<Message<JsonObject>> {

  private String address;

  private String broadcastAddress;

  private boolean enabled;

  private long expire = 30000;

  private Map<String, Component> components = new HashMap<>();

  private Map<Long, Timer> timers = new HashMap<>();

  @Override
  public void start() {
    super.start();
    address = getMandatoryStringConfig("address");
    broadcastAddress = getMandatoryStringConfig("broadcast");
    enabled = getOptionalBooleanConfig("enabled", true);
    expire = getOptionalLongConfig("expire", expire);
    eb.registerHandler(address, this);
  }

  @Override
  public void handle(Message<JsonObject> message) {
    String action = getMandatoryString("action", message);
    switch (action) {
      case "create":
        doCreate(message);
        break;
      case "fork":
        doFork(message);
        break;
      case "ack":
        doAck(message);
        break;
      case "fail":
        doFail(message);
        break;
      default:
        sendError(message, String.format("Invalid action %s.", action));
        break;
    }
  }

  private Handler<Component> ackHandler = new Handler<Component>() {
    @Override
    public void handle(Component component) {
      Timer timer = component.timer;
      if (timer != null) {
        timer.ids.remove(component.id);
      }
      clearRoot(component);
      eb.publish(broadcastAddress, new JsonObject().putString("action", "ack").putString("id", component.id));
    }
  };

  private Handler<Component> failHandler = new Handler<Component>() {
    @Override
    public void handle(Component component) {
      Timer timer = component.timer;
      if (timer != null) {
        timer.ids.add(component.id);
      }
      clearRoot(component);
      eb.publish(broadcastAddress, new JsonObject().putString("action", "fail").putString("id", component.id));
    }
  };

  /**
   * Creates a message.
   */
  private void doCreate(Message<JsonObject> message) {
    String id = getMandatoryString("id", message);
    if (enabled) {
       // We simply add ack and fail handlers to the root component. If a descendant
      // component is failed then it will bubble up to the root. Once all child components
      // are acked the parent will be acked which will also bubble up to the root.
      Timer timer = getCurrentTimer();
      timer.ids.add(id);
      Component component = new Component(id, timer);
      component.ackHandler(ackHandler);
      component.failHandler(failHandler);
      components.put(component.id, component);
    }
    // If acking is disabled then immediately ack the message back to its source.
    else {
      eb.publish(broadcastAddress, new JsonObject().putString("action", "ack").putString("id", id));
    }
  }

  /**
   * Expires a component by ID.
   */
  private void expire(String id) {
    // Simply clear the expired component. It should be up to the feeder to timeout
    // the component if it failed. This is simply an added mechanism to prevent memory leaks.
    if (components.containsKey(id)) {
      components.get(id).fail();
    }
  }

  /**
   * Clears an entire message tree from storage.
   */
  private void clearRoot(Component component) {
    clearComponent(component);
  }

  /**
   * Clears all children of a component from storage.
   */
  private void clearComponent(Component component) {
    if (component.children.size() > 0) {
      for (Component child : component.children) {
        clearComponent(child);
      }
      if (components.containsKey(component.id)) {
        components.remove(component.id);
      }
    }
    else if (components.containsKey(component.id)) {
      components.remove(component.id);
    }
  }

  /**
   * Creates a message fork.
   */
  private void doFork(Message<JsonObject> message) {
    String parentId = getMandatoryString("parent", message);
    if (components.containsKey(parentId)) {
      String id = getMandatoryString("id", message);
      Component component = new Component(id);
      components.get(parentId).addChild(component);
      components.put(id, component);
    }
  }

  /**
   * Acks a message.
   */
  private void doAck(Message<JsonObject> message) {
    String id = getMandatoryString("id", message);
    if (components.containsKey(id)) {
      components.get(id).ack();
    }
  }

  /**
   * Fails a message.
   */
  private void doFail(Message<JsonObject> message) {
    String id = getMandatoryString("id", message);
    if (components.containsKey(id)) {
      components.get(id).fail();
    }
  }

  /**
   * An expire timer.
   */
  private class Timer {
    private final long endTime;
    private final Set<String> ids = new HashSet<String>();

    private Timer(long endTime) {
      this.endTime = endTime;
    }

    private Timer start() {
      vertx.setTimer(expire, new Handler<Long>() {
        @Override
        public void handle(Long timerId) {
          timers.remove(endTime);
          for (String id : ids) {
            expire(id);
          }
        }
      });
      timers.put(endTime, this);
      return this;
    }

  }

  /**
   * Gets the current timer.
   *
   * Timers are grouped by rounding the expire time to the nearest .1 second.
   * Message timeouts should be lengthy, and this ensures that a timer
   * is not created for every single message emitted from a feeder.
   */
  private Timer getCurrentTimer() {
    long end = Math.round((System.currentTimeMillis() + expire) / 100) * 100;
    if (timers.containsKey(end)) {
      return timers.get(end);
    }
    else {
      return new Timer(end).start();
    }
  }

  /**
   * Represents a single component in a message tree.
   */
  private static final class Component {
    private final String id;
    private final Timer timer;
    private final Set<Component> children = new HashSet<>();
    private final Set<Component> complete = new HashSet<>();
    private Handler<Component> ackHandler;
    private Handler<Component> failHandler;
    private boolean ready = true;
    private boolean acked;
    private boolean ack;
    private boolean locked;

    private Handler<Component> childAckHandler = new Handler<Component>() {
      @Override
      public void handle(Component component) {
        if (!failed()) {
          complete.add(component);
          if (complete.size() == children.size()) {
            ready();
          }
        }
      }
    };

    private Handler<Component> childFailHandler = new Handler<Component>() {
      @Override
      public void handle(Component component) {
        if (!failed()) {
          fail();
        }
      }
    };

    private Component(String id) {
      this.id = id;
      this.timer = null;
    }

    private Component(String id, Timer timer) {
      this.id = id;
      this.timer = timer;
    }

    /**
     * Indicates that the message is ready to be acked.
     */
    private void ready() {
      ready = true;
      checkAck();
    }

    /**
     * Acks the message.
     */
    private void ack() {
      acked = true; ack = true;
      checkAck();
    }

    /**
     * Fails the message.
     */
    private void fail() {
      acked = true; ack = false;
      checkAck();
    }

    /**
     * Indicates whether the message has been failed.
     */
    private final boolean failed() {
      return acked && !ack;
    }

    /**
     * Checks whether the ack or fail handler can be invoked.
     */
    private void checkAck() {
      if (!locked && acked) {
        // If the message is ready and acked then invoke the ack handler.
        if (ack && ready) {
          locked = true;
          ackHandler.handle(this);
        }
        // If the message was failed then immediately invoke the fail handler.
        else if (!ack) {
          locked = true;
          failHandler.handle(this);
        }
      }
    }

    /**
     * Sets an ack handler on the message.
     */
    private void ackHandler(Handler<Component> handler) {
      this.ackHandler = handler;
    }

    /**
     * Sets a fail handler on the message.
     */
    private void failHandler(Handler<Component> handler) {
      this.failHandler = handler;
    }

    /**
     * Adds a child to the message.
     *
     * @param child
     *   The child message.
     */
    private final void addChild(Component child) {
      children.add(child);
      ready = false;
      child.ackHandler(childAckHandler);
      child.failHandler(childFailHandler);
    }
  }

}
