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

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.ClusterException;
import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.cluster.ClusterScope;
import net.kuujo.vertigo.cluster.DeploymentException;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.WrappedWatchableAsyncMap;
import net.kuujo.vertigo.impl.ContextBuilder;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultActiveNetwork;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;
import net.kuujo.vertigo.network.manager.NetworkManager;
import net.kuujo.vertigo.util.Configs;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Base class for cluster manager implementations.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
abstract class AbstractClusterManager implements ClusterManager {
  protected final Vertx vertx;
  protected final Container container;
  protected final EventBus eventBus;
  protected final Cluster cluster;

  protected AbstractClusterManager(Vertx vertx, Container container, Cluster cluster) {
    this.vertx = vertx;
    this.container = container;
    this.eventBus = vertx.eventBus();
    this.cluster = cluster;
  }

  /**
   * Creates a network-level cluster.<p>
   *
   * Vertigo always coordinates at the highest available level. That is, if the
   * current Vert.x instance is clustered then Vertigo coordinates using cluster-wide
   * shared data, otherwise it coordinates using Vert.x <code>SharedData</code>.<p>
   *
   * Network clusters are created by resolving the current coordination cluster
   * scope with the network's cluster scope. If the coordination cluster is a
   * <code>CLUSTER</code> cluster then the network's cluster scope can be either
   * <code>CLUSTER</code> or <code>LOCAL</code>, otherwise the network's cluster
   * is <code>LOCAL</code> regardless.
   */
  private Cluster createNetworkCluster(NetworkContext context) {
    if (context.cluster().scope().equals(ClusterScope.CLUSTER) && scope().equals(ClusterScope.CLUSTER)) {
      return new ClusterFactory(vertx, container).createCluster(context.cluster().address(), ClusterScope.CLUSTER);
    } else {
      return new ClusterFactory(vertx, container).createCluster(context.cluster().address(), ClusterScope.LOCAL);
    }
  }

