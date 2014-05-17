/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.cluster.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.platform.PlatformManager;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.hazelcast.core.MultiMap;

/**
 * Default cluster manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultClusterManager implements ClusterManager {
  private final String cluster;
  private final Vertx vertx;
  private final PlatformManager platform;
  private final ClusterListener listener;
  private final ClusterData data;
  private final MultiMap<String, String> nodes;
  private final MultiMap<String, String> groups;
  private final MultiMap<String, String> deployments;
  private final Map<Object, String> groupSelectors;
  private final Map<Object, String> nodeSelectors;

  private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String action = message.body().getString("action");
      if (action != null) {
        switch (action) {
          case "find":
            doFind(message);
            break;
          case "list":
            doList(message);
            break;
          case "select":
            doSelect(message);
            break;
          case "deploy":
            doDeploy(message);
            break;
          case "undeploy":
            doUndeploy(message);
            break;
          default:
            String type = message.body().getString("type");
            if (type == null) {
              message.reply(new JsonObject().putString("status", "error").putString("message", "No data type specified."));
              return;
            }

            switch (type) {
              case "key":
                switch (action) {
                  case "get":
                    doKeyGet(message);
                    break;
                  case "set":
                    doKeySet(message);
                    break;
                  case "delete":
                    doKeyDelete(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
              case "counter":
                switch (action) {
                  case "increment":
                    doCounterIncrement(message);
                    break;
                  case "decrement":
                    doCounterDecrement(message);
                    break;
                  case "get":
                    doCounterGet(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
              case "map":
                switch (action) {
                  case "put":
                    doMapPut(message);
                    break;
                  case "get":
                    doMapGet(message);
                    break;
                  case "remove":
                    doMapRemove(message);
                    break;
                  case "contains":
                    doMapContainsKey(message);
                    break;
                  case "keys":
                    doMapKeys(message);
                    break;
                  case "values":
                    doMapValues(message);
                    break;
                  case "empty":
                    doMapIsEmpty(message);
                    break;
                  case "clear":
                    doMapClear(message);
                    break;
                  case "size":
                    doMapSize(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
              case "list":
                switch (action) {
                  case "add":
                    doListAdd(message);
                    break;
                  case "get":
                    doListGet(message);
                    break;
                  case "remove":
                    doListRemove(message);
                    break;
                  case "contains":
                    doListContains(message);
                    break;
                  case "size":
                    doListSize(message);
                    break;
                  case "empty":
                    doListIsEmpty(message);
                    break;
                  case "clear":
                    doListClear(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
              case "set":
                switch (action) {
                  case "add":
                    doSetAdd(message);
                    break;
                  case "remove":
                    doSetRemove(message);
                    break;
                  case "contains":
                    doSetContains(message);
                    break;
                  case "size":
                    doSetSize(message);
                    break;
                  case "empty":
                    doSetIsEmpty(message);
                    break;
                  case "clear":
                    doSetClear(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
              case "queue":
                switch (action) {
                  case "add":
                    doQueueAdd(message);
                    break;
                  case "remove":
                    doQueueRemove(message);
                    break;
                  case "contains":
                    doQueueContains(message);
                    break;
                  case "empty":
                    doQueueIsEmpty(message);
                    break;
                  case "size":
                    doQueueSize(message);
                    break;
                  case "clear":
                    doQueueClear(message);
                    break;
                  case "offer":
                    doQueueOffer(message);
                    break;
                  case "element":
                    doQueueElement(message);
                    break;
                  case "poll":
                    doQueuePoll(message);
                    break;
                  case "peek":
                    doQueuePeek(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
            }
            break;
        }
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Must specify an action"));
      }
    }
  };

  private final Handler<String> joinHandler = new Handler<String>() {
    @Override
    public void handle(String nodeID) {
      doNodeJoined(nodeID);
    }
  };

  private final Handler<String> leaveHandler = new Handler<String>() {
    @Override
    public void handle(String nodeID) {
      doNodeLeft(nodeID);
    }
  };

  public DefaultClusterManager(String cluster, Vertx vertx, Container container, PlatformManager platform, ClusterListener listener, ClusterData data) {
    this.cluster = cluster;
    this.vertx = vertx;
    this.platform = platform;
    this.listener = listener;
    this.data = data;
    this.nodes = data.getMultiMap(String.format("nodes.%s", cluster));
    this.groups = data.getMultiMap(String.format("groups.%s", cluster));
    this.deployments = data.getMultiMap(String.format("deployments.%s", cluster));
    this.groupSelectors = data.getMap(String.format("selectors.group.%s", cluster));
    this.nodeSelectors = data.getMap(String.format("selectors.node.%s", cluster));
  }

  @Override
  public String address() {
    return cluster;
  }

  @Override
  public ClusterManager start() {
    return start(null);
  }

  @Override
  public ClusterManager start(Handler<AsyncResult<Void>> doneHandler) {
    listener.joinHandler(joinHandler);
    listener.leaveHandler(leaveHandler);
    vertx.eventBus().registerHandler(address(), messageHandler, doneHandler);
    return this;
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    listener.joinHandler(null);
    listener.leaveHandler(null);
    vertx.eventBus().unregisterHandler(address(), messageHandler, doneHandler);
  }

  /**
   * Formats a key for the cluster.
   */
  private String formatKey(String key) {
    return String.format("%s.%s", cluster, key);
  }

  /**
   * Called when a node joins the cluster.
   */
  private void doNodeJoined(final String nodeID) {
    // Do nothing.
  }

  /**
   * Called when a node leaves the cluster.
   */
  private synchronized void doNodeLeft(final String nodeID) {
    Collection<String> nodes = this.nodes.remove(nodeID);
    if (nodes != null) {
      synchronized (groups) {
        for (String node : nodes) {
          for (String group : groups.keySet()) {
            groups.remove(group, node);
          }
        }
      }
    }
  }

  /**
   * Finds a node in the cluster.
   */
  private void doFind(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type != null) {
      switch (type) {
        case "group":
          doFindGroup(message);
          break;
        case "node":
          doFindNode(message);
          break;
        default:
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid type specified."));
          break;
      }
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No type specified."));
    }
  }

  /**
   * Finds a group in the cluster.
   */
  private void doFindGroup(final Message<JsonObject> message) {
    String group = message.body().getString("group");
    if (group == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid group name."));
      return;
    }

    String address = String.format("%s.%s", cluster, group);
    if (groups.containsKey(address)) {
      message.reply(new JsonObject().putString("status", "ok").putString("result", address));
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid group."));
    }
  }

  /**
   * Finds a node in the cluster.
   */
  private void doFindNode(final Message<JsonObject> message) {
    String node = message.body().getString("node");
    if (node == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid node address."));
      return;
    }

    for (String group : groups.keySet()) {
      String address = String.format("%s.%s", group, node);
      if (groups.containsEntry(group, address)) {
        message.reply(new JsonObject().putString("status", "ok").putString("result", address));
        return;
      }
    }
    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid node."));
  }

  /**
   * Lists objects in the cluster.
   */
  private void doList(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type != null) {
      switch (type) {
        case "group":
          doListGroup(message);
          break;
        case "node":
          doListNode(message);
          break;
        default:
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid type specified."));
          break;
      }
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No type specified."));
    }
  }

  /**
   * Lists groups in the cluster.
   */
  private void doListGroup(final Message<JsonObject> message) {
    JsonArray groups = new JsonArray();
    for (String group : this.groups.keySet()) {
      groups.addString(group);
    }
    message.reply(new JsonObject().putString("status", "ok").putArray("result", groups));
  }

  /**
   * Lists nodes in the cluster.
   */
  private void doListNode(final Message<JsonObject> message) {
    List<String> nodes = new ArrayList<>();
    for (String group : this.groups.keySet()) {
      nodes.addAll(groups.get(group));
    }
    message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(nodes.toArray(new String[nodes.size()]))));
  }

  /**
   * Selects an object in the cluster.
   */
  private void doSelect(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type != null) {
      switch (type) {
        case "group":
          doSelectGroup(message);
          break;
        case "node":
          doSelectNode(message);
          break;
        default:
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid type specified."));
          break;
      }
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No type specified."));
    }
  }

  /**
   * Selects a group in the cluster.
   */
  private void doSelectGroup(final Message<JsonObject> message) {
    Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
    } else {
      String address = selectGroup(key);
      if (address == null) {
        message.reply(new JsonObject().putString("status", "error").putString("message", "No groups to select."));
      } else {
        message.reply(new JsonObject().putString("status", "ok").putString("result", address));
      }
    }
  }

  /**
   * Selects a group in the cluster.
   */
  private String selectGroup(Object key) {
    String address = groupSelectors.get(key);
    if (address != null) {
      return address;
    }
    Set<String> groups = this.groups.keySet();
    int index = new Random().nextInt(groups.size());
    int i = 0;
    for (String group : groups) {
      if (i == index) {
        groupSelectors.put(key, group);
        return group;
      }
      i++;
    }
    return null;
  }

  /**
   * Selects a node in the cluster.
   */
  private void doSelectNode(final Message<JsonObject> message) {
    Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
    } else {
      String address = selectNode(key);
      if (address == null) {
        message.reply(new JsonObject().putString("status", "error").putString("message", "No nodes to select."));
      } else {
        message.reply(new JsonObject().putString("status", "ok").putString("result", address));
      }
    }
  }

  /**
   * Selects a node in the cluster.
   */
  private String selectNode(Object key) {
    String address = nodeSelectors.get(key);
    if (address != null) {
      return address;
    }
    Set<String> nodes = new HashSet<>();
    for (String group : groups.keySet()) {
      nodes.addAll(groups.get(group));
    }
    int index = new Random().nextInt(nodes.size());
    int i = 0;
    for (String node : nodes) {
      if (i == index) {
        nodeSelectors.put(key, node);
        return node;
      }
      i++;
    }
    return null;
  }

  /**
   * Deploys a module or verticle.
   */
  private void doDeploy(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment type specified."));
    } else {
      switch (type) {
        case "module":
          doDeployModule(message);
          break;
        case "verticle":
          doDeployVerticle(message);
          break;
        default:
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid deployment type."));
          break;
      }
    }
  }

  /**
   * Deploys a module
   */
  private void doDeployModule(final Message<JsonObject> message) {
    String moduleName = message.body().getString("module");
    if (moduleName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module name specified."));
      return;
    }

    JsonObject config = message.body().getObject("config");
    if (config == null) {
      config = new JsonObject();
    }
    int instances = message.body().getInteger("instances", 1);
    platform.deployModule(moduleName, config, instances, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          deployments.put(cluster, message.body().copy().putString("id", result.result()).encode());
          message.reply(new JsonObject().putString("status", "ok").putString("id", result.result()));
        }
      }
    });
  }

  /**
   * Deploys a verticle.
   */
  private void doDeployVerticle(final Message<JsonObject> message) {
    String main = message.body().getString("main");
    if (main == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No verticle main specified."));
      return;
    }

    JsonObject config = message.body().getObject("config");
    if (config == null) {
      config = new JsonObject();
    }
    int instances = message.body().getInteger("instances", 1);
    boolean worker = message.body().getBoolean("worker", false);
    if (worker) {
      boolean multiThreaded = message.body().getBoolean("multi-threaded", false);
      platform.deployWorkerVerticle(main, config, instances, multiThreaded, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            deployments.put(cluster, message.body().copy().putString("id", result.result()).encode());
            message.reply(new JsonObject().putString("status", "ok").putString("id", result.result()));
          }
        }
      });
    } else {
      platform.deployVerticle(main, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            deployments.put(cluster, message.body().copy().putString("id", result.result()).encode());
            message.reply(new JsonObject().putString("status", "ok").putString("id", result.result()));
          }
        }
      });
    }
  }

  /**
   * Undeploys a module or verticle.
   */
  private void doUndeploy(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment type specified."));
    } else {
      switch (type) {
        case "module":
          doUndeployModule(message);
          break;
        case "verticle":
          doUndeployVerticle(message);
          break;
        default:
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid deployment type " + type));
          break;
      }
    }
  }

  /**
   * Undeploys a module.
   */
  private void doUndeployModule(final Message<JsonObject> message) {
    String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      removeDeployment(deploymentID);
      platform.undeployModule(deploymentID, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok"));
          }
        }
      });
    }
  }

  /**
   * Undeploys a verticle.
   */
  private void doUndeployVerticle(final Message<JsonObject> message) {
    String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      removeDeployment(deploymentID);
      platform.undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok"));
          }
        }
      });
    }
  }

  /**
   * Removes a deployment from the deployments map.
   */
  private void removeDeployment(String deploymentID) {
    Collection<String> deployments = this.deployments.get(cluster);
    String deployment = null;
    for (String sdeployment : deployments) {
      JsonObject info = new JsonObject(sdeployment);
      if (info.getString("id").equals(deploymentID)) {
        deployment = sdeployment;
        break;
      }
    }
    if (deployment != null) {
      this.deployments.remove(cluster, deployment);
    }
  }

  /**
   * Handles setting a key.
   */
  private void doKeySet(final Message<JsonObject> message) {
    final String key = message.body().getString("name");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    final Object value = message.body().getValue("value");

    try {
      data.getMap(formatKey("keys")).put(key, value);
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles getting a key.
   */
  private void doKeyGet(final Message<JsonObject> message) {
    final String key = message.body().getString("name");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    try {
      Object value = data.getMap(formatKey("keys")).get(key);
      message.reply(new JsonObject().putString("status", "ok").putValue("result", value));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles deleting a key.
   */
  private void doKeyDelete(final Message<JsonObject> message) {
    final String key = message.body().getString("name");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    try {
      data.getMap(formatKey("keys")).remove(key);
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles getting a counter.
   */
  private void doCounterGet(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Map<Object, Long> counters = data.getMap(formatKey("counters"));
      Long value = counters.get(name);
      if (value == null) {
        value = 0L;
      }
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", value));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles incrementing a counter.
   */
  private void doCounterIncrement(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Map<Object, Long> counters = data.getMap(formatKey("counters"));
      Long value = counters.get(name);
      if (value == null) {
        value = 0L;
      }
      value++;
      counters.put(name, value);
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", value));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles decrementing a counter.
   */
  private void doCounterDecrement(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Map<Object, Long> counters = data.getMap(formatKey("counters"));
      Long value = counters.get(name);
      if (value == null) {
        value = 0L;
      }
      value--;
      counters.put(name, value);
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", value));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster map put command.
   */
  private void doMapPut(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      Object result = data.getMap(formatKey(name)).put(key, value);
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster map get command.
   */
  private void doMapGet(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final String key = message.body().getString("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    try {
      Object result = data.getMap(formatKey(name)).get(key);
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster map remove command.
   */
  private void doMapRemove(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final String key = message.body().getString("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    try {
      Object result = data.getMap(formatKey(name)).remove(key);
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster exists command.
   */
  private void doMapContainsKey(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final String key = message.body().getString("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    try {
      boolean result = data.getMap(formatKey(name)).containsKey(key);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles map keys command.
   */
  private void doMapKeys(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Set<Object> result = data.getMap(formatKey(name)).keySet();
      message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.toArray())));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles map values command.
   */
  private void doMapValues(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Collection<Object> result = data.getMap(formatKey(name)).values();
      message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.toArray())));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles cluster map is empty command.
   */
  private void doMapIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      boolean result = data.getMap(formatKey(name)).isEmpty();
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Counts the number of items in a map.
   */
  private void doMapSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      int result = data.getMap(formatKey(name)).size();
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Clears all items in a map.
   */
  private void doMapClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      data.getMap(formatKey(name)).clear();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a list addition.
   */
  private void doListAdd(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = data.getList(formatKey(name)).add(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a list get.
   */
  private void doListGet(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Integer index = message.body().getInteger("index");
    if (index == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No index specified."));
      return;
    }

    try {
      Object result = data.getList(formatKey(name)).get(index);
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a list removal.
   */
  private void doListRemove(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    if (message.body().containsField("index")) {
      final int index = message.body().getInteger("index");
      try {
        Object result = data.getList(formatKey(name)).remove(index);
        message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
      } catch (Exception e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
      }
    } else {
      final Object value = message.body().getValue("value");
      if (value == null) {
        message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      } else {
        try {
          boolean result = data.getList(formatKey(name)).remove(value);
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
        } catch (Exception e) {
          message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
        }
      }
    }
  }

  /**
   * Checks whether a list contains a value.
   */
  private void doListContains(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = data.getList(formatKey(name)).contains(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles cluster list is empty command.
   */
  private void doListIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      boolean result = data.getList(formatKey(name)).isEmpty();
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Counts the number of items in a list.
   */
  private void doListSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      int result = data.getList(formatKey(name)).size();
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Clears all items in a list.
   */
  private void doListClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      data.getList(formatKey(name)).clear();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a set addition.
   */
  private void doSetAdd(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = data.getSet(formatKey(name)).add(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a set removal.
   */
  private void doSetRemove(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
    } else {
      try {
        boolean result = data.getSet(formatKey(name)).remove(value);
        message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
      } catch (Exception e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
      }
    }
  }

  /**
   * Checks whether a set contains a value.
   */
  private void doSetContains(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = data.getSet(formatKey(name)).contains(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles cluster set is empty command.
   */
  private void doSetIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      boolean result = data.getSet(formatKey(name)).isEmpty();
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Counts the number of items in a set.
   */
  private void doSetSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      int result = data.getSet(formatKey(name)).size();
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Clears all items in a set.
   */
  private void doSetClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      data.getSet(formatKey(name)).clear();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue addition.
   */
  private void doQueueAdd(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = data.getQueue(formatKey(name)).add(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue removal.
   */
  private void doQueueRemove(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
    } else {
      try {
        boolean result = data.getQueue(formatKey(name)).remove(value);
        message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
      } catch (Exception e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
      }
    }
  }

  /**
   * Checks whether a queue contains a value.
   */
  private void doQueueContains(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = data.getQueue(formatKey(name)).contains(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles cluster queue is empty command.
   */
  private void doQueueIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      boolean result = data.getQueue(formatKey(name)).isEmpty();
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Counts the number of items in a queue.
   */
  private void doQueueSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      int result = data.getQueue(formatKey(name)).size();
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Clears all items in a queue.
   */
  private void doQueueClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      data.getQueue(formatKey(name)).clear();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue offer command.
   */
  private void doQueueOffer(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = data.getQueue(formatKey(name)).offer(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue element command.
   */
  private void doQueueElement(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Object result = data.getQueue(formatKey(name)).element();
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue poll command.
   */
  private void doQueuePoll(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Object result = data.getQueue(formatKey(name)).poll();
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue peek command.
   */
  private void doQueuePeek(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Object result = data.getQueue(formatKey(name)).peek();
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

}
