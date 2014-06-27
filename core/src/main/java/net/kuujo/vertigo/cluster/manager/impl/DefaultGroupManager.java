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
package net.kuujo.vertigo.cluster.manager.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.kuujo.vertigo.cluster.manager.GroupManager;
import net.kuujo.vertigo.platform.PlatformManager;
import net.kuujo.vertigo.util.ContextManager;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.spi.Action;

import com.hazelcast.core.MultiMap;

/**
 * Default group manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultGroupManager implements GroupManager {
  private static final Logger log = LoggerFactory.getLogger(DefaultGroupManager.class);
  private final String group;
  private final String internal = UUID.randomUUID().toString();
  private final Vertx vertx;
  private final ContextManager context;
  private final PlatformManager platform;
  private final ClusterListener listener;
  private final MultiMap<String, String> groups;
  private final MultiMap<String, String> deployments;
  private final Map<Object, String> nodeSelectors;

  private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Received message %s", DefaultGroupManager.this, message.body().encode()));
      }

      String action = message.body().getString("action");
      if (action != null) {
        switch (action) {
          case "ping":
            doPing(message);
            break;
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

  private final Handler<Message<JsonObject>> internalHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String action = message.body().getString("action");
      if (action != null) {
        switch (action) {
          case "undeploy":
            doInternalUndeploy(message);
            break;
        }
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

  public DefaultGroupManager(String group, String cluster, Vertx vertx, ContextManager context, PlatformManager platform, ClusterListener listener, ClusterData data) {
    this.group = group;
    this.vertx = vertx;
    this.context = context;
    this.platform = platform;
    this.listener = listener;
    this.groups = data.getMultiMap(String.format("groups.%s", cluster));
    this.deployments = data.getMultiMap(String.format("deployments.%s", cluster));
    this.nodeSelectors = data.getMap(String.format("selectors.node.%s", group));
  }

  @Override
  public String address() {
    return group;
  }

  @Override
  public void start() {
    start(null);
  }

  @Override
  public void start(Handler<AsyncResult<Void>> doneHandler) {
    listener.registerJoinHandler(joinHandler);
    listener.registerLeaveHandler(leaveHandler);
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(2).setHandler(doneHandler);
    vertx.eventBus().registerHandler(internal, internalHandler, counter);
    vertx.eventBus().registerHandler(group, messageHandler, counter);
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public void stop(final Handler<AsyncResult<Void>> doneHandler) {
    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        groups.remove(group);
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        vertx.eventBus().unregisterHandler(group, messageHandler, doneHandler);
        listener.unregisterJoinHandler(null);
        listener.unregisterLeaveHandler(null);
        final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(2).setHandler(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            clearDeployments(doneHandler);
          }
        });
        vertx.eventBus().unregisterHandler(internal, internalHandler, counter);
        vertx.eventBus().unregisterHandler(group, messageHandler, counter);
      }
    });
  }

  /**
   * When the cluster is shutdown properly we need to remove deployments
   * from the deployments map in order to ensure that deployments aren't
   * redeployed if this node leaves the cluster.
   */
  private void clearDeployments(final Handler<AsyncResult<Void>> doneHandler) {
    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        Collection<String> sdeploymentsInfo = deployments.get(group);
        for (String sdeploymentInfo : sdeploymentsInfo) {
          JsonObject deploymentInfo = new JsonObject(sdeploymentInfo);
          if (deploymentInfo.getString("address").equals(internal)) {
            deployments.remove(group, sdeploymentInfo);
          }
        }
        return null;
      }
    }, doneHandler);
  }

  /**
   * Called when a node joins the cluster.
   */
  private void doNodeJoined(final String nodeID) {
    log.info(String.format("%s - %s joined the cluster", this, nodeID));
  }

  /**
   * Called when a node leaves the cluster.
   */
  private synchronized void doNodeLeft(final String nodeID) {
    log.info(String.format("%s - %s left the cluster", this, nodeID));
    context.run(new Runnable() {
      @Override
      public void run() {
        // Redeploy any failed deployments.
        synchronized (deployments) {
          Collection<String> sdeploymentsInfo = deployments.get(group);
          for (final String sdeploymentInfo : sdeploymentsInfo) {
            final JsonObject deploymentInfo = new JsonObject(sdeploymentInfo);
            // If the deployment node is equal to the node that left the cluster then
            // remove the deployment from the deployments list and attempt to redeploy it.
            if (deploymentInfo.getString("node").equals(nodeID)) {
              // If the deployment is an HA deployment then attempt to redeploy it on this node.
              if (deployments.remove(group, sdeploymentInfo) && deploymentInfo.getBoolean("ha", false)) {
                doRedeploy(deploymentInfo);
              }
            }
          }
        }
      }
    });
  }

  /**
   * Pings the group.
   */
  private void doPing(final Message<JsonObject> message) {
    message.reply(new JsonObject().putString("status", "pong").putString("result", "group"));
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

    final String address = String.format("%s.%s", group, nodeName);
    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return groups.containsEntry(group, address);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else if (!result.result()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid node."));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putString("result", address));
        }
      }
    });
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
    context.execute(new Action<Collection<String>>() {
      @Override
      public Collection<String> perform() {
        return groups.get(group);
      }
    }, new Handler<AsyncResult<Collection<String>>>() {
      @Override
      public void handle(AsyncResult<Collection<String>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else if (result.result() == null) {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new String[result.result().size()]))));
        }
      }
    });
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
    final Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
    } else {
      context.execute(new Action<String>() {
        @Override
        public String perform() {
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
      }, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else if (result.result() == null) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "No nodes to select."));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putString("result", result.result()));
          }
        }
      });
    }
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
    platform.deployModule(moduleName, config, instances, createDeploymentHandler(message));
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
      platform.deployWorkerVerticle(main, config, instances, multiThreaded, createDeploymentHandler(message));
    } else {
      platform.deployVerticle(main, config, instances, createDeploymentHandler(message));
    }
  }

  /**
   * Creates a platform deployment handler.
   */
  private Handler<AsyncResult<String>> createDeploymentHandler(final Message<JsonObject> message) {
    return new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          addNewDeployment(result.result(), message.body(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              message.reply(new JsonObject().putString("status", "ok").putString("id", result.result()));
            }
          });
        }
      }
    };
  }

  /**
   * Adds a deployment to the cluster deployments map.
   */
  private void addNewDeployment(final String deploymentID, final JsonObject deploymentInfo, Handler<AsyncResult<String>> doneHandler) {
    context.execute(new Action<String>() {
      @Override
      public String perform() {
        deployments.put(group, deploymentInfo.copy()
            .putString("id", deploymentID)
            .putString("realID", deploymentID)
            .putString("address", internal)
            .putString("node", listener.nodeId()).encode());
        return deploymentID;
      }
    }, doneHandler);
  }

  /**
   * Redeploys a deployment.
   */
  private void doRedeploy(final JsonObject deploymentInfo) {
    if (deploymentInfo.getString("type").equals("module")) {
      log.info(String.format("%s - redeploying module %s", DefaultGroupManager.this, deploymentInfo.getString("module")));
      final CountDownLatch latch = new CountDownLatch(1);
      platform.deployModule(deploymentInfo.getString("module"), deploymentInfo.getObject("config", new JsonObject()), deploymentInfo.getInteger("instances", 1), createRedeployHandler(deploymentInfo, latch));
      try {
        latch.await(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
      }
    } else if (deploymentInfo.getString("type").equals("verticle")) {
      log.info(String.format("%s - redeploying verticle %s", DefaultGroupManager.this, deploymentInfo.getString("main")));
      final CountDownLatch latch = new CountDownLatch(1);
      if (deploymentInfo.getBoolean("worker", false)) {
        platform.deployWorkerVerticle(deploymentInfo.getString("main"), deploymentInfo.getObject("config", new JsonObject()), deploymentInfo.getInteger("instances", 1), deploymentInfo.getBoolean("multi-threaded"), createRedeployHandler(deploymentInfo, latch));
      } else {
        platform.deployVerticle(deploymentInfo.getString("main"), deploymentInfo.getObject("config", new JsonObject()), deploymentInfo.getInteger("instances", 1), createRedeployHandler(deploymentInfo, latch));
      }
      try {
        latch.await(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Creates a redeploy handler.
   */
  private Handler<AsyncResult<String>> createRedeployHandler(final JsonObject deploymentInfo, final CountDownLatch latch) {
    return new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          log.error(result.cause());
          latch.countDown();
        } else {
          addMappedDeployment(result.result(), deploymentInfo, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              latch.countDown();
            }
          });
        }
      }
    };
  }

  /**
   * Adds a changed deployment to the deployments map.
   */
  private void addMappedDeployment(final String deploymentID, final JsonObject deploymentInfo, Handler<AsyncResult<String>> doneHandler) {
    context.execute(new Action<String>() {
      @Override
      public String perform() {
        deploymentInfo.putString("realID", deploymentID);
        deploymentInfo.putString("node", listener.nodeId());
        deploymentInfo.putString("address", internal);
        deployments.put(group, deploymentInfo.encode());
        return deploymentID;
      }
    }, doneHandler);
  }

  /**
   * Undeploys a module or verticle.
   */
  private void doUndeploy(final Message<JsonObject> message) {
    final String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      findDeploymentAddress(deploymentID, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else if (result.result() == null) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid deployment " + deploymentID));
          } else {
            vertx.eventBus().sendWithTimeout(result.result(), message.body(), 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
              @Override
              public void handle(AsyncResult<Message<JsonObject>> result) {
                if (result.failed()) {
                  message.fail(((ReplyException) result.cause()).failureCode(), result.cause().getMessage());
                } else {
                  message.reply(result.result().body());
                }
              }
            });
          }
        }
      });
    }
  }

  /**
   * Locates the internal address of the node on which a deployment is deployed.
   */
  private void findDeploymentAddress(final String deploymentID, Handler<AsyncResult<String>> resultHandler) {
    context.execute(new Action<String>() {
      @Override
      public String perform() {
        synchronized (deployments) {
          JsonObject locatedInfo = null;
          Collection<String> sdeploymentsInfo = deployments.get(group);
          for (String sdeploymentInfo : sdeploymentsInfo) {
            JsonObject deploymentInfo = new JsonObject(sdeploymentInfo);
            if (deploymentInfo.getString("id").equals(deploymentID)) {
              locatedInfo = deploymentInfo;
              break;
            }
          }
          if (locatedInfo != null) {
            return locatedInfo.getString("address");
          }
          return null;
        }
      }
    }, resultHandler);
  }

  /**
   * Internally undeploys a module/verticle.
   */
  private void doInternalUndeploy(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment type specified."));
    } else {
      switch (type) {
        case "module":
          doInternalUndeployModule(message);
          break;
        case "verticle":
          doInternalUndeployVerticle(message);
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
  private void doInternalUndeployModule(final Message<JsonObject> message) {
    final String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      removeDeployment(deploymentID, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          platform.undeployModule(result.succeeded() && result.result() != null ? result.result() : deploymentID, createUndeployHandler(message));
        }
      });
    }
  }

  /**
   * Undeploys a verticle.
   */
  private void doInternalUndeployVerticle(final Message<JsonObject> message) {
    final String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      removeDeployment(deploymentID, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          platform.undeployVerticle(result.succeeded() && result.result() != null ? result.result() : deploymentID, createUndeployHandler(message));
        }
      });
    }
  }

  /**
   * Creates a platform undeploy handler.
   */
  private Handler<AsyncResult<Void>> createUndeployHandler(final Message<JsonObject> message) {
    return new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok"));
        }
      }
    };
  }

  /**
   * Removes a deployment from the deployments map and returns the real deploymentID.
   */
  private void removeDeployment(final String deploymentID, Handler<AsyncResult<String>> doneHandler) {
    context.execute(new Action<String>() {
      @Override
      public String perform() {
        Collection<String> groupDeployments = deployments.get(group);
        if (groupDeployments != null) {
          String deployment = null;
          for (String sdeployment : groupDeployments) {
            JsonObject info = new JsonObject(sdeployment);
            if (info.getString("id").equals(deploymentID)) {
              deployment = sdeployment;
              break;
            }
          }
          if (deployment != null) {
            deployments.remove(group, deployment);
            return new JsonObject(deployment).getString("realID");
          }
        }
        return null;
      }
    }, doneHandler);
  }

  @Override
  public String toString() {
    return String.format("GroupManager[%s]", group);
  }

}