  @Override
  public ClusterManager getNetwork(final String name, final Handler<AsyncResult<ActiveNetwork>> resultHandler) {
    // Load the network's context from the coordination cluster. If the network
    // is deployed in the current Vert.x cluster then its context will be available
    // in the Vertigo coordination (highest level) cluster.
    final WatchableAsyncMap<String, String> data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(name), vertx);
    data.get(name, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result() != null) {
          // If the context exists in the coordination cluster, load the network's
          // cluster and make sure the network is actually deployed.
          final NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
          final Cluster contextCluster = createNetworkCluster(context);

          // We check whether the network is deployed by querying the network's cluster
          // to determine whether the network's manager is deployed. The manager will
          // always be deployed in the *network's* cluster rather than the coordination cluster.
          contextCluster.isDeployed(context.name(), new Handler<AsyncResult<Boolean>>() {
            @Override
            public void handle(AsyncResult<Boolean> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(resultHandler);
              } else if (!result.result()) {
                // If the manager is not deployed then don't return the active network. However,
                // we don't remove the network's context from the cluster just in case the
                // network is currently being deployed. We don't want to interfere with deployment.
                new DefaultFutureResult<ActiveNetwork>(new DeploymentException("Network is not deployed.")).setHandler(resultHandler);
              } else {
                // Finally, if the network is loaded and deployed then subscribe to messages
                // from the context on behalf of the active network. Once the active network
                // has been subscribed to updates, the network has been loaded.
                final DefaultActiveNetwork active = new DefaultActiveNetwork(context.config(), AbstractClusterManager.this);
                // To create the active network we simply watch the network's context in the
                // coordination cluster and update the active network when it changes.
                data.watch(name, new Handler<MapEvent<String, String>>() {
                  @Override
                  public void handle(MapEvent<String, String> event) {
                    String scontext = event.value();
                    if (scontext != null && scontext.length() > 0) {
                      active.update(DefaultNetworkContext.fromJson(new JsonObject(scontext)));
                    }
                  }
                }, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(resultHandler);
                    } else {
                      new DefaultFutureResult<ActiveNetwork>(active).setHandler(resultHandler);
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
    return this;
  }

  @Override
  public ClusterManager isRunning(String name, final Handler<AsyncResult<Boolean>> resultHandler) {
    // To check if a network is running we first need to determine whether
    // the network's configuration is stored in the coordination cluster.
    final WatchableAsyncMap<String, String> data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(name), vertx);
    data.get(name, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<Boolean>(new ClusterException(result.cause())).setHandler(resultHandler);
        } else if (result.result() == null) {
          // If no context was stored in the coordination cluster then assume
          // that the network is not running since we can't coordinate without
          // a context.
          new DefaultFutureResult<Boolean>(false).setHandler(resultHandler);
        } else {
          // If the context exists in the coordination cluster then load the
          // context and the network's cluster and determine whether the network's
          // manager is deployed.
          final NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
          final Cluster contextCluster = createNetworkCluster(context);
          contextCluster.isDeployed(context.name(), resultHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ClusterManager deployNetwork(NetworkConfig network) {
    return deployNetwork(network, null);
  }

  @Override
  public ClusterManager deployNetwork(final NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // When deploying the network, we first need to check the *coordination* cluster
    // to determine whether a network of that name is already deployed in the Vert.x
    // cluster. If the network is already deployed, then we merge the given network
    // configuration with the existing network configuration. If the network seems to
    // be new, then we deploy a new network manager on the network's cluster.
    final WatchableAsyncMap<String, String> data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(network.getName()), vertx);
    data.get(network.getName(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else if (result.result() == null) {
          // If the context doesn't already exist, the network must not be deployed.
          // Deploy the network manager.
          doDeployNetwork(network, null, doneHandler);
        } else {
          // If the context already exists in the coordination cluster, load the context
          // and the network's cluster to check whether the network's manager is deployed.
          // If the manager isn't already deployed then deploy it.
          final NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
          final Cluster contextCluster = createNetworkCluster(context);
          contextCluster.isDeployed(context.name(), new Handler<AsyncResult<Boolean>>() {
            @Override
            public void handle(AsyncResult<Boolean> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(doneHandler);
              } else if (result.result()) {
                // If the network manager is already deployed in the network's cluster then
                // simply merge and update the network's configuration.
                doDeployNetwork(network, context, doneHandler);
              } else {
                // If the network manager hasn't yet been deployed then deploy the manager
                // and then update the network's configuration.
                contextCluster.deployVerticle(context.name(), NetworkManager.class.getName(), new JsonObject().putString("name", context.name()), 1, new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(doneHandler);
                    } else {
                      doDeployNetwork(network, context, doneHandler);
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
    return this;
  }

  /**
   * Handles deployment of a network.
   */
  private void doDeployNetwork(final NetworkConfig network, final NetworkContext currentContext, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // If a previous context for the network was provided, merge the previous context's
    // configuration with the new network configuration and build a new context.
    NetworkContext updatedContext;
    if (currentContext == null) {
      updatedContext = ContextBuilder.buildContext(network);
    } else {
      updatedContext = ContextBuilder.buildContext(Configs.mergeNetworks(currentContext.config(), network));
    }

    final NetworkContext context = updatedContext;

    // Create an active network to return to the user. The active network can be used to
    // alter the configuration of the live network.
    final WatchableAsyncMap<String, String> data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(context.name()), vertx);
    final DefaultActiveNetwork active = new DefaultActiveNetwork(context.config(), AbstractClusterManager.this);
    data.watch(context.name(), new Handler<MapEvent<String, String>>() {
      @Override
      public void handle(MapEvent<String, String> event) {
        String scontext = event.value();
        if (scontext != null && scontext.length() > 0) {
          active.update(DefaultNetworkContext.fromJson(new JsonObject(scontext)));
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(doneHandler);
        } else {
          // When the context is set in the cluster, the network's manager will be notified
          // via a cluster event. The manager will then unset the network's status key and
          // update the network. Once the network has been updated (components are deployed
          // and undeployed and connections are created or removed as necessary) the manager
          // will reset the network's status key to the updated version. We can use this fact
          // to determine when the configuration change is complete by watching the network's
          // status key for the new context version.
          data.watch(context.status(), new Handler<MapEvent<String, String>>() {
            @Override
            public void handle(MapEvent<String, String> event) {
              // Once the network has been updated we can stop watching the network's status key.
              // Once the status key is unwatched trigger the async handler indicating that the
              // update is complete.
              if (event.type().equals(MapEvent.Type.CREATE) && event.value().equals(context.version())) {
                data.unwatch(context.status(), this, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    new DefaultFutureResult<ActiveNetwork>(active).setHandler(doneHandler);
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
                new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(doneHandler);
              } else {
                data.put(context.address(), DefaultNetworkContext.toJson(context).encode(), new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    // Only fail the handler if the put failed. We don't trigger the async handler
                    // here because we're still waiting for the status key to be set after the update.
                    if (result.failed()) {
                      new DefaultFutureResult<ActiveNetwork>(new ClusterException(result.cause())).setHandler(doneHandler);
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

  @Override
  public ClusterManager undeployNetwork(String name) {
    return undeployNetwork(name, null);
  }

  @Override
  public ClusterManager undeployNetwork(final String name, final Handler<AsyncResult<Void>> doneHandler) {
    // The network's manager should be deployed under a deployment ID
    // of the same name as the network. If the network's manager is deployed
    // then unset the network's context, indicating that the manager should
    // undeploy the network.
    cluster.isDeployed(name, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<Void>(new DeploymentException("Network is not deployed.")).setHandler(doneHandler);
        } else {
          new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(name), vertx).remove(name, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
              } else {
                cluster.undeployVerticle(name, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
                    } else {
                      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
    return this;
  }

  @Override
  public ClusterManager undeployNetwork(NetworkConfig network) {
    return undeployNetwork(network, null);
  }

  @Override
  public ClusterManager undeployNetwork(final NetworkConfig network, final Handler<AsyncResult<Void>> doneHandler) {
    cluster.isDeployed(network.getName(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<Void>(new DeploymentException("Network is not deployed.")).setHandler(doneHandler);
        } else {
          cluster.<String, String>getMap(network.getName()).get(network.getName(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
              } else if (result.result() != null) {
                NetworkContext currentContext = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
                NetworkConfig updatedConfig = Configs.unmergeNetworks(currentContext.config(), network);
                final NetworkContext context = ContextBuilder.buildContext(updatedConfig);

                // If all the components in the network were removed then undeploy the entire network.
                if (context.components().isEmpty()) {
                  new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(network.getName()), vertx).remove(context.address(), new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> result) {
                      if (result.failed()) {
                        new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
                      } else {
                        cluster.undeployVerticle(context.address(), new Handler<AsyncResult<Void>>() {
                          @Override
                          public void handle(AsyncResult<Void> result) {
                            if (result.failed()) {
                              new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
                            } else {
                              new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
                            }
                          }
                        });
                      }
                    }
                  });
                } else {
                  // If the entire network isn't being undeployed, watch the network's status
                  // key to determine when the new configuration has be updated. The network
                  // manager will set the status key to the updated context version once complete.
                  new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(context.name()), vertx).watch(context.status(), new Handler<MapEvent<String, String>>() {
                    @Override
                    public void handle(MapEvent<String, String> event) {
                      // Once the status key has been set to the updated version, stop watching the
                      // status key with this handler and trigger the async handler.
                      if (event.type().equals(MapEvent.Type.CREATE) && event.value().equals(context.version())) {
                        new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(context.name()), vertx).unwatch(context.status(), this, new Handler<AsyncResult<Void>>() {
                          @Override
                          public void handle(AsyncResult<Void> result) {
                            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
                          }
                        });
                      }
                    }
                  }, new Handler<AsyncResult<Void>>() {
                    @Override
                    public void handle(AsyncResult<Void> result) {
                      if (result.failed()) {
                        new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
                      } else {
                        // Once the status key is being watched, update the network's context. This
                        // will cause the network's manager to undeploy components and update connections
                        // and will set the status key once complete.
                        new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(network.getName()), vertx).put(context.address(), DefaultNetworkContext.toJson(context).encode(), new Handler<AsyncResult<String>>() {
                          @Override
                          public void handle(AsyncResult<String> result) {
                            // Only trigger the async handler if the put failed. The async handler will
                            // be complete once the network's status key has been set to the updated version.
                            if (result.failed()) {
                              new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
                            }
                          }
                        });
                      }
                    }
                  });
                }
              } else {
                new DefaultFutureResult<Void>(new DeploymentException("Network configuration not found.")).setHandler(doneHandler);
              }
            }
          });
        }
      }
    });
    return this;
  }

}
