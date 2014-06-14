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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import net.kuujo.vertigo.Config;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.manager.NodeManager;
import net.kuujo.vertigo.impl.ContextBuilder;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;
import net.kuujo.vertigo.network.manager.NetworkManager;
import net.kuujo.vertigo.platform.ModuleInfo;
import net.kuujo.vertigo.platform.PlatformManager;
import net.kuujo.vertigo.util.Configs;
import net.kuujo.vertigo.util.ContextManager;
import net.kuujo.vertigo.util.serialization.SerializationException;
import net.kuujo.vertigo.util.serialization.Serializer;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.spi.Action;

import com.hazelcast.core.MultiMap;

/**
 * Defualt node manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultNodeManager implements NodeManager {
  private static final Serializer serializer = SerializerFactory.getSerializer(Config.class);
  private static final Logger log = LoggerFactory.getLogger(DefaultNodeManager.class);
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
  private final String node;
  private final String group;
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
  private final Map<String, String> managers = new HashMap<>();

  private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Received message %s", DefaultNodeManager.this, message.body().encode()));
      }

      String action = message.body().getString("action");
      if (action != null) {
        switch (action) {
          case "ping":
            doPing(message);
            break;
          case "installed":
            doInstalled(message);
            break;
          case "install":
            doInstall(message);
            break;
          case "uninstall":
            doUninstall(message);
            break;
          case "upload":
            doUpload(message);
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
      }
    }
  };

  public DefaultNodeManager(String node, String group, String cluster, Vertx vertx, ContextManager context, PlatformManager platform, ClusterListener listener, ClusterData data) {
    this.node = node;
    this.group = group;
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
  }

  @Override
  public String address() {
    return String.format("%s.%s.%s", cluster, group, node);
  }

  @Override
  public NodeManager start() {
    return start(null);
  }

  @Override
  public NodeManager start(final Handler<AsyncResult<Void>> doneHandler) {
    vertx.eventBus().registerHandler(node, messageHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          context.execute(new Action<Void>() {
            @Override
            public Void perform() {
              if (!nodes.containsEntry(listener.nodeId(), node)) {
                nodes.put(listener.nodeId(), node);
              }
              if (!groups.containsEntry(group, node)) {
                groups.put(group, node);
              }
              return null;
            }
          }, doneHandler);
        }
      }
    });
    return this;
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
        nodes.remove(listener.nodeId(), node);
        groups.remove(group, node);
        return null;
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        vertx.eventBus().unregisterHandler(node, messageHandler, doneHandler);
      }
    });
  }

  /**
   * Pings the node.
   */
  private void doPing(final Message<JsonObject> message) {
    message.reply(new JsonObject().putString("status", "pong").putString("result", "node"));
  }

  /**
   * Checks if a module is installed.
   */
  private void doInstalled(final Message<JsonObject> message) {
    String moduleName = message.body().getString("module");
    if (moduleName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module specified."));
      return;
    }

    platform.getModuleInfo(moduleName, new Handler<AsyncResult<ModuleInfo>>() {
      @Override
      public void handle(AsyncResult<ModuleInfo> result) {
        if (result.failed() || result.result() == null) {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", false));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", true));
        }
      }
    });
  }

  /**
   * Installs a module.
   */
  private void doInstall(final Message<JsonObject> message) {
    String moduleName = message.body().getString("module");
    if (moduleName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module specified."));
      return;
    }

    String uploadID = message.body().getString("upload");
    if (uploadID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No upload found."));
      return;
    }

    final File modRoot = new File(TEMP_DIR, "vertx-zip-mods");
    final File modZip = new File(modRoot, uploadID + ".zip");

    vertx.fileSystem().exists(modZip.getAbsolutePath(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else if (!result.result()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid upload."));
        } else {
          platform.installModule(modZip.getAbsolutePath(), new Handler<AsyncResult<Void>>() {
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
    });
  }

  /**
   * Uninstalls a module.
   */
  private void doUninstall(final Message<JsonObject> message) {
    String moduleName = message.body().getString("module");
    if (moduleName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module specified."));
      return;
    }

    platform.uninstallModule(moduleName, new Handler<AsyncResult<Void>>() {
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
   * Uploads a file.
   */
  private void doUpload(final Message<JsonObject> message) {
    final String id = UUID.randomUUID().toString();
    final File modRoot = new File(TEMP_DIR, "vertx-zip-mods");
    vertx.fileSystem().mkdir(modRoot.getAbsolutePath(), true, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to create upload file."));
        } else {
          File modZip = new File(modRoot, id + ".zip");
          modZip.deleteOnExit();
          vertx.fileSystem().open(modZip.getAbsolutePath(), new Handler<AsyncResult<AsyncFile>>() {
            @Override
            public void handle(AsyncResult<AsyncFile> result) {
              if (result.failed()) {
                message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to create upload file."));
              } else {
                handleUpload(result.result(), id, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to register upload handler."));
                    } else {
                      message.reply(new JsonObject().putString("status", "ok").putString("id", id));
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
  }

  /**
   * Handles a file upload.
   */
  private void handleUpload(AsyncFile file, String address, Handler<AsyncResult<Void>> doneHandler) {
    vertx.eventBus().registerHandler(address, handleUpload(file, address), doneHandler);
  }

  /**
   * Handles a file upload.
   */
  private Handler<Message<Buffer>> handleUpload(final AsyncFile file, final String address) {
    final AtomicLong position = new AtomicLong();
    return new Handler<Message<Buffer>>() {
      @Override
      public void handle(final Message<Buffer> message) {
        final Handler<Message<Buffer>> handler = this;
        final Buffer buffer = message.body();
        if (buffer.length() > 0) {
          file.write(buffer, position.get(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                file.close();
                message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                vertx.eventBus().unregisterHandler(address, handler);
              } else {
                position.addAndGet(buffer.length());
                message.reply(new JsonObject().putString("status", "ok"));
              }
            }
          });
        } else {
          file.flush(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                vertx.eventBus().unregisterHandler(address, handler);
              } else {
                file.close(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                      vertx.eventBus().unregisterHandler(address, handler);
                    } else {
                      vertx.eventBus().unregisterHandler(address, handler, new Handler<AsyncResult<Void>>() {
                        @Override
                        public void handle(AsyncResult<Void> result) {
                          message.reply(new JsonObject().putString("status", "ok"));
                        }
                      });
                    }
                  }
                });
              }
            }
          });
        }
      }
    };
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
              deployments.put(node, message.body().copy().putString("id", deploymentID).encode());
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
                deployments.put(node, message.body().copy().putString("id", deploymentID).encode());
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
                deployments.put(node, message.body().copy().putString("id", deploymentID).encode());
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
      if (network instanceof String) {
        doDeployNetwork((String) network, message);
      } else if (network instanceof JsonObject) {
        JsonObject jsonNetwork = (JsonObject) network;
        try {
          NetworkConfig config = serializer.deserializeObject(jsonNetwork, NetworkConfig.class);
          doDeployNetwork(config, message);
        } catch (SerializationException e) {
          message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
        }
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid network configuration."));
      }
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No network specified."));
    }
  }

  /**
   * Deploys a network from name.
   */
  private void doDeployNetwork(final String name, final Message<JsonObject> message) {
    // When deploying a bare network, we first attempt to load any existing
    // configuration from the cluster. This ensures that we don't overwrite
    // a cluster configuration. In some cases, the manager can be redeployed
    // by deploying a network name.
    String scontext = data.<String, String>getMap(String.format("%s.%s", cluster, name)).get(String.format("%s.%s", cluster, name));
    final NetworkContext context = scontext != null ? DefaultNetworkContext.fromJson(new JsonObject(scontext)) : ContextBuilder.buildContext(new DefaultNetworkConfig(name), cluster);

    // Simply deploy an empty network.
    if (!managers.containsKey(context.address())) {
      // If the network manager hasn't yet been deployed then deploy the manager
      // and then update the network's configuration.
      platform.deployVerticle(NetworkManager.class.getName(), new JsonObject().putString("cluster", cluster).putString("address", context.address()), 1, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to deploy network manager."));
          } else {
            // Once the manager has been deployed we can add the network name to
            // the set of networks that are deployed in the cluster.
            final String deploymentID = result.result();
            DefaultNodeManager.this.context.execute(new Action<Void>() {
              @Override
              public Void perform() {
                networks.add(context.name());
                return null;
              }
            }, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                // And store the manager's deployment ID in the local managers map.
                managers.put(context.address(), deploymentID);
                doDeployNetwork(context, message);
              }
            });
          }
        }
      });
    } else {
      message.reply(new JsonObject().putString("status", "ok").putObject("context", DefaultNetworkContext.toJson(context)));
    }
  }

  /**
   * Deploys a network from configuration.
   */
  private void doDeployNetwork(final NetworkConfig network, final Message<JsonObject> message) {
    // When deploying the network, we first need to check the cluster
    // to determine whether a network of that name is already deployed in the Vert.x
    // cluster. If the network is already deployed, then we merge the given network
    // configuration with the existing network configuration. If the network seems to
    // be new, then we deploy a new network manager on the network's cluster.
    String scontext = data.<String, String>getMap(String.format("%s.%s", cluster, network.getName())).get(String.format("%s.%s", cluster, network.getName()));

    // Create the new network context. If a context already existed in the cluster
    // then merge the new configuration with the existing configuration. Otherwise
    // just build a network context.
    NetworkContext updatedContext;
    if (scontext != null) {
      updatedContext = ContextBuilder.buildContext(Configs.mergeNetworks(DefaultNetworkContext.fromJson(new JsonObject(scontext)).config(), network), cluster);
    } else {
      updatedContext = ContextBuilder.buildContext(network, cluster);
    }

    final NetworkContext context = updatedContext;

    // If the network's manager is deployed then its deployment ID will have
    // been stored in the local managers map.
    if (!managers.containsKey(context.address())) {
      // If the network manager hasn't yet been deployed then deploy the manager
      // and then update the network's configuration.
      platform.deployVerticle(NetworkManager.class.getName(), new JsonObject().putString("cluster", cluster).putString("address", context.address()), 1, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to deploy network manager."));
          } else {
            // Once the manager has been deployed we can add the network name to
            // the set of networks that are deployed in the cluster.
            final String deploymentID = result.result();
            DefaultNodeManager.this.context.execute(new Action<Void>() {
              @Override
              public Void perform() {
                networks.add(context.name());
                return null;
              }
            }, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                // And store the manager's deployment ID in the local managers map.
                managers.put(context.address(), deploymentID);
                doDeployNetwork(context, message);
              }
            });
          }
        }
      });
    } else {
      // If the network manager is already deployed in the network's cluster then
      // simply merge and update the network's configuration.
      doDeployNetwork(context, message);
    }
  }

  /**
   * Deploys a network from context.
   */
  private void doDeployNetwork(final NetworkContext context, final Message<JsonObject> message) {
    final WrappedWatchableMap<String, String> data = new WrappedWatchableMap<String, String>(context.address(), this.data.<String, String>getMap(context.address()), vertx);

    // When the context is set in the cluster, the network's manager will be notified
    // via a cluster event. The manager will then unset the network's status key and
    // update the network. Once the network has been updated (components are deployed
    // and undeployed and connections are created or removed as necessary) the manager
    // will reset the network's status key to the updated version. We can use this fact
    // to determine when the configuration change is complete by watching the network's
    // status key for the new context version.
    data.watch(context.status(), null, new Handler<MapEvent<String, String>>() {
      @Override
      public void handle(MapEvent<String, String> event) {
        // Once the network has been updated we can stop watching the network's status key.
        // Once the status key is unwatched trigger the async handler indicating that the
        // update is complete.
        if (event.type().equals(MapEvent.Type.CREATE) && event.value().equals(context.version())) {
          data.unwatch(context.status(), null, this, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              message.reply(new JsonObject().putString("status", "ok").putObject("context", DefaultNetworkContext.toJson(context)));
            }
          });
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        // Once the network's status key is being watched, set the new configuration in
        // the cluster. This change will be recognized by the network's manager which will
        // then update the running network's configuration.
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          try {
            data.put(context.address(), DefaultNetworkContext.toJson(context).encode());
          } catch (Exception e) {
            message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
          }
        }
      }
    });
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
        Collection<String> nodeDeployments = deployments.get(node);
        if (nodeDeployments != null) {
          String deployment = null;
          for (String sdeployment : nodeDeployments) {
            JsonObject info = new JsonObject(sdeployment);
            if (info.getString("id").equals(deploymentID)) {
              deployment = sdeployment;
              break;
            }
          }
          if (deployment != null) {
            deployments.remove(node, deployment);
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
      if (network instanceof String) {
        doUndeployNetwork((String) network, message);
      } else if (network instanceof JsonObject) {
        JsonObject jsonNetwork = (JsonObject) network;
        try {
          NetworkConfig config = serializer.deserializeObject(jsonNetwork, NetworkConfig.class);
          doUndeployNetwork(config, message);
        } catch (SerializationException e) {
          message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
        }
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid network configuration."));
      }
    } else {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No network specified."));
    }
  }

  /**
   * Undeploys a network by name.
   */
  private void doUndeployNetwork(final String name, final Message<JsonObject> message) {
    // First we need to load the network's context from the cluster.
    final WrappedWatchableMap<String, String> data = new WrappedWatchableMap<String, String>(String.format("%s.%s", cluster, name), this.data.<String, String>getMap(String.format("%s.%s", cluster, name)), vertx);
    String scontext = data.get(String.format("%s.%s", cluster, name));
    if (scontext == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Network is not deployed."));
    } else {
      // If the network's manager is deployed locally then its deployment ID
      // will have been stored in the local managers map.
      final NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(scontext));
      if (managers.containsKey(context.address())) {
        // Now that we have the network's context we need to watch the network's
        // status key before we begin undeploying the network. Once we unset the
        // network's configuration key the network's manager will automatically
        // begin undeploying the network.
        data.watch(context.status(), MapEvent.Type.CHANGE, new Handler<MapEvent<String, String>>() {
          @Override
          public void handle(MapEvent<String, String> event) {
            // When the network's status key is set to an empty string, that indicates
            // that the network's manager has completely undeployed all components and
            // connections within the network. The manager is the only element of the
            // network left running, so we can go ahead and undeploy it.
            if (event.value() != null && event.value().equals("")) {
              // First, stop watching the status key so this handler doesn't accidentally
              // get called again for some reason.
              data.unwatch(context.status(), MapEvent.Type.CHANGE, this, new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> result) {
                  // Now undeploy the manager from the context cluster. Once the manager
                  // has been undeployed the undeployment of the network is complete.
                  platform.undeployVerticle(managers.remove(context.address()), new Handler<AsyncResult<Void>>() {
                    @Override
                    public void handle(AsyncResult<Void> result) {
                      if (result.failed()) {
                        message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                      } else {
                        // We can be nice and unset the status key since the manager was undeployed :-)
                        DefaultNodeManager.this.context.execute(new Action<Void>() {
                          @Override
                          public Void perform() {
                            networks.remove(context.address());
                            data.remove(context.status());
                            return null;
                          }
                        }, new Handler<AsyncResult<Void>>() {
                          @Override
                          public void handle(AsyncResult<Void> result) {
                            message.reply(new JsonObject().putString("status", "ok"));
                          }
                        });
                      }
                    }
                  });
                }
              });
            }
          }
        }, new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            // Once we've started watching the status key, unset the network's context in
            // the cluster. The network's manager will be notified of the configuration
            // change and will begin undeploying all of its components. Once the components
            // have been undeployed it will reset its status key.
            if (result.failed()) {
              message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
            } else {
              DefaultNodeManager.this.context.run(new Runnable() {
                @Override
                public void run() {
                  data.remove(context.address());
                }
              });
            }
          }
        });
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Network is not deployed."));
      }
    }
  }

  /**
   * Undeploys a network by configuration.
   */
  private void doUndeployNetwork(final NetworkConfig network, final Message<JsonObject> message) {
    // First we need to load the network's context from the cluster.
    final WrappedWatchableMap<String, String> data = new WrappedWatchableMap<String, String>(String.format("%s.%s", cluster, network.getName()), this.data.<String, String>getMap(String.format("%s.%s", cluster, network.getName())), vertx);
    String scontext = data.get(String.format("%s.%s", cluster, network.getName()));
    if (scontext == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Network is not deployed."));
    } else {
      // Determine whether the network's manager is deployed.
      final NetworkContext tempContext = DefaultNetworkContext.fromJson(new JsonObject(scontext));
      if (managers.containsKey(tempContext.address())) {
        // If the network's manager is deployed then unmerge the given
        // configuration from the configuration of the network that's
        // already running. If the resulting configuration is a no-component
        // network then that indicates that the entire network is being undeployed.
        // Otherwise, we simply update the existing configuration and allow the
        // network's manager to handle deployment and undeployment of components.
        NetworkConfig updatedConfig = Configs.unmergeNetworks(tempContext.config(), network);
        final NetworkContext context = ContextBuilder.buildContext(updatedConfig, cluster);

        // If the new configuration has no components then undeploy the entire network.
        if (context.components().isEmpty()) {
          // We need to watch the network's status key to determine when the network's
          // components have been completely undeployed. Once that happens it's okay
          // to then undeploy the network's manager.
          data.watch(context.status(), MapEvent.Type.CHANGE, new Handler<MapEvent<String, String>>() {
            @Override
            public void handle(MapEvent<String, String> event) {
              // When the network's status key is set to an empty string, that indicates
              // that the network's manager has completely undeployed all components and
              // connections within the network. The manager is the only element of the
              // network left running, so we can go ahead and undeploy it.
              if (event.value() != null && event.value().equals("")) {
                // First, stop watching the status key so this handler doesn't accidentally
                // get called again for some reason.
                data.unwatch(context.status(), MapEvent.Type.CHANGE, this, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    // Now undeploy the manager from the context cluster. Once the manager
                    // has been undeployed the undeployment of the network is complete.
                    platform.undeployVerticle(managers.remove(context.address()), new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        if (result.failed()) {
                          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                        } else {
                          // We can be nice and unset the status key since the manager was undeployed :-)
                          DefaultNodeManager.this.context.execute(new Action<Void>() {
                            @Override
                            public Void perform() {
                              networks.remove(context.address());
                              data.remove(context.status());
                              return null;
                            }
                          }, new Handler<AsyncResult<Void>>() {
                            @Override
                            public void handle(AsyncResult<Void> result) {
                              message.reply(new JsonObject().putString("status", "ok"));
                            }
                          });
                        }
                      }
                    });
                  }
                });
              }
            }
          }, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              // Once we've started watching the status key, unset the network's context in
              // the cluster. The network's manager will be notified of the configuration
              // change and will begin undeploying all of its components. Once the components
              // have been undeployed it will reset its status key.
              if (result.failed()) {
                message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
              } else {
                DefaultNodeManager.this.context.run(new Runnable() {
                  @Override
                  public void run() {
                    data.remove(context.address());
                  }
                });
              }
            }
          });
        } else {
          // If only a portion of the running network is being undeployed, we need
          // to watch the network's status key for notification once the configuration
          // change is complete.
          data.watch(context.status(), MapEvent.Type.CHANGE, new Handler<MapEvent<String, String>>() {
            @Override
            public void handle(MapEvent<String, String> event) {
              if (event.value() != null && event.value().equals(context.version())) {
                // The network's configuration has been completely updated. Stop watching
                // the network's status key and call the async handler.
                data.unwatch(context.status(), null, this, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    message.reply(new JsonObject().putString("status", "ok").putObject("context", DefaultNetworkContext.toJson(context)));
                  }
                });
              }
            }
          }, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              // Once we've started watching the status key, update the network's context in
              // the cluster. The network's manager will be notified of the configuration
              // change and will begin deploying or undeploying components and connections
              // as necessary.
              if (result.failed()) {
                message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
              } else {
                DefaultNodeManager.this.context.run(new Runnable() {
                  @Override
                  public void run() {
                    data.put(context.address(), DefaultNetworkContext.toJson(context).encode());
                  }
                });
              }
            }
          });
        }
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Network is not deployed."));
      }
    }
  }

  @Override
  public String toString() {
    return String.format("NodeManager[%s]", node);
  }

}
