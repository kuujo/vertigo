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
package net.kuujo.vertigo.auditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Observes component message trees and manages ack/fail.
 *
 * @author Jordan Halterman
 */
public final class Auditor extends BusModBase implements Handler<Message<JsonObject>> {
  private String address;
  private String broadcastAddress;
  private boolean enabled;
  private long timeout = 30000;
  private long delay = 0;
  private long cleanupInterval = 500;
  private Map<String, Root> roots = new LinkedHashMap<String, Root>();
  private Map<String, Node> nodes = new HashMap<String, Node>();

  public static final String ADDRESS = "address";
  public static final String BROADCAST = "broadcast";
  public static final String ENABLED = "enabled";
  public static final String TIMEOUT = "timeout";
  public static final String DELAY = "delay";
  public static final String CLEANUP_INTERVAL = "cleanup";

  @Override
  public void start() {
    super.start();
    address = getMandatoryStringConfig(ADDRESS);
    broadcastAddress = getMandatoryStringConfig(BROADCAST);
    enabled = getOptionalBooleanConfig(ENABLED, true);
    timeout = getOptionalLongConfig(TIMEOUT, timeout);
    delay = getOptionalLongConfig(DELAY, delay);
    cleanupInterval = getOptionalLongConfig(CLEANUP_INTERVAL, cleanupInterval);
    eb.registerHandler(address, this);
    startTimer();
  }

  /**
   * Starts a periodic timer that checks for timed out messages.
   */
  private void startTimer() {
    vertx.setPeriodic(cleanupInterval, new Handler<Long>() {
      @Override
      public void handle(Long timerId) {
        checkTimeout();
      }
    });
  }

  /**
   * Checks messages for timeouts.
   */
  private void checkTimeout() {
    // Iterate over nodes and fail any nodes whose expiration time has passed.
    // Nodes are stored in a LinkedHashMap in the order in which they're created,
    // so we can iterate up to the oldest node which has not yet timed out and stop.
    long currentTime = System.currentTimeMillis();
    Iterator<Map.Entry<String, Root>> iterator = roots.entrySet().iterator();
    while (iterator.hasNext()) {
      Root root = iterator.next().getValue();
      if (root.timeout <= currentTime) {
        iterator.remove();
        root.timeout();
      }
      else {
        break;
      }
    }
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

  private Handler<Node> ackHandler = new Handler<Node>() {
    @Override
    public void handle(Node root) {
      eb.publish(broadcastAddress, new JsonObject().putString("action", "ack").putString("id", root.id));
      for (Node child : root.children) {
        clearNode(child);
      }
      roots.remove(root.id);
    }
  };

  private Handler<Node> failHandler = new Handler<Node>() {
    @Override
    public void handle(Node root) {
      eb.publish(broadcastAddress, new JsonObject().putString("action", "fail").putString("id", root.id));
      for (Node child : root.children) {
        clearNode(child);
      }
      roots.remove(root.id);
    }
  };

  private Handler<Node> timeoutHandler = new Handler<Node>() {
    @Override
    public void handle(Node root) {
      eb.publish(broadcastAddress, new JsonObject().putString("action", "timeout").putString("id", root.id));
      for (Node child : root.children) {
        clearNode(child);
      }
    }
  };

  /**
   * Creates a message.
   */
  private void doCreate(Message<JsonObject> message) {
    String id = getMandatoryString("id", message);
    if (enabled) {
      // If no forks were defined for the message, just send a failure.
      JsonArray forks = message.body().getArray("forks");
      if (forks == null || forks.size() == 0) {
        eb.publish(broadcastAddress, new JsonObject().putString("action", "ack").putString("id", id));
        return;
      }

       // We simply add ack and fail handlers to the root node. If a descendant
      // node is failed then it will bubble up to the root. Once all child nodes
      // are acked the parent will be acked which will also bubble up to the root.
      Root root = new Root(id, vertx, System.currentTimeMillis() + timeout, delay);
      root.ackHandler = ackHandler;
      root.failHandler = failHandler;
      root.timeoutHandler = timeoutHandler;
      roots.put(root.id, root);

      // Add all initial forks for the root.
      for (Object forkId : forks) {
        Node fork = new Node((String) forkId, vertx, delay);
        root.addChild(fork);
        nodes.put(fork.id, fork);
      }
    }
    // If acking is disabled then immediately ack the message back to its source.
    else {
      eb.publish(broadcastAddress, new JsonObject().putString("action", "ack").putString("id", id));
    }
  }

  /**
   * Clears all children of a node from storage.
   */
  private void clearNode(Node node) {
    nodes.remove(node.id);
  }

  /**
   * Creates a message fork.
   */
  private void doFork(Message<JsonObject> message) {
    String parentId = getMandatoryString("parent", message);
    if (nodes.containsKey(parentId)) {
      JsonArray forks = message.body().getArray("forks");
      if (forks != null) {
        for (Object forkId : forks) {
          Node node = new Node((String) forkId, vertx, delay);
          nodes.get(parentId).addChild(node);
          nodes.put(node.id, node);
        }
      }
    }
  }

  /**
   * Acks a message.
   */
  private void doAck(Message<JsonObject> message) {
    String id = getMandatoryString("id", message);
    if (nodes.containsKey(id)) {
      nodes.get(id).ack();
    }
  }

  /**
   * Fails a message.
   */
  private void doFail(Message<JsonObject> message) {
    String id = getMandatoryString("id", message);
    if (nodes.containsKey(id)) {
      nodes.get(id).fail();
    }
  }

  /**
   * Represents a single node in a message tree.
   */
  private static class Node {
    final String id;
    private final Vertx vertx;
    private final long delay;
    private long delayTimer;
    final List<Node> children = new ArrayList<>();
    private final List<Node> complete = new ArrayList<>();
    Handler<Node> ackHandler;
    Handler<Node> failHandler;
    private boolean ready = true;
    private boolean acked;
    private boolean ack;
    private boolean locked;

    private Handler<Node> childAckHandler = new Handler<Node>() {
      @Override
      public void handle(Node node) {
        if (!failed()) {
          complete.add(node);
          if (complete.size() == children.size()) {
            ready();
          }
        }
      }
    };

    private Handler<Node> childFailHandler = new Handler<Node>() {
      @Override
      public void handle(Node node) {
        if (!failed()) {
          fail();
        }
      }
    };

    private Handler<Long> delayHandler = new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        locked = true;
        ackHandler.handle(Node.this);
      }
    };

