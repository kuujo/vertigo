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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.Config;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.ClusterException;
import net.kuujo.vertigo.cluster.Group;
import net.kuujo.vertigo.cluster.Node;
import net.kuujo.vertigo.cluster.data.AsyncCounter;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.AsyncMultiMap;
import net.kuujo.vertigo.cluster.data.AsyncQueue;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncCounter;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncList;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncMultiMap;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncQueue;
import net.kuujo.vertigo.cluster.data.impl.DefaultAsyncSet;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.component.ModuleConfig;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultActiveNetwork;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;
import net.kuujo.vertigo.util.Configs;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Default cluster client implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultCluster implements Cluster {
  private static final long DEFAULT_REPLY_TIMEOUT = 30000;
  private final String address;
  private final Vertx vertx;
  private final Container container;
  private final Map<Handler<Node>, Handler<Message<String>>> joinHandlers = new HashMap<>();
  private final Map<Handler<Node>, Handler<Message<String>>> leaveHandlers = new HashMap<>();

  public DefaultCluster(String address, Vertx vertx, Container container) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public Cluster registerJoinHandler(final Handler<Node> handler) {
    return registerJoinHandler(handler, null);
  }

  @Override
  public Cluster registerJoinHandler(final Handler<Node> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<Message<String>> messageHandler = new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        handler.handle(new DefaultNode(message.body(), vertx, container));
      }
    };
    joinHandlers.put(handler, messageHandler);
    vertx.eventBus().registerHandler(String.format("%s.join", address), messageHandler, doneHandler);
    return this;
  }

  @Override
  public Cluster unregisterJoinHandler(final Handler<Node> handler) {
    return unregisterJoinHandler(handler, null);
  }

  @Override
  public Cluster unregisterJoinHandler(final Handler<Node> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<Message<String>> messageHandler = joinHandlers.remove(handler);
    if (messageHandler != null) {
      vertx.eventBus().unregisterHandler(String.format("%s.join", address), messageHandler, doneHandler);
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public Cluster registerLeaveHandler(final Handler<Node> handler) {
    return registerLeaveHandler(handler, null);
  }

  @Override
  public Cluster registerLeaveHandler(final Handler<Node> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<Message<String>> messageHandler = new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {
        handler.handle(new DefaultNode(message.body(), vertx, container));
      }
    };
    leaveHandlers.put(handler, messageHandler);
    vertx.eventBus().registerHandler(String.format("%s.leave", address), messageHandler, doneHandler);
    return this;
  }

  @Override
  public Cluster unregisterLeaveHandler(final Handler<Node> handler) {
    return unregisterLeaveHandler(handler, null);
  }

  @Override
  public Cluster unregisterLeaveHandler(final Handler<Node> handler, final Handler<AsyncResult<Void>> doneHandler) {
    Handler<Message<String>> messageHandler = leaveHandlers.remove(handler);
    if (messageHandler != null) {
      vertx.eventBus().unregisterHandler(String.format("%s.leave", address), messageHandler, doneHandler);
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public Cluster ping(final Handler<AsyncResult<Cluster>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "ping");
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Cluster>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Cluster>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("pong")) {
          if (result.result().body().getString("result").equals("cluster")) {
            new DefaultFutureResult<Cluster>(DefaultCluster.this).setHandler(resultHandler);
          } else {
            new DefaultFutureResult<Cluster>(new ClusterException("Not a valid cluster address.")).setHandler(resultHandler);
          }
        }
      }
    });
    return this;
  }

  @Override
  public Cluster getGroup(final String group, final Handler<AsyncResult<Group>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "find")
        .putString("type", "group")
        .putString("group", group);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Group>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Group>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Group>(new DefaultGroup(result.result().body().getString("result"), vertx, container)).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster getGroups(final Handler<AsyncResult<Collection<Group>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "list")
        .putString("type", "group");
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<Group>>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<Group>>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          JsonArray jsonGroups = result.result().body().getArray("result");
          List<Group> groups = new ArrayList<>();
          for (Object jsonGroup : jsonGroups) {
            groups.add(new DefaultGroup((String) jsonGroup, vertx, container));
          }
          new DefaultFutureResult<Collection<Group>>(groups).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster selectGroup(Object key, final Handler<AsyncResult<Group>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "select")
        .putString("type", "group")
        .putValue("key", key);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Group>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Group>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Group>(new DefaultGroup(result.result().body().getString("result"), vertx, container)).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster getNode(String node, final Handler<AsyncResult<Node>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "find")
        .putString("type", "node")
        .putString("node", node);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Node>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Node>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Node>(new DefaultNode(result.result().body().getString("result"), vertx, container)).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster getNodes(final Handler<AsyncResult<Collection<Node>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "list")
        .putString("type", "node");
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<Node>>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<Node>>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          JsonArray jsonNodes = result.result().body().getArray("result");
          List<Node> nodes = new ArrayList<>();
          for (Object jsonNode : jsonNodes) {
            nodes.add(new DefaultNode((String) jsonNode, vertx, container));
          }
          new DefaultFutureResult<Collection<Node>>(nodes).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster selectNode(Object key, final Handler<AsyncResult<Node>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "select")
        .putString("type", "node")
        .putValue("key", key);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Node>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Node>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Node>(new DefaultNode(result.result().body().getString("result"), vertx, container)).setHandler(resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster installModule(String moduleName) {
    return installModule(moduleName, null);
  }

  @Override
  public Cluster installModule(final String moduleName, final Handler<AsyncResult<Void>> doneHandler) {
    getNodes(new Handler<AsyncResult<Collection<Node>>>() {
      @Override
      public void handle(AsyncResult<Collection<Node>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(result.result().size()).setHandler(doneHandler);
          for (Node node : result.result()) {
            node.installModule(moduleName, counter);
          }
        }
      }
    });
    return this;
  }

  @Override
  public Cluster uninstallModule(String moduleName) {
    return uninstallModule(moduleName, null);
  }

  @Override
  public Cluster uninstallModule(final String moduleName, final Handler<AsyncResult<Void>> doneHandler) {
    getNodes(new Handler<AsyncResult<Collection<Node>>>() {
      @Override
      public void handle(AsyncResult<Collection<Node>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(result.result().size()).setHandler(doneHandler);
          for (Node node : result.result()) {
            node.uninstallModule(moduleName, counter);
          }
        }
      }
    });
    return this;
  }

  @Override
  public Cluster deployModule(String moduleName) {
    return deployModule(moduleName, null, 1, null);
  }

  @Override
  public Cluster deployModule(String moduleName, JsonObject config) {
    return deployModule(moduleName, config, 1, null);
  }

  @Override
  public Cluster deployModule(String moduleName, int instances) {
    return deployModule(moduleName, null, instances, null);
  }

  @Override
  public Cluster deployModule(String moduleName, JsonObject config, int instances) {
    return deployModule(moduleName, config, instances, null);
  }

  @Override
  public Cluster deployModule(String moduleName, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(moduleName, null, 1, doneHandler);
  }

  @Override
  public Cluster deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(moduleName, config, 1, doneHandler);
  }

  @Override
  public Cluster deployModule(String moduleName, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployModule(moduleName, null, instances, doneHandler);
  }

  @Override
  public Cluster deployModule(String moduleName, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("type", "module")
        .putString("module", moduleName)
        .putObject("config", config != null ? config : new JsonObject())
        .putNumber("instances", instances);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<String>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster deployVerticle(String main) {
    return deployVerticle(main, null, 1, null);
  }

  @Override
  public Cluster deployVerticle(String main, JsonObject config) {
    return deployVerticle(main, config, 1, null);
  }

  @Override
  public Cluster deployVerticle(String main, int instances) {
    return deployVerticle(main, null, instances, null);
  }

  @Override
  public Cluster deployVerticle(String main, JsonObject config, int instances) {
    return deployVerticle(main, config, instances, null);
  }

  @Override
  public Cluster deployVerticle(String main, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(main, null, 1, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(main, config, 1, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployVerticle(main, null, instances, doneHandler);
  }

  @Override
  public Cluster deployVerticle(String main, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config != null ? config : new JsonObject())
        .putNumber("instances", instances);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<String>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster deployWorkerVerticle(String main) {
    return deployWorkerVerticle(main, null, 1, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String main, JsonObject config) {
    return deployWorkerVerticle(main, config, 1, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String main, int instances) {
    return deployWorkerVerticle(main, null, instances, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded) {
    return deployWorkerVerticle(main, config, instances, false, null);
  }

  @Override
  public Cluster deployWorkerVerticle(String main, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(main, null, 1, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(main, config, 1, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String main, int instances, Handler<AsyncResult<String>> doneHandler) {
    return deployWorkerVerticle(main, null, instances, false, doneHandler);
  }

  @Override
  public Cluster deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, final Handler<AsyncResult<String>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "deploy")
        .putString("type", "verticle")
        .putString("main", main)
        .putObject("config", config != null ? config : new JsonObject())
        .putNumber("instances", instances)
        .putBoolean("worker", true)
        .putBoolean("multi-threaded", multiThreaded);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<String>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<String>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<String>(result.result().body().getString("id")).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster undeployModule(String deploymentID) {
    return undeployModule(deploymentID, null);
  }

  @Override
  public Cluster undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("type", "module")
        .putString("id", deploymentID);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster undeployVerticle(String deploymentID) {
    return undeployVerticle(deploymentID, null);
  }

  @Override
  public Cluster undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("type", "verticle")
        .putString("id", deploymentID);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("ok")) {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster getNetworks(final Handler<AsyncResult<Collection<ActiveNetwork>>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "list")
        .putString("type", "network");
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Collection<ActiveNetwork>>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Collection<ActiveNetwork>>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          final List<ActiveNetwork> networks = new ArrayList<>();
          JsonArray jsonNetworks = result.result().body().getArray("result");
          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(jsonNetworks.size());
          counter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<Collection<ActiveNetwork>>(result.cause()).setHandler(resultHandler);
              } else {
                new DefaultFutureResult<Collection<ActiveNetwork>>(networks).setHandler(resultHandler);
              }
            }
          });
          for (Object jsonNetwork : jsonNetworks) {
            createActiveNetwork(DefaultNetworkContext.fromJson((JsonObject) jsonNetwork), new Handler<AsyncResult<ActiveNetwork>>() {
              @Override
              public void handle(AsyncResult<ActiveNetwork> result) {
                if (result.failed()) {
                  counter.fail(result.cause());
                } else {
                  networks.add(result.result());
                  counter.succeed();
                }
              }
            });
          }
        }
      }
    });
    return this;
  }

  @Override
  public Cluster getNetwork(String name, final Handler<AsyncResult<ActiveNetwork>> resultHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "find")
        .putString("type", "network")
        .putString("network", name);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.result().body().getString("message"))).setHandler(resultHandler);
        } else {
          createActiveNetwork(DefaultNetworkContext.fromJson(result.result().body().getObject("result")), resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster deployNetwork(String name) {
    return deployNetwork(name, null);
  }

  @Override
  public Cluster deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(new DefaultNetworkConfig(name), doneHandler);
  }

  @Override
  public Cluster deployNetwork(JsonObject network) {
    return deployNetwork(network, null);
  }

  @Override
  public Cluster deployNetwork(JsonObject network, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(Configs.createNetwork(network), doneHandler);
  }

  @Override
  public Cluster deployNetwork(NetworkConfig network) {
    return deployNetwork(network, null);
  }

  @Override
  public Cluster deployNetwork(final NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // When deploying a network, we first need to select the node to which
    // the network belongs. Each network will have a randomly selected master.
    // In order for the network to be able to distribute components across the
    // cluster it needs to have all the installables locally installed.
    selectNode(network.getName(), new Handler<AsyncResult<Node>>() {
      @Override
      public void handle(AsyncResult<Node> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          // Once we've selected a node we need to install all the modules to the node.
          final Node node = result.result();
          List<ModuleConfig> modules = new ArrayList<>();
          for (ComponentConfig<?> component : network.getComponents()) {
            if (component.getType().equals(ComponentConfig.Type.MODULE)) {
              modules.add((ModuleConfig) component);
            }
          }

          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(modules.size());
          counter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
              } else {
                // Once all the modules have been installed we can deploy the network.
                JsonObject message = new JsonObject()
                    .putString("action", "deploy")
                    .putString("type", "network")
                    .putObject("network", SerializerFactory.getSerializer(Config.class).serializeToObject(network));
                vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
                  @Override
                  public void handle(AsyncResult<Message<JsonObject>> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(doneHandler);
                    } else if (result.result().body().getString("status").equals("error")) {
                      new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
                    } else {
                      createActiveNetwork(DefaultNetworkContext.fromJson(result.result().body().getObject("context")), doneHandler);
                    }
                  }
                });
              }
            }
          });

          for (ModuleConfig module : modules) {
            node.installModule(module.getModule(), counter);
          }
        }
      }
    });
    return this;
  }

  @Override
  public Cluster undeployNetwork(String name) {
    return undeployNetwork(name, null);
  }

  @Override
  public Cluster undeployNetwork(String name, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("type", "network")
        .putString("network", name);
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public Cluster undeployNetwork(JsonObject network) {
    return undeployNetwork(network, null);
  }

  @Override
  public Cluster undeployNetwork(JsonObject network, Handler<AsyncResult<Void>> doneHandler) {
    return undeployNetwork(Configs.createNetwork(network), doneHandler);
  }

  @Override
  public Cluster undeployNetwork(NetworkConfig network) {
    return undeployNetwork(network, null);
  }

  @Override
  public Cluster undeployNetwork(NetworkConfig network, final Handler<AsyncResult<Void>> doneHandler) {
    JsonObject message = new JsonObject()
        .putString("action", "undeploy")
        .putString("type", "network")
        .putObject("network", SerializerFactory.getSerializer(Config.class).serializeToObject(network));
    vertx.eventBus().sendWithTimeout(address, message, DEFAULT_REPLY_TIMEOUT, new Handler<AsyncResult<Message<JsonObject>>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result().body().getString("status").equals("error")) {
          new DefaultFutureResult<Void>(new ClusterException(result.result().body().getString("message"))).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Creates and returns an active network.
   */
  private void createActiveNetwork(final NetworkContext context, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    final DefaultActiveNetwork active = new DefaultActiveNetwork(context.config(), DefaultCluster.this);
    vertx.eventBus().registerHandler(String.format("%s.%s.change", context.name(), context.name()), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        String event = message.body().getString("type");
        if (event.equals("change") && message.body().getString("value") != null) {
          active.update(DefaultNetworkContext.fromJson(new JsonObject(message.body().getString("value"))));
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<ActiveNetwork>(active).setHandler(doneHandler);
        }
      }
    });
  }

  @Override
  public <K, V> AsyncMap<K, V> getMap(String name) {
    return new DefaultAsyncMap<K, V>(address, name, vertx);
  }

  @Override
  public <K, V> AsyncMultiMap<K, V> getMultiMap(String name) {
    return new DefaultAsyncMultiMap<K, V>(address, name, vertx);
  }

  @Override
  public <T> AsyncSet<T> getSet(String name) {
    return new DefaultAsyncSet<T>(address, name, vertx);
  }

  @Override
  public <T> AsyncList<T> getList(String name) {
    return new DefaultAsyncList<T>(address, name, vertx);
  }

  @Override
  public <T> AsyncQueue<T> getQueue(String name) {
    return new DefaultAsyncQueue<T>(address, name, vertx);
  }

  @Override
  public AsyncCounter getCounter(String name) {
    return new DefaultAsyncCounter(address, name, vertx);
  }

  @Override
  public String toString() {
    return String.format("Cluster[%s]", address);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof Cluster && ((Cluster) object).address().equals(address);
  }

}
