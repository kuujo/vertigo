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
package net.kuujo.vertigo.cluster;

import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.impl.ContextBuilder;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultActiveNetwork;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
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

  @Override
  public ClusterManager getNetwork(final String name, final Handler<AsyncResult<ActiveNetwork>> resultHandler) {
    cluster.isDeployed(name, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(resultHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<ActiveNetwork>(new DeploymentException("Network is not deployed.")).setHandler(resultHandler);
        } else {
          cluster.<String, String>getMap(name).get(name, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(resultHandler);
              } else {
                NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
                final DefaultActiveNetwork active = new DefaultActiveNetwork(context.config(), AbstractClusterManager.this);
                cluster.<String, String>getMap(name).watch(name, new Handler<MapEvent<String, String>>() {
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
                      new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(resultHandler);
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
    cluster.isDeployed(name, resultHandler);
    return this;
  }

  @Override
  public ClusterManager deployNetwork(String name) {
    return deployNetwork(name, null);
  }

  @Override
  public ClusterManager deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(new DefaultNetworkConfig(name), doneHandler);
  }

  @Override
  public ClusterManager deployNetwork(NetworkConfig network) {
    return deployNetwork(network, null);
  }

  @Override
  public ClusterManager deployNetwork(final NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    cluster.isDeployed(network.getName(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(doneHandler);
        } else if (result.result()) {
          doDeployNetwork(network, doneHandler);
        } else {
          cluster.<String, String>getMap(network.getName()).remove(network.getName(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(doneHandler);
              } else {
                // Deploy the network manager according to the network cluster type. If the
                // current Vertigo scope is CLUSTER *and* the network's scope is CLUSTER then
                // deploy the manager as a cluster. Otherwise, the network is local since either
                // local scope was specified on the network or the current instance is local.
                // All coordination is done in the current Vertigo scope regarless of the
                // network configuration.
                Cluster contextCluster;
                if (network.getScope().equals(ClusterScope.CLUSTER) && scope().equals(ClusterScope.CLUSTER)) {
                  contextCluster = new ClusterFactory(vertx, container).createCluster(ClusterScope.CLUSTER);
                } else {
                  contextCluster = new ClusterFactory(vertx, container).createCluster(ClusterScope.LOCAL);
                }
                JsonObject config = new JsonObject().putString("name", network.getName());
                contextCluster.deployVerticle(network.getName(), NetworkManager.class.getName(), config, 1, new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(doneHandler);
                    } else {
                      doDeployNetwork(network, doneHandler);
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
  private void doDeployNetwork(final NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // Attempt to find an existing configuration for the given network.
    cluster.<String, String>getMap(network.getName()).get(network.getName(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(doneHandler);
        } else {
          // If the network's configuration already exists, merge the new
          // configuration with the existing configuration and update the
          // network context.
          NetworkContext updatedContext;
          if (result.result() != null) {
            NetworkContext currentContext = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
            NetworkConfig updatedConfig = Configs.mergeNetworks(currentContext.config(), network);
            updatedContext = ContextBuilder.buildContext(updatedConfig);
          } else {
            updatedContext = ContextBuilder.buildContext(network);
          }

          // Once the updated context has been set in the cluster, the network's manager
          // verticle (which should be watching the configuration) will be notified and
          // will asynchronously update component configurations and deploy/undeploy
          // any components as necessary.
          final NetworkContext context = updatedContext;
          cluster.<String, String>getMap(network.getName()).put(context.address(), DefaultNetworkContext.toJson(context).encode(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(doneHandler);
              } else {
                // Create an active network. The active network should watch the configuration
                // in the cluster as well so we can update the active network's configuration.
                final DefaultActiveNetwork active = new DefaultActiveNetwork(context.config(), AbstractClusterManager.this);
                cluster.<String, String>getMap(network.getName()).watch(network.getName(), new Handler<MapEvent<String, String>>() {
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
                      new DefaultFutureResult<ActiveNetwork>(new DeploymentException(result.cause())).setHandler(doneHandler);
                    } else {
                      new DefaultFutureResult<ActiveNetwork>(active).setHandler(doneHandler);
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
          cluster.<String, String>getMap(name).remove(name, new Handler<AsyncResult<String>>() {
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
                if (context.components().isEmpty()) {
                  cluster.<String, String>getMap(network.getName()).remove(context.address(), new Handler<AsyncResult<String>>() {
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
                  cluster.<String, String>getMap(network.getName()).put(context.address(), DefaultNetworkContext.toJson(context).encode(), new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> result) {
                      if (result.failed()) {
                        new DefaultFutureResult<Void>(new DeploymentException(result.cause())).setHandler(doneHandler);
                      } else {
                        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
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
