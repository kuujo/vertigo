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
package net.kuujo.vevent.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Monitors message trees and manages ack/fail.
 *
 * @author Jordan Halterman
 */
public class Authenticator extends BusModBase implements Handler<Message<JsonObject>> {

  private String address;

  private String broadcastAddress;

  private Map<String, Node> nodes = new HashMap<>();

  @Override
  public void start() {
    super.start();
    address = getMandatoryStringConfig("address");
    broadcastAddress = getMandatoryStringConfig("broadcast");
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

  private Handler<Node> ackHandler = new Handler<Node>() {
    @Override
    public void handle(Node node) {
      clearRoot(node);
      eb.publish(broadcastAddress, new JsonObject().putString("action", "ack").putString("id", node.id()));
    }
  };

  private Handler<Node> failHandler = new Handler<Node>() {
    @Override
    public void handle(Node node) {
      clearRoot(node);
      eb.publish(broadcastAddress, new JsonObject().putString("action", "fail").putString("id", node.id()));
    }
  };

  /**
   * Creates a message.
   */
  private void doCreate(Message<JsonObject> message) {
    // We simply add ack and fail handlers to the root node. If a descendant
    // node is failed then it will bubble up to the root. Once all child nodes
    // are acked the parent will be acked which will also bubble up to the root.
    String id = getMandatoryString("id", message);
    Node node = new Node(id);
    node.ackHandler(ackHandler);
    node.failHandler(failHandler);
    nodes.put(node.id(), node);
  }

  /**
   * Clears an entire message tree from storage.
   */
  private void clearRoot(Node node) {
    clearNode(node);
  }

  /**
   * Clears all children of a node from storage.
   */
  private void clearNode(Node node) {
    if (node.hasChildren()) {
      for (Node child : node.children()) {
        clearNode(child);
      }
      if (nodes.containsKey(node.id())) {
        nodes.remove(node.id());
      }
    }
    else if (nodes.containsKey(node.id())) {
      nodes.remove(node.id());
    }
  }

  /**
   * Creates a message fork.
   */
  private void doFork(Message<JsonObject> message) {
    String parentId = getMandatoryString("parent", message);
    if (nodes.containsKey(parentId)) {
      String id = getMandatoryString("id", message);
      Node node = new Node(id);
      nodes.get(parentId).addChild(node);
      nodes.put(id, node);
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
  private static final class Node {
    private String id;
    private Set<Node> children = new HashSet<>();
    private Set<Node> complete = new HashSet<>();
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

    public Node(String id) {
      this.id = id;
    }

    /**
     * Returns the node ID.
     */
    public final String id() {
      return id;
    }

    /**
     * Indicates that the message is ready to be acked.
     */
    public void ready() {
      ready = true;
      checkAck();
    }

    /**
     * Acks the message.
     */
    public void ack() {
      acked = true; ack = true;
      checkAck();
    }

    /**
     * Fails the message.
     */
    public void fail() {
      acked = true; ack = false;
      checkAck();
    }

    /**
     * Indicates whether the message has been failed.
     */
    public final boolean failed() {
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
    public void ackHandler(Handler<Node> handler) {
      this.ackHandler = handler;
    }

    /**
     * Sets a fail handler on the message.
     */
    public void failHandler(Handler<Node> handler) {
      this.failHandler = handler;
    }

    /**
     * Adds a child to the message.
     *
     * @param child
     *   The child message.
     */
    public final void addChild(Node child) {
      children.add(child);
      ready = false;
      child.ackHandler(childAckHandler);
      child.failHandler(childFailHandler);
    }

    /**
     * Returns all node children.
     */
    public final Set<Node> children() {
      return children;
    }

    /**
     * Returns a boolean indicating whether the node has children.
     */
    public final boolean hasChildren() {
      return children.size() > 0;
    }
  }

}
