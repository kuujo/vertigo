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

import java.util.Collection;
import java.util.Map;
import java.util.Random;

import net.kuujo.vertigo.cluster.GroupManager;
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
 * Default group manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultGroupManager implements GroupManager {
  private final String group;
  private final Vertx vertx;
  private final PlatformManager platform;
  private final MultiMap<String, String> groups;
  private final MultiMap<String, String> deployments;
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
            message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
            break;
        }
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Must specify an action"));
      }
    }
  };

  public DefaultGroupManager(String group, String cluster, Vertx vertx, Container container, PlatformManager platform, ClusterListener listener, ClusterData data) {
    this.group = group;
    this.vertx = vertx;
    this.platform = platform;
    this.groups = data.getMultiMap(String.format("groups.%s", cluster));
    this.deployments = data.getMultiMap(String.format("deployments.%s", cluster));
    this.nodeSelectors = data.getMap(String.format("selectors.node.%s", group));
  }

  @Override
  public String address() {
    return group;
  }

  @Override
  public GroupManager start() {
    return start(null);
  }

  @Override
  public GroupManager start(Handler<AsyncResult<Void>> doneHandler) {
    vertx.eventBus().registerHandler(group, messageHandler, doneHandler);
    return this;
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    groups.remove(group);
    vertx.eventBus().unregisterHandler(group, messageHandler, doneHandler);
  }

  /**
   * Finds a node in the group.
   */
  private void doFind(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type != null) {
      switch (type) {
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
   * Finds a node in the group.
   */
  private void doFindNode(final Message<JsonObject> message) {
    String nodeName = message.body().getString("node");
    if (nodeName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No node specified."));
      return;
    }

    String address = String.format("%s.%s", group, nodeName);
    if (groups.containsEntry(group, address)) {
      message.reply(new JsonObject().putString("status", "ok").putString("result", address));
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid node."));
    }
  }

  /**
   * Lists objects in the group.
   */
  private void doList(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type != null) {
      switch (type) {
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
   * Lists nodes in the group.
   */
  private void doListNode(final Message<JsonObject> message) {
    Collection<String> nodes = groups.get(group);
    if (nodes == null) {
      message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray()));
    } else {
      message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(nodes.toArray(new String[nodes.size()]))));
    }
  }

  /**
   * Selects an object in the group.
   */
  private void doSelect(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type != null) {
      switch (type) {
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
   * Selects a node in the group.
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
   * Selects a node in the group.
   */
  private String selectNode(Object key) {
    String address = nodeSelectors.get(key);
    if (address != null) {
      return address;
    }
    Collection<String> nodes = groups.get(group);
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
          deployments.put(group, message.body().copy().putString("id", result.result()).encode());
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
            deployments.put(group, message.body().copy().putString("id", result.result()).encode());
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
            deployments.put(group, message.body().copy().putString("id", result.result()).encode());
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
    Collection<String> deployments = this.deployments.get(group);
    String deployment = null;
    for (String sdeployment : deployments) {
      JsonObject info = new JsonObject(sdeployment);
      if (info.getString("id").equals(deploymentID)) {
        deployment = sdeployment;
        break;
      }
    }
    if (deployment != null) {
      this.deployments.remove(group, deployment);
    }
  }

}