    private Node(String id, Vertx vertx, long delay) {
      this.id = id;
      this.vertx = vertx;
      this.delay = delay;
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
    protected final void ack() {
      acked = true; ack = true;
      checkAck();
    }

    /**
     * Fails the message.
     */
    protected final void fail() {
      acked = true; ack = false;
      checkAck();
    }

    /**
     * Indicates whether the message has been failed.
     */
    protected final boolean failed() {
      return acked && !ack;
    }

    /**
     * Checks whether the ack or fail handler can be invoked.
     */
    private void checkAck() {
      // If the node is not locked but has been acked then call handlers.
      if (!locked && acked) {
        // If the node is ready and acked then invoke the ack handler.
        if (ack && ready) {
          // If the auditor is using a delay timer, set the timer. If a child
          // node is added to the tree before the timer expires the timer
          // will be removed.
          if (delay > 0) {
            startDelay();
          }
          // Otherwise, just call the ack handler immediately.
          else {
            locked = true;
            ackHandler.handle(this);
          }
        }
        // Otherwise if the node is failed then call the fail handler
        // and lock the node.
        else if (!ack) {
          locked = true;
          failHandler.handle(this);
        }
      }
    }

    /**
     * Starts the delay timer.
     */
    private void startDelay() {
      if (delay > 0 && delayTimer == 0) {
        delayTimer = vertx.setTimer(delay, delayHandler);
      }
    }

    /**
     * Stops the delay timer.
     */
    private void stopDelay() {
      if (delayTimer > 0) {
        vertx.cancelTimer(delayTimer);
        delayTimer = 0;
      }
    }

    /**
     * Adds a child to the message.
     *
     * @param child
     *   The child message.
     */
    protected void addChild(Node child) {
      children.add(child);
      ready = false;
      child.ackHandler = childAckHandler;
      child.failHandler = childFailHandler;
      stopDelay();
    }
  }

  /**
   * Represents the root element of a message tree.
   */
  private static final class Root extends Node {
    private final long timeout;
    Handler<Node> timeoutHandler;

    private Root(String id, Vertx vertx, long timeout, long delay) {
      super(id, vertx, delay);
      this.timeout = timeout;
    }

    private final void timeout() {
      timeoutHandler.handle(this);
    }

    @Override
    protected final void addChild(Node child) {
      super.addChild(child);
      if (!failed()) {
        ack();
      }
    }
  }

}
