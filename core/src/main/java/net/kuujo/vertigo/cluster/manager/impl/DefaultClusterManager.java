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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.kuujo.vertigo.Config;
import net.kuujo.vertigo.cluster.manager.ClusterManager;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;
import net.kuujo.vertigo.platform.PlatformManager;
import net.kuujo.vertigo.util.ContextManager;
import net.kuujo.vertigo.util.serialization.SerializationException;
import net.kuujo.vertigo.util.serialization.Serializer;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.spi.Action;

import com.hazelcast.core.MultiMap;

/**
 * Default cluster manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultClusterManager implements ClusterManager {
  private static final Serializer serializer = SerializerFactory.getSerializer(Config.class);
  private final String cluster;
  private final Vertx vertx;
  private final ContextManager context;
  private final PlatformManager platform;
  private final ClusterListener listener;
  private final ClusterData data;
  private final MultiMap<String, String> nodes;
  private final MultiMap<String, String> groups;
  private final MultiMap<String, String> deployments;
  private final Set<String> networks;
  private final Map<Object, String> groupSelectors;
  private final Map<Object, String> nodeSelectors;

  private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
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
                break;
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
                break;
              case "multimap":
                switch (action) {
                  case "put":
                    doMultiMapPut(message);
                    break;
                  case "get":
                    doMultiMapGet(message);
                    break;
                  case "remove":
                    doMultiMapRemove(message);
                    break;
                  case "contains":
                    doMultiMapContains(message);
                    break;
                  case "keys":
                    doMultiMapKeys(message);
                    break;
                  case "values":
                    doMultiMapValues(message);
                    break;
                  case "empty":
                    doMultiMapIsEmpty(message);
                    break;
                  case "clear":
                    doMultiMapClear(message);
                    break;
                  case "size":
                    doMultiMapSize(message);
                    break;
                }
                break;
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

  public DefaultClusterManager(String cluster, Vertx vertx, ContextManager context, PlatformManager platform, ClusterListener listener, ClusterData data) {
    this.cluster = cluster;
    this.vertx = vertx;
    this.context = context;
    this.platform = platform;
    this.listener = listener;
    this.data = data;
    this.nodes = data.getMultiMap(String.format("nodes.%s", cluster));
    this.groups = data.getMultiMap(String.format("groups.%s", cluster));
    this.deployments = data.getMultiMap(String.format("deployments.%s", cluster));
    this.networks = data.getSet(String.format("run.%s", cluster));
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
    context.run(new Runnable() {
      @Override
      public void run() {
        synchronized (nodes) {
          // If we were the first node to remove the nodes then we need
          // to inform listeners that the node left the cluster over the
          // event bus.
          Collection<String> removedNodes = nodes.remove(nodeID);
          if (removedNodes != null) {
            synchronized (groups) {
              for (final String node : removedNodes) {
                for (final String group : groups.keySet()) {
                  groups.remove(group, node);
                  vertx.runOnContext(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                      vertx.eventBus().publish(String.format("%s.leave", group), node);
                    }
                  });
                }
                vertx.runOnContext(new Handler<Void>() {
                  @Override
                  public void handle(Void event) {
                    vertx.eventBus().publish(String.format("%s.leave", cluster), node);
                  }
                });
              }
            }

            // Check for any network masters that left the cluster.
            synchronized (networks) {
              for (final String name : networks) {
                String address = nodeSelectors.get(name);
                if (address != null && removedNodes.contains(address)) {
                  // If the node to which the network was assigned is one of the nodes
                  // that left the cluster then redeploy the network. If no changes
                  // have been made to the network then this will simply result in the
                  // network's manager being redeployed.
                  selectNode(name, new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> result) {
                      if (result.succeeded() && result.result() != null) {
                        // Just redeploy the network by sending a 
                        JsonObject message = new JsonObject()
                            .putString("action", "deploy")
                            .putString("type", "network")
                            .putString("network", name);
                        vertx.eventBus().send(result.result(), message);
                      }
                    }
                  });
                }
              }
            }
          }
        }
      }
    });
  }

  /**
   * Pings the cluster.
   */
  private void doPing(final Message<JsonObject> message) {
    message.reply(new JsonObject().putString("status", "pong").putString("result", "cluster"));
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
        case "network":
          doFindNetwork(message);
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

    final String address = String.format("%s.%s", cluster, group);
    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return groups.containsKey(address);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else if (!result.result()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid group."));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putString("result", address));
        }
      }
    });
  }

  /**
   * Finds a node in the cluster.
   */
  private void doFindNode(final Message<JsonObject> message) {
    final String node = message.body().getString("node");
    if (node == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid node address."));
      return;
    }

    context.execute(new Action<String>() {
      @Override
      public String perform() {
        for (String group : groups.keySet()) {
          String address = String.format("%s.%s", group, node);
          if (groups.containsEntry(group, address)) {
            return address;
          }
        }
        return null;
      }
    }, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else if (result.result() == null) {
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid node."));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putString("result", result.result()));
        }
      }
    });
  }

  /**
   * Loads a network configuration.
   */
  private void doFindNetwork(final Message<JsonObject> message) {
    final String name = message.body().getString("network");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No network name specified."));
    } else {
      context.execute(new Action<NetworkContext>() {
        @Override
        public NetworkContext perform() {
          String scontext = data.<String, String>getMap(String.format("%s.%s", cluster, name)).get(String.format("%s.%s", cluster, name));
          if (scontext != null) {
            return DefaultNetworkContext.fromJson(new JsonObject(scontext));
          }
          return null;
        }
      }, new Handler<AsyncResult<NetworkContext>>() {
        @Override
        public void handle(AsyncResult<NetworkContext> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else if (result.result() == null) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "Not a valid network."));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putObject("result", DefaultNetworkContext.toJson(result.result())));
          }
        }
      });
    }
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
        case "network":
          doListNetwork(message);
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
    context.execute(new Action<Set<String>>() {
      @Override
      public Set<String> perform() {
        return groups.keySet();
      }
    }, new Handler<AsyncResult<Set<String>>>() {
      @Override
      public void handle(AsyncResult<Set<String>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new String[result.result().size()]))));
        }
      }
    });
  }

  /**
   * Lists nodes in the cluster.
   */
  private void doListNode(final Message<JsonObject> message) {
    context.execute(new Action<Collection<String>>() {
      @Override
      public Collection<String> perform() {
        List<String> nodes = new ArrayList<>();
        for (String group : groups.keySet()) {
          nodes.addAll(groups.get(group));
        }
        return nodes;
      }
    }, new Handler<AsyncResult<Collection<String>>>() {
      @Override
      public void handle(AsyncResult<Collection<String>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new String[result.result().size()]))));
        }
      }
    });
  }

  /**
   * Lists the networks running in the cluster.
   */
  private void doListNetwork(final Message<JsonObject> message) {
    context.execute(new Action<List<NetworkContext>>() {
      @Override
      public List<NetworkContext> perform() {
        List<NetworkContext> contexts = new ArrayList<>();
        for (String name : networks) {
          String scontext = data.<String, String>getMap(String.format("%s.%s", cluster, name)).get(String.format("%s.%s", cluster, name));
          if (scontext != null) {
            contexts.add(DefaultNetworkContext.fromJson(new JsonObject(scontext)));
          }
        }
        return contexts;
      }
    }, new Handler<AsyncResult<List<NetworkContext>>>() {
      @Override
      public void handle(AsyncResult<List<NetworkContext>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          JsonArray contexts = new JsonArray();
          for (NetworkContext context : result.result()) {
            contexts.addObject(DefaultNetworkContext.toJson(context));
          }
          message.reply(new JsonObject().putString("status", "ok").putArray("result", contexts));
        }
      }
    });
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
    final Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
    } else {
      selectGroup(key, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else if (result.result() == null) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "No groups to select."));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putString("result", result.result()));
          }
        }
      });
    }
  }

  /**
   * Selects a group in the cluster.
   */
  private void selectGroup(final Object key, final Handler<AsyncResult<String>> doneHandler) {
    context.execute(new Action<String>() {
      @Override
      public String perform() {
        String address = groupSelectors.get(key);
        if (address != null) {
          return address;
        }
        Set<String> groups = DefaultClusterManager.this.groups.keySet();
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
    }, doneHandler);
  }

  /**
   * Selects a node in the cluster.
   */
  private void doSelectNode(final Message<JsonObject> message) {
    final Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
    } else {
      selectNode(key, new Handler<AsyncResult<String>>() {
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
   * Selects a node.
   */
  private void selectNode(final Object key, final Handler<AsyncResult<String>> doneHandler) {
    context.execute(new Action<String>() {
      @Override
      public String perform() {
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
    }, doneHandler);
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
        case "network":
          doDeployNetwork(message);
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
          final String deploymentID = result.result();
          context.execute(new Action<String>() {
            @Override
            public String perform() {
              deployments.put(cluster, message.body().copy().putString("id", deploymentID).encode());
              return deploymentID;
            }
          }, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              message.reply(new JsonObject().putString("status", "ok").putString("id", deploymentID));
            }
          });
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
            final String deploymentID = result.result();
            context.execute(new Action<String>() {
              @Override
              public String perform() {
                deployments.put(cluster, message.body().copy().putString("id", deploymentID).encode());
                return deploymentID;
              }
            }, new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                message.reply(new JsonObject().putString("status", "ok").putString("id", deploymentID));
              }
            });
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
            final String deploymentID = result.result();
            context.execute(new Action<String>() {
              @Override
              public String perform() {
                deployments.put(cluster, message.body().copy().putString("id", deploymentID).encode());
                return deploymentID;
              }
            }, new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                message.reply(new JsonObject().putString("status", "ok").putString("id", deploymentID));
              }
            });
          }
        }
      });
    }
  }

  /**
   * Deploys a network.
   */
  private void doDeployNetwork(final Message<JsonObject> message) {
    Object network = message.body().getValue("network");
    if (network != null) {
      // When deploying a network to the cluster, we first determine
      // the node to which the network belongs by selecting a node.
      // The node that is selected for the network is always the node
      // to which components will be uploaded and from which components
      // will be deployed.
      String key;
      if (network instanceof String) {
        key = (String) network;
      } else if (network instanceof JsonObject) {
        try {
          key = serializer.deserializeObject((JsonObject) network, NetworkConfig.class).getName();
        } catch (SerializationException e) {
          message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
          return;
        }
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid network configuration."));
        return;
      }

      selectNode(key, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else if (result.result() == null) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "No nodes available."));
          } else {
            vertx.eventBus().sendWithTimeout(result.result(), message.body(), 120000, new Handler<AsyncResult<Message<JsonObject>>>() {
              @Override
              public void handle(AsyncResult<Message<JsonObject>> result) {
                if (result.failed()) {
                  message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to reach node."));
                } else {
                  message.reply(result.result().body());
                }
              }
            });
          }
        }
      });
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No network specified."));
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
        case "network":
          doUndeployNetwork(message);
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
    final String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      removeDeployment(deploymentID, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
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
      });
    }
  }

  /**
   * Undeploys a verticle.
   */
  private void doUndeployVerticle(final Message<JsonObject> message) {
    final String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      removeDeployment(deploymentID, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
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
      });
    }
  }

  /**
   * Removes a deployment from the deployments map.
   */
  private void removeDeployment(final String deploymentID, Handler<AsyncResult<Void>> doneHandler) {
    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        Collection<String> clusterDeployments = deployments.get(cluster);
        if (clusterDeployments != null) {
          String deployment = null;
          for (String sdeployment : clusterDeployments) {
            JsonObject info = new JsonObject(sdeployment);
            if (info.getString("id").equals(deploymentID)) {
              deployment = sdeployment;
              break;
            }
          }
          if (deployment != null) {
            deployments.remove(cluster, deployment);
          }
        }
        return null;
      }
    }, doneHandler);
  }

  /**
   * Undeploys a network.
   */
  private void doUndeployNetwork(final Message<JsonObject> message) {
    Object network = message.body().getValue("network");
    if (network != null) {
      // When undeploying a network from the cluster, we first determine
      // the node to which the network belongs by selecting a node.
      // The node that is selected for the network is always the node
      // that controls the network.
      String key;
      if (network instanceof String) {
        key = (String) network;
      } else if (network instanceof JsonObject) {
        try {
          key = serializer.deserializeObject((JsonObject) network, NetworkConfig.class).getName();
        } catch (SerializationException e) {
          message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
          return;
        }
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid network configuration."));
        return;
      }

      selectNode(key, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else if (result.result() == null) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "No nodes available."));
          } else {
            vertx.eventBus().sendWithTimeout(result.result(), message.body(), 120000, new Handler<AsyncResult<Message<JsonObject>>>() {
              @Override
              public void handle(AsyncResult<Message<JsonObject>> result) {
                if (result.failed()) {
                  message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to reach node."));
                } else {
                  message.reply(result.result().body());
                }
              }
            });
          }
        }
      });
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No network specified."));
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

    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        data.getMap(formatKey("keys")).put(key, value);
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
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

  /**
   * Handles getting a key.
   */
  private void doKeyGet(final Message<JsonObject> message) {
    final String key = message.body().getString("name");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getMap(formatKey("keys")).get(key);
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
        }
      }
    });
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

    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        data.getMap(formatKey("keys")).remove(key);
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
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

  /**
   * Handles getting a counter.
   */
  private void doCounterGet(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    context.execute(new Action<Long>() {
      @Override
      public Long perform() {
        Map<Object, Long> counters = data.getMap(formatKey("counters"));
        Long value = counters.get(name);
        if (value == null) {
          value = 0L;
        }
        return value;
      }
    }, new Handler<AsyncResult<Long>>() {
      @Override
      public void handle(AsyncResult<Long> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putNumber("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Long>() {
      @Override
      public Long perform() {
        Map<Object, Long> counters = data.getMap(formatKey("counters"));
        Long value = counters.get(name);
        if (value == null) {
          value = 0L;
        }
        value++;
        counters.put(name, value);
        return value;
      }
    }, new Handler<AsyncResult<Long>>() {
      @Override
      public void handle(AsyncResult<Long> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putNumber("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Long>() {
      @Override
      public Long perform() {
        Map<Object, Long> counters = data.getMap(formatKey("counters"));
        Long value = counters.get(name);
        if (value == null) {
          value = 0L;
        }
        value--;
        counters.put(name, value);
        return value;
      }
    }, new Handler<AsyncResult<Long>>() {
      @Override
      public void handle(AsyncResult<Long> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putNumber("result", result.result()));
        }
      }
    });
  }

  /**
   * Handles a cluster multi-map put command.
   */
  private void doMultiMapPut(final Message<JsonObject> message) {
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

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getMultiMap(formatKey(name)).put(key, value);
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
        }
      }
    });
  }

  /**
   * Handles a cluster multi-map get command.
   */
  private void doMultiMapGet(final Message<JsonObject> message) {
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

    context.execute(new Action<Collection<Object>>() {
      @Override
      public Collection<Object> perform() {
        return data.getMultiMap(formatKey(name)).get(key);
      }
    }, new Handler<AsyncResult<Collection<Object>>>() {
      @Override
      public void handle(AsyncResult<Collection<Object>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new Object[result.result().size()]))));
        }
      }
    });
  }

  /**
   * Handles a cluster multi-map remove command.
   */
  private void doMultiMapRemove(final Message<JsonObject> message) {
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
    if (value != null) {
      context.execute(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return data.getMultiMap(formatKey(name)).remove(key, value);
        }
      }, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
          }
        }
      });
    } else {
      context.execute(new Action<Collection<Object>>() {
        @Override
        public Collection<Object> perform() {
          return data.getMultiMap(formatKey(name)).remove(key);
        }
      }, new Handler<AsyncResult<Collection<Object>>>() {
        @Override
        public void handle(AsyncResult<Collection<Object>> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new Object[result.result().size()]))));
          }
        }
      });
    }
  }

  /**
   * Handles a cluster multi-map contains command.
   */
  private void doMultiMapContains(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object key = message.body().getValue("key");
    final Object value = message.body().getValue("value");

    if (key != null && value != null) {
      context.execute(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return data.getMultiMap(formatKey(name)).containsEntry(key, value);
        }
      }, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
          }
        }
      });
    } else if (key != null) {
      context.execute(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return data.getMultiMap(formatKey(name)).containsKey(key);
        }
      }, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
          }
        }
      });
    } else if (value != null) {
      context.execute(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return data.getMultiMap(formatKey(name)).containsValue(key);
        }
      }, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
          }
        }
      });
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key or value specified."));
    }
  }

  /**
   * Handles a cluster multi-map keys command.
   */
  private void doMultiMapKeys(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    context.execute(new Action<Set<Object>>() {
      @Override
      public Set<Object> perform() {
        return data.getMultiMap(formatKey(name)).keySet();
      }
    }, new Handler<AsyncResult<Set<Object>>>() {
      @Override
      public void handle(AsyncResult<Set<Object>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new Object[result.result().size()]))));
        }
      }
    });
  }

  /**
   * Handles a cluster multi-map values command.
   */
  private void doMultiMapValues(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    context.execute(new Action<Collection<Object>>() {
      @Override
      public Collection<Object> perform() {
        return data.getMultiMap(formatKey(name)).values();
      }
    }, new Handler<AsyncResult<Collection<Object>>>() {
      @Override
      public void handle(AsyncResult<Collection<Object>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new Object[result.result().size()]))));
        }
      }
    });
  }

  /**
   * Handles cluster multi-map is empty command.
   */
  private void doMultiMapIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getMultiMap(formatKey(name)).size() == 0;
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
  }

  /**
   * Counts the number of items in a multimap.
   */
  private void doMultiMapSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    context.execute(new Action<Integer>() {
      @Override
      public Integer perform() {
        return data.getMultiMap(formatKey(name)).size();
      }
    }, new Handler<AsyncResult<Integer>>() {
      @Override
      public void handle(AsyncResult<Integer> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putNumber("result", result.result()));
        }
      }
    });
  }

  /**
   * Clears all items in a multi-map.
   */
  private void doMultiMapClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        data.getMultiMap(formatKey(name)).clear();
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
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

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getMap(formatKey(name)).put(key, value);
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
        }
      }
    });
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

    final Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getMap(formatKey(name)).get(key);
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
        }
      }
    });
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

    final Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getMap(formatKey(name)).remove(key);
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
        }
      }
    });
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

    final Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getMap(formatKey(name)).containsKey(key);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Set<Object>>() {
      @Override
      public Set<Object> perform() {
        return data.getMap(formatKey(name)).keySet();
      }
    }, new Handler<AsyncResult<Set<Object>>>() {
      @Override
      public void handle(AsyncResult<Set<Object>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new Object[result.result().size()]))));
        }
      }
    });
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

    context.execute(new Action<Collection<Object>>() {
      @Override
      public Collection<Object> perform() {
        return data.getMap(formatKey(name)).values();
      }
    }, new Handler<AsyncResult<Collection<Object>>>() {
      @Override
      public void handle(AsyncResult<Collection<Object>> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.result().toArray(new Object[result.result().size()]))));
        }
      }
    });
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getMap(formatKey(name)).isEmpty();
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Integer>() {
      @Override
      public Integer perform() {
        return data.getMap(formatKey(name)).size();
      }
    }, new Handler<AsyncResult<Integer>>() {
      @Override
      public void handle(AsyncResult<Integer> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putNumber("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        data.getMap(formatKey(name)).clear();
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getList(formatKey(name)).add(value);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getList(formatKey(name)).get(index);
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
        }
      }
    });
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

      context.execute(new Action<Object>() {
        @Override
        public Object perform() {
          return data.getList(formatKey(name)).remove(index);
        }
      }, new Handler<AsyncResult<Object>>() {
        @Override
        public void handle(AsyncResult<Object> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
          }
        }
      });
    } else {
      final Object value = message.body().getValue("value");
      if (value == null) {
        message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      } else {
        context.execute(new Action<Boolean>() {
          @Override
          public Boolean perform() {
            return data.getList(formatKey(name)).remove(value);
          }
        }, new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            if (result.failed()) {
              message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
            } else {
              message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
            }
          }
        });
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getList(formatKey(name)).contains(value);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getList(formatKey(name)).isEmpty();
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Integer>() {
      @Override
      public Integer perform() {
        return data.getList(formatKey(name)).size();
      }
    }, new Handler<AsyncResult<Integer>>() {
      @Override
      public void handle(AsyncResult<Integer> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putNumber("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        data.getList(formatKey(name)).clear();
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getSet(formatKey(name)).add(value);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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
      context.execute(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return data.getSet(formatKey(name)).remove(value);
        }
      }, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
          }
        }
      });
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getSet(formatKey(name)).contains(value);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getSet(formatKey(name)).isEmpty();
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Integer>() {
      @Override
      public Integer perform() {
        return data.getSet(formatKey(name)).size();
      }
    }, new Handler<AsyncResult<Integer>>() {
      @Override
      public void handle(AsyncResult<Integer> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putNumber("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        data.getSet(formatKey(name)).clear();
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getQueue(formatKey(name)).add(value);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

      context.execute(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return data.getQueue(formatKey(name)).remove(value);
        }
      }, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
          }
        }
      });
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getQueue(formatKey(name)).contains(value);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getQueue(formatKey(name)).isEmpty();
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Integer>() {
      @Override
      public Integer perform() {
        return data.getQueue(formatKey(name)).size();
      }
    }, new Handler<AsyncResult<Integer>>() {
      @Override
      public void handle(AsyncResult<Integer> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putNumber("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Void>() {
      @Override
      public Void perform() {
        data.getQueue(formatKey(name)).clear();
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
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

    context.execute(new Action<Boolean>() {
      @Override
      public Boolean perform() {
        return data.getQueue(formatKey(name)).offer(value);
      }
    }, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getQueue(formatKey(name)).element();
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getQueue(formatKey(name)).poll();
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
        }
      }
    });
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

    context.execute(new Action<Object>() {
      @Override
      public Object perform() {
        return data.getQueue(formatKey(name)).peek();
      }
    }, new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putValue("result", result.result()));
        }
      }
    });
  }

}
