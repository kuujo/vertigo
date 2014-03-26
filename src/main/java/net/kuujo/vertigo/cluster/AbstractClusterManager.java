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
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.impl.ContextBuilder;
import net.kuujo.vertigo.context.impl.DefaultNetworkContext;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.Configs;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultActiveNetwork;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.network.manager.NetworkManager;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * Abstract cluster manager implementation.
 *
 * @author Jordan Halterman
 */
abstract class AbstractClusterManager implements VertigoClusterManager {
  protected final Vertx vertx;
  protected final Container container;
  protected final EventBus eventBus;
  protected final VertigoCluster cluster;

  protected AbstractClusterManager(Vertx vertx, Container container, VertigoCluster cluster) {
    this.vertx = vertx;
    this.container = container;
    this.eventBus = vertx.eventBus();
    this.cluster = cluster;
  }

  @Override
  public VertigoClusterManager getNetwork(final String name, final Handler<AsyncResult<ActiveNetwork>> resultHandler) {
    cluster.isDeployed(name, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(resultHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<ActiveNetwork>(new DeploymentException("Network is not deployed.")).setHandler(resultHandler);
        } else {
          cluster.<String, String>getMap("__CLUSTER__").get(name, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(resultHandler);
              } else {
                NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
                final DefaultActiveNetwork active = new DefaultActiveNetwork(context.config(), AbstractClusterManager.this);
                cluster.<String, String>getMap("__CLUSTER__").watch(name, new Handler<MapEvent<String, String>>() {
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
                      new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(resultHandler);
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
  public VertigoClusterManager deployNetwork(String name) {
    return deployNetwork(name, null);
  }

  @Override
  public VertigoClusterManager deployNetwork(String name, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return deployNetwork(new DefaultNetworkConfig(name), doneHandler);
  }

  @Override
  public VertigoClusterManager deployNetwork(NetworkConfig network) {
    return deployNetwork(network, null);
  }

  @Override
  public VertigoClusterManager deployNetwork(final NetworkConfig network, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    cluster.isDeployed(network.getName(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else if (result.result()) {
          doDeployNetwork(network, doneHandler);
        } else {
          cluster.<String, String>getMap("__CLUSTER__").remove(network.getName(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
              } else {
                JsonObject config = new JsonObject()
                    .putString("address", network.getName())
                    .putString("cluster", cluster.getClass().getName());
                cluster.deployVerticle(network.getName(), NetworkManager.class.getName(), config, 1, new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
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
    cluster.<String, String>getMap("__CLUSTER__").get(network.getName(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          NetworkContext updatedContext;
          if (result.result() != null) {
            NetworkContext currentContext = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
            NetworkConfig updatedConfig = Configs.mergeNetworks(currentContext.config(), network);
            updatedContext = ContextBuilder.buildContext(updatedConfig, cluster);
          } else {
            updatedContext = ContextBuilder.buildContext(network, cluster);
          }

          final NetworkContext context = updatedContext;
          cluster.<String, String>getMap("__CLUSTER__").put(context.address(), DefaultNetworkContext.toJson(context).encode(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
              } else {
                final DefaultActiveNetwork active = new DefaultActiveNetwork(context.config(), AbstractClusterManager.this);
                cluster.<String, String>getMap("__CLUSTER__").watch(network.getName(), new Handler<MapEvent<String, String>>() {
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
                      new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
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
  public VertigoClusterManager undeployNetwork(String address) {
    return undeployNetwork(address, null);
  }

  @Override
  public VertigoClusterManager undeployNetwork(final String address, final Handler<AsyncResult<Void>> doneHandler) {
    cluster.isDeployed(address, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<Void>(new DeploymentException("Network is not deployed.")).setHandler(doneHandler);
        } else {
          cluster.<String, String>getMap("__CLUSTER__").remove(address, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              } else {
                cluster.undeployVerticle(address, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
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
  public VertigoClusterManager undeployNetwork(NetworkConfig network) {
    return undeployNetwork(network, null);
  }

  @Override
  public VertigoClusterManager undeployNetwork(final NetworkConfig network, final Handler<AsyncResult<Void>> doneHandler) {
    cluster.isDeployed(network.getName(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<Void>(new DeploymentException("Network is not deployed.")).setHandler(doneHandler);
        } else {
          cluster.<String, String>getMap("__CLUSTER__").get(network.getName(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              } else if (result.result() != null) {
                NetworkContext currentContext = DefaultNetworkContext.fromJson(new JsonObject(result.result()));
                NetworkConfig updatedConfig = Configs.unmergeNetworks(currentContext.config(), network);
                final NetworkContext context = ContextBuilder.buildContext(updatedConfig, cluster);
                if (context.components().isEmpty()) {
                  cluster.<String, String>getMap("__CLUSTER__").remove(context.address(), new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> result) {
                      if (result.failed()) {
                        new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
                      } else {
                        cluster.undeployVerticle(context.address(), new Handler<AsyncResult<Void>>() {
                          @Override
                          public void handle(AsyncResult<Void> result) {
                            if (result.failed()) {
                              new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
                            } else {
                              new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
                            }
                          }
                        });
                      }
                    }
                  });
                } else {
                  cluster.<String, String>getMap("__CLUSTER__").put(context.address(), DefaultNetworkContext.toJson(context).encode(), new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> result) {
                      if (result.failed()) {
                        new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
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
