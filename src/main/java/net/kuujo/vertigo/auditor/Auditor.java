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
  private long expire = 30000;
  private long delay = 0;
  private long cleanupInterval = 500;
  private Map<String, Node> nodes = new LinkedHashMap<>();

  public static final String ADDRESS = "address";
  public static final String BROADCAST = "broadcast";
  public static final String ENABLED = "enabled";
  public static final String EXPIRE = "expire";
  public static final String DELAY = "delay";
  public static final String CLEANUP_INTERVAL = "cleanup";

  @Override
  public void start() {
    super.start();
    address = getMandatoryStringConfig(ADDRESS);
    broadcastAddress = getMandatoryStringConfig(BROADCAST);
    enabled = getOptionalBooleanConfig(ENABLED, true);
    expire = getOptionalLongConfig(EXPIRE, expire);
    delay = getOptionalLongConfig(DELAY, delay);
    cleanupInterval = getOptionalLongConfig(CLEANUP_INTERVAL, cleanupInterval);
    eb.registerHandler(address, this);
    startTimer();
  }

  /**
   * Starts a periodic timer that checks for expired messages.
   */
  private void startTimer() {
    vertx.setPeriodic(cleanupInterval, new Handler<Long>() {
      @Override
      public void handle(Long timerId) {
        checkExpire();
      }
    });
  }

  /**
   * Checks messages for expirations.
   */
  private void checkExpire() {
    // Iterate over nodes and fail any nodes whose expiration time has passed.
    // Nodes are stored in a LinkedHashMap in the order in which they're created,
    // so we can iterate up to the oldest node which has not yet expired and stop.
    long currentTime = System.currentTimeMillis();
    Iterator<Map.Entry<String, Node>> iterator = nodes.entrySet().iterator();
    while (iterator.hasNext()) {
      Node node = iterator.next().getValue();
      if (node.expire <= currentTime) {
        iterator.remove();
        node.fail();
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
    public void handle(Node node) {
      eb.publish(broadcastAddress, new JsonObject().putString("action", "ack").putString("id", node.id));
    }
  };

  private Handler<Node> failHandler = new Handler<Node>() {
    @Override
    public void handle(Node node) {
      eb.publish(broadcastAddress, new JsonObject().putString("action", "fail").putString("id", node.id));
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
      Node node = new Root(id, vertx, System.currentTimeMillis() + expire, delay);
      node.ackHandler(ackHandler);
      node.failHandler(failHandler);
      nodes.put(node.id, node);

      // Add all initial forks for the root.
      for (Object forkId : forks) {
        Node fork = new Node((String) forkId, vertx, System.currentTimeMillis() + expire, delay);
        node.addChild(fork);
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
          Node node = new Node((String) forkId, vertx, System.currentTimeMillis() + expire, delay);
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
    String failMessage = message.body().getString("message");
    if (nodes.containsKey(id)) {
      nodes.get(id).fail(failMessage);
    }
  }

  /**
   * Represents a single node in a message tree.
   */
  private class Node {
    private final String id;
    private final Vertx vertx;
    private final long expire;
    private final long delay;
    private long delayTimer;
    private final List<Node> children = new ArrayList<>();
    private final List<Node> complete = new ArrayList<>();
    private Handler<Node> ackHandler;
    private Handler<Node> failHandler;
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

    private Node(String id, Vertx vertx, long expire, long delay) {
      this.id = id;
      this.vertx = vertx;
      this.expire = expire;
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
     * Fails the message.
     */
    protected final void fail(String message) {
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
      if (!locked && acked) {
        // If the message is ready and acked then invoke the ack handler.
        if (ack && ready) {
          // If the auditor is using a delay timer, set the timer. If a child
          // message is added to the tree before the timer expires the timer
          // will be removed.
          if (delay > 0) {
            delayTimer = vertx.setTimer(delay, new Handler<Long>() {
              @Override
              public void handle(Long timerID) {
                locked = true;
                ackHandler.handle(Node.this);
                clearNode(Node.this);
              }
            });
          }
          else {
            locked = true;
            ackHandler.handle(this);
            clearNode(Node.this);
          }
        }
        // If the message was failed then immediately invoke the fail handler.
        else if (!ack) {
          locked = true;
          failHandler.handle(this);
          clearNode(Node.this);
        }
      }
    }

    /**
     * Sets an ack handler on the message.
     */
    private void ackHandler(Handler<Node> handler) {
      this.ackHandler = handler;
    }

    /**
     * Sets a fail handler on the message.
     */
    private void failHandler(Handler<Node> handler) {
      this.failHandler = handler;
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
      child.ackHandler(childAckHandler);
      child.failHandler(childFailHandler);
      if (delay > 0 && delayTimer > 0) {
        vertx.cancelTimer(delayTimer);
        delayTimer = 0;
      }
    }
  }

  /**
   * Represents the root element of a message tree.
   */
  private final class Root extends Node {
    private Root(String id, Vertx vertx, long expire, long delay) {
      super(id, vertx, expire, delay);
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
