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
package net.kuujo.vertigo.network.manager;

import static net.kuujo.vertigo.util.Config.buildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.ClusterFactory;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.WrappedWatchableAsyncMap;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.component.impl.DefaultComponentContext;
import net.kuujo.vertigo.component.impl.DefaultInstanceContext;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Task;
import net.kuujo.vertigo.util.TaskRunner;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

/**
 * Vertigo network manager.<p>
 *
 * The manager is at the core of all Vertigo networks. Its responsibilities
 * are to handle deployment, undeployment, reconfiguration, and coordination
 * of components within a network. Each network has its own network manager.
 * The manager will be deployed to the network's cluster scope using the
 * network name as the user-assigned deployment ID.<p>
 *
 * The manager uses the highest level cluster scope available to coordinate
 * networks. When the manager deploys a component, it will set a key in the
 * cluster for that component containing the component's configuration. Once
 * the component has been deployed, the component will then watch that key
 * for changes. If the network's configuration changes - e.g. through an
 * active network configuration change - the network will automatically
 * notify all running components by updating their individual configurations
 * in the cluster.<p>
 *
 * Once a component has completed startup, it will set a status key in the
 * cluster. The manager watches status keys for each component instance in
 * the network and once all status keys have been set the manager sets a
 * network-wide status key indicating that the network is ready, e.g. all
 * instances in the network are running and their connections are open.
 * When a configuration change occurs, the manager will unset the network-wide
 * status key, indicating to deployed instances that a configuration change
 * is taking place. This allows components to potentially take action preventing
 * data loss prior to configuration changes.<p>
 *
 * Note that configuration changes are essentially atomic. When a configuration
 * change is detected, if the manager is already processing a configuration change
 * then the change will be queued for processing once the current configuration
 * change is complete.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NetworkManager extends Verticle {
  private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
  private String address;
  private Cluster cluster;
  private WatchableAsyncMap<String, String> data;
  private Set<String> ready = new HashSet<>();
  private NetworkContext currentContext;
  private final TaskRunner tasks = new TaskRunner();

  private final Map<String, Handler<MapEvent<String, String>>> watchHandlers = new HashMap<>();

  private final Handler<MapEvent<String, String>> watchHandler = new Handler<MapEvent<String, String>>() {
    @Override
    public void handle(MapEvent<String, String> event) {
      if (event.type().equals(MapEvent.Type.CREATE)) {
        handleCreate(DefaultNetworkContext.fromJson(new JsonObject(event.value())));
      } else if (event.type().equals(MapEvent.Type.UPDATE)) {
        handleUpdate(DefaultNetworkContext.fromJson(new JsonObject(event.value())));
      } else if (event.type().equals(MapEvent.Type.DELETE)) { 
        handleDelete();
      }
    }
  };

  @Override
  public void start(final Future<Void> startResult) {
    address = container.config().getString("address");
    if (address == null) {
      startResult.setFailure(new IllegalArgumentException("No network address specified."));
      return;
    }

    String scluster = container.config().getString("cluster");
    if (scluster == null) {
      startResult.setFailure(new IllegalArgumentException("No cluster address specified."));
      return;
    }

    cluster = ClusterFactory.getCluster(scluster, vertx, container);

    // Load the current cluster. Regardless of the network's cluster scope,
    // we use the CLUSTER for coordination if it's available. This ensures
    // that identical networks cannot be deployed from separate clustered
    // Vert.x instances.
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(address), vertx);
        data.watch(address, watchHandler, new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              startResult.setFailure(result.cause());
            } else {
              // In the event that the manager failed, the current network context
              // will be persisted in the cluster. Attempt to retrieve it.
              data.get(address, new Handler<AsyncResult<String>>() {
                @Override
                public void handle(AsyncResult<String> result) {
                  if (result.failed()) {
                    startResult.setFailure(result.cause());
                  } else if (result.result() != null) {
                    currentContext = DefaultNetworkContext.fromJson(new JsonObject(result.result()));

                    final CountingCompletionHandler<Void> componentCounter = new CountingCompletionHandler<Void>(currentContext.components().size());
                    componentCounter.setHandler(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        if (result.failed()) {
                          startResult.setFailure(result.cause());
                        } else {
                          NetworkManager.super.start(startResult);
                        }
                        task.complete();
                      }
                    });

                    // Try to determine the current status of the network.
                    for (ComponentContext<?> component : currentContext.components()) {
                      final CountingCompletionHandler<Void> instanceCounter = new CountingCompletionHandler<Void>(component.instances().size());
                      instanceCounter.setHandler(new Handler<AsyncResult<Void>>() {
                        @Override
                        public void handle(AsyncResult<Void> result) {
                          if (result.failed()) {
                            componentCounter.fail(result.cause());
                          } else {
                            componentCounter.succeed();
                          }
                        }
                      });
                      for (final InstanceContext instance : component.instances()) {
                        data.containsKey(instance.status(), new Handler<AsyncResult<Boolean>>() {
                          @Override
                          public void handle(AsyncResult<Boolean> result) {
                            if (result.failed()) {
                              instanceCounter.fail(result.cause());
                            } else if (result.result()) {
                              handleReady(instance.address());
                              instanceCounter.succeed();
                            } else {
                              handleUnready(instance.address());
                              instanceCounter.succeed();
                            }
                          }
                        });
                      }
                    }
                  } else {
                    task.complete();
                    NetworkManager.super.start(startResult);
                  }
                }
              });
            }
          }
        });
      }
    });
  }

  /**
   * Handles the creation of the network.
   */
  private void handleCreate(final NetworkContext context) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        currentContext = context;

        // Any time the network is being reconfigured, unready the network.
        // This will cause components to pause during the reconfiguration.
        unready(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              log.error(result.cause());
            } else {
              deployNetwork(context, new Handler<AsyncResult<NetworkContext>>() {
                @Override
                public void handle(AsyncResult<NetworkContext> result) {
                  if (result.failed()) {
                    log.error(result.cause());
                  } else {
                    log.info("Successfully deployed network " + context.address());
                    task.complete();
                    checkReady();
                  }
                }
              });
            }
          }
        });
      }
    });
  }

  /**
   * Handles the update of the network.
   */
  private void handleUpdate(final NetworkContext context) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        // Any time the network is being reconfigured, unready the network.
        // This will cause components to pause during the reconfiguration.
        unready(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              log.error(result.cause());
            } else {
              if (currentContext != null) {
                final NetworkContext runningContext = currentContext;
                currentContext = context;
                

                // We have to update all instance contexts before deploying
                // any new components in order to ensure connections are
                // available for startup.
                log.info("Updating network " + currentContext.name() + " in cluster " + address);
                updateNetwork(currentContext, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      log.error(result.cause());
                    } else {
                      undeployRemovedComponents(currentContext, runningContext, new Handler<AsyncResult<Void>>() {
                        @Override
                        public void handle(AsyncResult<Void> result) {
                          if (result.failed()) {
                            log.error(result.cause());
                          } else {
                            deployAddedComponents(currentContext, runningContext, new Handler<AsyncResult<Void>>() {
                              @Override
                              public void handle(AsyncResult<Void> result) {
                                if (result.failed()) {
                                  log.error(result.cause());
                                } else {
                                  task.complete();
                                  checkReady();
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
              else {
                // Just deploy the entire network if it wasn't already deployed.
                currentContext = context;

                deployNetwork(context, new Handler<AsyncResult<NetworkContext>>() {
                  @Override
                  public void handle(AsyncResult<NetworkContext> result) {
                    if (result.failed()) {
                      log.error(result.cause());
                    } else {
                      log.info("Successfully deployed network " + context.address());
                      task.complete();
                      checkReady();
                    }
                  }
                });
              }
            }
          }
        });
      }
    });
  }

  /**
   * Undeploys components that were removed from the network.
   */
  private void undeployRemovedComponents(final NetworkContext context, final NetworkContext runningContext, final Handler<AsyncResult<Void>> doneHandler) {
    // Undeploy any components that were removed from the network.
    final List<ComponentContext<?>> removedComponents = new ArrayList<>();
    for (ComponentContext<?> runningComponent : runningContext.components()) {
      if (context.component(runningComponent.name()) == null) {
        removedComponents.add(runningComponent);
      }
    }

    if (!removedComponents.isEmpty()) {
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(removedComponents.size());
      counter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            log.info(String.format("Removed %s components from %s", removedComponents.size(), context.name()));
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });
      undeployComponents(removedComponents, counter);
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
  }

  /**
   * Deploys components that were added to the network.
   */
  private void deployAddedComponents(final NetworkContext context, NetworkContext runningContext, final Handler<AsyncResult<Void>> doneHandler) {
    // Deploy any components that were added to the network.
    final List<ComponentContext<?>> addedComponents = new ArrayList<>();
    for (ComponentContext<?> component : context.components()) {
      if (runningContext.component(component.name()) == null) {
        addedComponents.add(component);
      }
    }
    if (!addedComponents.isEmpty()) {
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(addedComponents.size());
      counter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            log.info(String.format("Added %s components to %s", addedComponents.size(), context.name()));
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });
      deployComponents(addedComponents, counter);
    } else {
      new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
    }
  }

  /**
   * Handles the deletion of the network.
   */
  private void handleDelete() {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        unready(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              log.error(result.cause());
            }
            if (currentContext != null) {
              undeployNetwork(currentContext, new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> result) {
                  if (result.failed()) {
                    log.error(result.cause());
                  } else {
                    // Once we've finished undeploying all the components of the
                    // network, set the network's status to nothing in order to
                    // indicate that the manager (this) can be undeployed.
                    data.put(currentContext.status(), "", new Handler<AsyncResult<String>>() {
                      @Override
                      public void handle(AsyncResult<String> result) {
                        if (result.failed()) {
                          log.error(result.cause());
                        } else {
                          log.info("Successfully undeployed components of " + currentContext.address());
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
    });
  }

  /**
   * Unreadies the network.
   */
  private void unready(final Handler<AsyncResult<Void>> doneHandler) {
    if (currentContext != null && data != null) {
      data.remove(currentContext.status(), new Handler<AsyncResult<String>>() {
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
  }

  /**
   * Called when a component instance is ready.
   */
  private void handleReady(String address) {
    ready.add(address);
    checkReady();
  }

  /**
   * Checks whether the network is ready.
   */
  private void checkReady() {
    if (allReady()) {
      // Set the network's status key to the current context version. This
      // can be used by listeners to determine when a configuration change is complete.
      data.put(currentContext.status(), currentContext.version(), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            log.error(result.cause());
          }
        }
      });
    }
  }

  /**
   * Called when a component instance is unready.
   */
  private void handleUnready(String address) {
    ready.remove(address);
    checkUnready();
  }

  /**
   * Checks whether the network is unready.
   */
  private void checkUnready() {
    if (!allReady()) {
      data.remove(currentContext.address(), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            log.error(result.cause());
          }
        }
      });
    }
  }

  /**
   * Checks whether all components are ready in the network.
   */
  private boolean allReady() {
    if (currentContext == null) return false;
    List<InstanceContext> instances = new ArrayList<>();
    boolean match = true;
    outer: for (ComponentContext<?> component : currentContext.components()) {
      instances.addAll(component.instances());
      for (InstanceContext instance : component.instances()) {
        if (!ready.contains(instance.address())) {
          match = false;
          break outer;
        }
      }
    }
    return match;
  }

  /**
   * Deploys a complete network.
   */
  private void deployNetwork(final NetworkContext context, final Handler<AsyncResult<NetworkContext>> doneHandler) {
    final CountingCompletionHandler<Void> complete = new CountingCompletionHandler<Void>(context.components().size());
    complete.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<NetworkContext>(context).setHandler(doneHandler);
        }
      }
    });
    deployComponents(context.components(), complete);
  }

  /**
   * Deploys all network components.
   */
  private void deployComponents(List<ComponentContext<?>> components, final CountingCompletionHandler<Void> complete) {
    for (final ComponentContext<?> component : components) {
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(component.instances().size());
      counter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            complete.fail(result.cause());
          } else {
            data.put(component.address(), DefaultComponentContext.toJson(component).encode(), new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                if (result.failed()) {
                  complete.fail(result.cause());
                } else {
                  complete.succeed();
                }
              }
            });
          }
        }
      });
      deployInstances(component.instances(), counter);
    }
  }

  /**
   * Deploys all network component instances.
   */
  private void deployInstances(List<InstanceContext> instances, final CountingCompletionHandler<Void> counter) {
    for (final InstanceContext instance : instances) {
      // Before deploying the instance, check if the instance is already deployed in
      // the network's cluster.
      cluster.isDeployed(instance.address(), new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else if (result.result()) {
            // Even if the instance is already deployed, update its context in the cluster.
            // It's possible that the instance's connections could have changed with the update.
            data.put(instance.address(), DefaultInstanceContext.toJson(instance).encode(), new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                if (result.failed()) {
                  counter.fail(result.cause());
                } else {
                  counter.succeed();
                }
              }
            });
          } else {
            deployInstance(instance, counter);
          }
        }
      });
    }
  }

  /**
   * Deploys a single component instance.
   */
  private void deployInstance(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    // Set the instance context in the cluster. This context will be loaded
    // and referenced by the instance once it's deployed.
    log.info(String.format("Deploying instance %d of %s", instance.number(), instance.component().name()));
    data.put(instance.address(), DefaultInstanceContext.toJson(instance).encode(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else {
          if (!watchHandlers.containsKey(instance.address())) {
            final Handler<MapEvent<String, String>> watchHandler = new Handler<MapEvent<String, String>>() {
              @Override
              public void handle(MapEvent<String, String> event) {
                if (event.type().equals(MapEvent.Type.CREATE)) {
                  handleReady(instance.address());
                } else if (event.type().equals(MapEvent.Type.DELETE)) {
                  handleUnready(instance.address());
                }
              }
            };
            // Watch the instance's status for changes. Once the instance has started
            // up, the component coordinator will set the instance's status key in the
            // cluster, indicating that the instance has completed started. Once all
            // instances in the network have completed startup the network will be started.
            data.watch(instance.status(), watchHandler, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  counter.fail(result.cause());
                } else {
                  watchHandlers.put(instance.address(), watchHandler);
                  if (instance.component().isModule()) {
                    deployModule(instance, counter);
                  } else if (instance.component().isVerticle() && !instance.component().asVerticle().isWorker()) {
                    deployVerticle(instance, counter);
                  } else if (instance.component().isVerticle() && instance.component().asVerticle().isWorker()) {
                    deployWorkerVerticle(instance, counter);
                  }
                }
              }
            });
          } else {
            if (instance.component().isModule()) {
              deployModule(instance, counter);
            } else if (instance.component().isVerticle() && !instance.component().asVerticle().isWorker()) {
              deployVerticle(instance, counter);
            } else if (instance.component().isVerticle() && instance.component().asVerticle().isWorker()) {
              deployWorkerVerticle(instance, counter);
            }
          }
        }
      }
    });
  }

  /**
   * Deploys a module component instance in the network's cluster.
   */
  private void deployModule(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    cluster.deployModuleTo(instance.address(), instance.component().group(), instance.component().asModule().module(), buildConfig(instance, cluster), 1, true, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else {
          counter.succeed();
        }
      }
    });
  }

  /**
   * Deploys a verticle component instance in the network's cluster.
   */
  private void deployVerticle(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    cluster.deployVerticleTo(instance.address(), instance.component().group(), instance.component().asVerticle().main(), buildConfig(instance, cluster), 1, true, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else {
          counter.succeed();
        }
      }
    });
  }

  /**
   * Deploys a worker verticle component instance in the network's cluster.
   */
  private void deployWorkerVerticle(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    cluster.deployWorkerVerticleTo(instance.address(), instance.component().group(), instance.component().asVerticle().main(), buildConfig(instance, cluster), 1, instance.component().asVerticle().isMultiThreaded(), true, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else {
          counter.succeed();
        }
      }
    });
  }

  /**
   * Undeploys a network.
   */
  private void undeployNetwork(final NetworkContext context, final Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> complete = new CountingCompletionHandler<Void>(context.components().size());
    complete.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    undeployComponents(context.components(), complete);
  }

  /**
   * Undeploys all network components.
   */
  private void undeployComponents(List<ComponentContext<?>> components, final CountingCompletionHandler<Void> complete) {
    for (final ComponentContext<?> component : components) {
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(component.instances().size());
      counter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            complete.fail(result.cause());
          } else {
            // Remove the component's context from the cluster.
            data.remove(component.address(), new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                if (result.failed()) {
                  complete.fail(result.cause());
                } else {
                  complete.succeed();
                }
              }
            });
          }
        }
      });
      undeployInstances(component.instances(), counter);
    }
  }

  /**
   * Undeploys all component instances.
   */
  private void undeployInstances(List<InstanceContext> instances, final CountingCompletionHandler<Void> counter) {
    for (final InstanceContext instance : instances) {
      cluster.isDeployed(instance.address(), new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else if (result.result()) {
            if (instance.component().isModule()) {
              undeployModule(instance, counter);
            } else if (instance.component().isVerticle()) {
              undeployVerticle(instance, counter);
            }
          } else {
            unwatchInstance(instance, counter);
          }
        }
      });
    }
  }

  /**
   * Unwatches a component instance's status and context.
   */
  private void unwatchInstance(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    Handler<MapEvent<String, String>> watchHandler = watchHandlers.remove(instance.address());
    if (watchHandler != null) {
      data.unwatch(instance.status(), watchHandler, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          data.remove(instance.address(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                counter.fail(result.cause());
              } else {
                counter.succeed();
              }
            }
          });
        }
      });
    } else {
      data.remove(instance.address(), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else {
            counter.succeed();
          }
        }
      });
    }
  }

  /**
   * Undeploys a module component instance.
   */
  private void undeployModule(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    cluster.undeployModule(instance.address(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        unwatchInstance(instance, counter);
      }
    });
  }

  /**
   * Undeploys a verticle component instance.
   */
  private void undeployVerticle(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    cluster.undeployVerticle(instance.address(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        unwatchInstance(instance, counter);
      }
    });
  }

  /**
   * Updates a network.
   */
  private void updateNetwork(final NetworkContext context, final Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> complete = new CountingCompletionHandler<Void>(context.components().size());
    complete.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    updateComponents(context.components(), complete);
  }

  /**
   * Updates all network components.
   */
  private void updateComponents(List<ComponentContext<?>> components, final CountingCompletionHandler<Void> complete) {
    for (final ComponentContext<?> component : components) {
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(component.instances().size());
      counter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            complete.fail(result.cause());
          } else {
            complete.succeed();
          }
        }
      });
      updateInstances(component.instances(), counter);
    }
  }

  /**
   * Updates all component instances.
   */
  private void updateInstances(List<InstanceContext> instances, final CountingCompletionHandler<Void> counter) {
    for (final InstanceContext instance : instances) {
      data.put(instance.address(), DefaultInstanceContext.toJson(instance).encode(), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else {
            counter.succeed();
          }
        }
      });
    }
  }

}
