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

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.impl.ContextBuilder;
import net.kuujo.vertigo.network.Network;
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
  public VertigoClusterManager getNetwork(final String address, final Handler<AsyncResult<NetworkContext>> resultHandler) {
    cluster.isDeployed(address, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(resultHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<NetworkContext>(new DeploymentException("Network is not deployed.")).setHandler(resultHandler);
        } else {
          cluster.<String, String>getMap("__CLUSTER__").get(address, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(resultHandler);
              } else {
                new DefaultFutureResult<NetworkContext>(NetworkContext.fromJson(new JsonObject(result.result())));
              }
            }
          });
        }
      }
    });
    return this;
  }

  @Override
  public VertigoClusterManager deployNetwork(Network network) {
    return deployNetwork(network, null);
  }

  @Override
  public VertigoClusterManager deployNetwork(final Network network, final Handler<AsyncResult<NetworkContext>> doneHandler) {
    cluster.isDeployed(network.getAddress(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
        } else if (result.result()) {
          doDeployNetwork(network, doneHandler);
        } else {
          cluster.<String, String>getMap("__CLUSTER__").remove(network.getAddress(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
              } else {
                JsonObject config = new JsonObject()
                    .putString("address", network.getAddress())
                    .putString("cluster", cluster.getClass().getName());
                cluster.deployVerticle(network.getAddress(), NetworkManager.class.getName(), config, 1, new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
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
  private void doDeployNetwork(final Network network, final Handler<AsyncResult<NetworkContext>> doneHandler) {
    cluster.<String, String>getMap("__CLUSTER__").get(network.getAddress(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
        } else {
          NetworkContext updatedContext;
          if (result.result() != null) {
            updatedContext = ContextBuilder.mergeContexts(NetworkContext.fromJson(new JsonObject(result.result())), ContextBuilder.buildContext(network, cluster));
          } else {
            updatedContext = ContextBuilder.buildContext(network, cluster);
          }

          final NetworkContext context = updatedContext;
          cluster.<String, String>getMap("__CLUSTER__").put(context.address(), NetworkContext.toJson(context).encode(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
              } else {
                new DefaultFutureResult<NetworkContext>(context).setHandler(doneHandler);
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
  public VertigoClusterManager undeployNetwork(Network network) {
    return undeployNetwork(network, null);
  }

  @Override
  public VertigoClusterManager undeployNetwork(final Network network, final Handler<AsyncResult<Void>> doneHandler) {
    cluster.isDeployed(network.getAddress(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<Void>(new DeploymentException("Network is not deployed.")).setHandler(doneHandler);
        } else {
          cluster.<String, String>getMap("__CLUSTER__").get(network.getAddress(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
              } else if (result.result() != null) {
                final NetworkContext context = ContextBuilder.unmergeContexts(NetworkContext.fromJson(new JsonObject(result.result())), ContextBuilder.buildContext(network, cluster));
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
                  cluster.<String, String>getMap("__CLUSTER__").put(context.address(), NetworkContext.toJson(context).encode(), new Handler<AsyncResult<String>>() {
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
