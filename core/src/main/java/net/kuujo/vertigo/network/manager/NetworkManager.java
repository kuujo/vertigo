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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.Group;
import net.kuujo.vertigo.cluster.Node;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.WrappedWatchableAsyncMap;
import net.kuujo.vertigo.cluster.impl.DefaultCluster;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.component.ModuleContext;
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
  private AsyncMap<String, String> deploymentIDs;
  private AsyncMap<String, String> deploymentNodes;
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

  private final Handler<Node> joinHandler = new Handler<Node>() {
    @Override
    public void handle(Node node) {
       doNodeJoined(node);
    }
  };

  private final Handler<Node> leaveHandler = new Handler<Node>() {
    @Override
    public void handle(Node node) {
      doNodeLeft(node);
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

    cluster = new DefaultCluster(scluster, vertx, container);
    deploymentIDs = cluster.<String, String>getMap(String.format("deployments.%s", address));
    deploymentNodes = cluster.<String, String>getMap(String.format("nodes.%s", address));

    // Load the current cluster. Regardless of the network's cluster scope,
    // we use the CLUSTER for coordination if it's available. This ensures
    // that identical networks cannot be deployed from separate clustered
    // Vert.x instances.
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(2);
        counter.setHandler(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              startResult.setFailure(result.cause());
            } else {
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
          }
        });
        cluster.registerJoinHandler(joinHandler, counter);
        cluster.registerLeaveHandler(leaveHandler, counter);
      }
    });
  }

  /**
   * Handles the creation of the network.
   */
  private void handleCreate(final NetworkContext context) {
    log.debug("Context created in cluster " + cluster.address());
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
              task.complete();
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
    log.debug("Context update received in cluster " + cluster.address());
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
              task.complete();
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
                      task.complete();
                    } else {
                      undeployRemovedComponents(currentContext, runningContext, new Handler<AsyncResult<Void>>() {
                        @Override
                        public void handle(AsyncResult<Void> result) {
                          if (result.failed()) {
                            log.error(result.cause());
                            task.complete();
                          } else {
                            deployAddedComponents(currentContext, runningContext, new Handler<AsyncResult<Void>>() {
                              @Override
                              public void handle(AsyncResult<Void> result) {
                                task.complete();
                                if (result.failed()) {
                                  log.error(result.cause());
                                } else {
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
                    task.complete();
                    if (result.failed()) {
                      log.error(result.cause());
                    } else {
                      log.info("Successfully deployed network " + context.address());
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
            log.info(String.format("Removed %d components from %s", removedComponents.size(), context.name()));
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
            log.info(String.format("Added %d components to %s", addedComponents.size(), context.name()));
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
    log.debug("Context deleted in cluster " + cluster.address());
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
                    task.complete();
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
                        task.complete();
                      }
                    });
                  }
                }
              });
            } else {
              task.complete();
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
      log.debug("Pausing network " + currentContext.name() + " in cluster " + cluster.address());
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
    log.debug("Received ready message from " + address + " in network " + currentContext.name());
    ready.add(address);
    checkReady();
  }

  /**
   * Checks whether the network is ready.
   */
  private void checkReady() {
    if (allReady()) {
      log.debug("All components ready in network " + currentContext.name() + ", starting components");
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
    log.debug("Received unready message from " + address + " in network " + currentContext.name());
    ready.remove(address);
    checkUnready();
  }

  /**
   * Checks whether the network is unready.
   */
  private void checkUnready() {
    if (!allReady()) {
      log.debug("Components not ready in network " + currentContext.name() + ", pausing components");
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
    log.debug("Deploying network " + context.toString());
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
  private void deployComponents(List<ComponentContext<?>> components, final CountingCompletionHandler<Void> counter) {
    for (final ComponentContext<?> component : components) {
      deployComponent(component, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else {
            data.put(component.address(), DefaultComponentContext.toJson(component).encode(), new Handler<AsyncResult<String>>() {
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
      });
    }
  }

  /**
   * Deploys a component.
   */
  private void deployComponent(final ComponentContext<?> component, final Handler<AsyncResult<Void>> doneHandler) {
    log.info(String.format("Deploying %d instances of %s", component.instances().size(), component.isModule() ? component.asModule().module() : component.asVerticle().main()));
    log.debug("Deploying component " + component.toString());
    // If the component is installable then we first need to install the
    // component to all the nodes in the cluster.
    if (component.isModule()) {
      final ModuleContext module = component.asModule();
      // If the component has a group then install the module to the group.
      if (component.group() != null) {
        // Make sure to install the module only to the appropriate deployment group.
        cluster.getGroup(component.group(), new Handler<AsyncResult<Group>>() {
          @Override
          public void handle(AsyncResult<Group> result) {
            if (result.failed()) {
              new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
            } else {
              result.result().getNodes(new Handler<AsyncResult<Collection<Node>>>() {
                @Override
                public void handle(AsyncResult<Collection<Node>> result) {
                  if (result.failed()) {
                    new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
                  } else {
                    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(result.result().size());
                    counter.setHandler(new Handler<AsyncResult<Void>>() {
                      @Override
                      public void handle(AsyncResult<Void> result) {
                        if (result.failed()) {
                          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
                        } else {
                          deployInstances(component.instances(), doneHandler);
                        }
                      }
                    });
                    for (Node node : result.result()) {
                      installModule(node, module, counter);
                    }
                  }
                }
              });
            }
          }
        });
      } else {
        // If the component doesn't have a group then it needs to be installed
        // to the cluster.
        cluster.getNodes(new Handler<AsyncResult<Collection<Node>>>() {
          @Override
          public void handle(AsyncResult<Collection<Node>> result) {
            if (result.failed()) {
              new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
            } else {
              final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(result.result().size());
              counter.setHandler(new Handler<AsyncResult<Void>>() {
                @Override
                public void handle(AsyncResult<Void> result) {
                  if (result.failed()) {
                    new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
                  } else {
                    deployInstances(component.instances(), doneHandler);
                  }
                }
              });
              for (Node node : result.result()) {
                installModule(node, module, counter);
              }
            }
          }
        });
      }
    } else {
      deployInstances(component.instances(), doneHandler);
    }
  }

  /**
   * Deploys all network component instances.
   */
  private void deployInstances(List<InstanceContext> instances, final Handler<AsyncResult<Void>> doneHandler) {
    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(instances.size()).setHandler(doneHandler);
    for (final InstanceContext instance : instances) {
      // Before deploying the instance, check if the instance is already deployed in
      // the network's cluster.
      deploymentIDs.get(instance.address(), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else if (result.result() == null) {
            // If no deployment ID has been assigned to the instance then that means it hasn't
            // yet been deployed. Deploy the instance.
            deployInstance(instance, counter);
          } else {
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
          }
        }
      });
    }
  }

  /**
   * Deploys a single component instance.
   */
  private void deployInstance(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    // First we need to select a node from the component's deployment group
    // to which to deploy the component.
    // If the component doesn't specify a group then deploy to any node in the cluster.
    if (instance.component().group() != null) {
      cluster.getGroup(instance.component().group(), new Handler<AsyncResult<Group>>() {
        @Override
        public void handle(AsyncResult<Group> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else {
            result.result().selectNode(instance.address(), new Handler<AsyncResult<Node>>() {
              @Override
              public void handle(AsyncResult<Node> result) {
                if (result.failed()) {
                  counter.fail(result.cause());
                } else {
                  deployInstance(result.result(), instance, counter);
                }
              }
            });
          }
        }
      });
    } else {
      cluster.selectNode(instance.address(), new Handler<AsyncResult<Node>>() {
        @Override
        public void handle(AsyncResult<Node> result) {
          if (result.failed()) {
            counter.fail(result.cause());
          } else {
            deployInstance(result.result(), instance, counter);
          }
        }
      });
    }
  }

  /**
   * Deploys an instance to a specific node.
   */
  private void deployInstance(final Node node, final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
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
                    deployModule(node, instance, counter);
                  } else if (instance.component().isVerticle() && !instance.component().asVerticle().isWorker()) {
                    deployVerticle(node, instance, counter);
                  } else if (instance.component().isVerticle() && instance.component().asVerticle().isWorker()) {
                    deployWorkerVerticle(node, instance, counter);
                  }
                }
              }
            });
          } else {
            if (instance.component().isModule()) {
              deployModule(node, instance, counter);
            } else if (instance.component().isVerticle() && !instance.component().asVerticle().isWorker()) {
              deployVerticle(node, instance, counter);
            } else if (instance.component().isVerticle() && instance.component().asVerticle().isWorker()) {
              deployWorkerVerticle(node, instance, counter);
            }
          }
        }
      }
    });
  }

  /**
   * Deploys a module component instance in the network's cluster.
   */
  private void deployModule(final Node node, final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    log.debug(String.format("Deploying %s to %s", instance.component().asModule().module(), node.address()));
    node.deployModule(instance.component().asModule().module(), buildConfig(instance, cluster), 1, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else {
          deploymentIDs.put(instance.address(), result.result(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              counter.succeed();
            }
          });
        }
      }
    });
  }

  /**
   * Deploys a verticle component instance in the network's cluster.
   */
  private void deployVerticle(final Node node, final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    log.debug(String.format("Deploying %s to %s", instance.component().asVerticle().main(), node.address()));
    node.deployVerticle(instance.component().asVerticle().main(), buildConfig(instance, cluster), 1, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else {
          deploymentIDs.put(instance.address(), result.result(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              counter.succeed();
            }
          });
        }
      }
    });
  }

  /**
   * Deploys a worker verticle component instance in the network's cluster.
   */
  private void deployWorkerVerticle(final Node node, final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    log.debug(String.format("Deploying %s to %s", instance.component().asVerticle().main(), node.address()));
    node.deployWorkerVerticle(instance.component().asVerticle().main(), buildConfig(instance, cluster), 1,instance.component().asVerticle().isMultiThreaded(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else {
          deploymentIDs.put(instance.address(), result.result(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              counter.succeed();
            }
          });
        }
      }
    });
  }

  /**
   * Installs all modules on a node.
   */
  private void installModules(final Node node, final NetworkContext context, final Handler<AsyncResult<Void>> doneHandler) {
    List<ModuleContext> modules = new ArrayList<>();
    for (ComponentContext<?> component : context.components()) {
      if (component.isModule()) {
        modules.add(component.asModule());
      }
    }

    final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(modules.size()).setHandler(doneHandler);
    for (ModuleContext module : modules) {
      installModule(node, module, counter);
    }
  }

  /**
   * Installs a module on a node.
   */
  private void installModule(final Node node, final ModuleContext module, final Handler<AsyncResult<Void>> doneHandler) {
    node.installModule(module.module(), doneHandler);
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
      if (instance.component().isModule()) {
        undeployModule(instance, counter);
      } else if (instance.component().isVerticle()) {
        undeployVerticle(instance, counter);
      }
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
    deploymentIDs.remove(instance.address(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else if (result.result() != null) {
          cluster.undeployModule(result.result(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              unwatchInstance(instance, counter);
            }
          });
        } else {
          unwatchInstance(instance, counter);
        }
      }
    });
  }

  /**
   * Undeploys a verticle component instance.
   */
  private void undeployVerticle(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    deploymentIDs.remove(instance.address(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          counter.fail(result.cause());
        } else if (result.result() != null) {
          cluster.undeployVerticle(result.result(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              unwatchInstance(instance, counter);
            }
          });
        } else {
          unwatchInstance(instance, counter);
        }
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

  /**
   * Handles a node joining the cluster.
   */
  private void doNodeJoined(final Node node) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        if (currentContext != null) {
          log.info(String.format("%s joined the cluster. Uploading components to new node", node.address()));
          installModules(node, currentContext, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                log.error(result.cause());
              } else {
                log.info(String.format("Successfully uploaded components to %s", node.address()));
              }
              task.complete();
            }
          });
        } else {
          task.complete();
        }
      }
    });
  }

  /**
   * Handles a node leaving the cluster.
   */
  private void doNodeLeft(final Node node) {
    tasks.runTask(new Handler<Task>() {
      @Override
      public void handle(final Task task) {
        if (currentContext != null) {
          log.info(String.format("%s left the cluster. Reassigning components", node.address()));
          deploymentNodes.keySet(new Handler<AsyncResult<Set<String>>>() {
            @Override
            public void handle(AsyncResult<Set<String>> result) {
              if (result.succeeded()) {
                final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(result.result().size());
                counter.setHandler(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    task.complete();
                  }
                });
  
                // If the instance was deployed on the node that left the cluster then
                // redeploy it on a new node.
                for (final String instanceAddress : result.result()) {
                  deploymentNodes.get(instanceAddress, new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> result) {
                      if (result.succeeded() && result.result().equals(node.address())) {
                        // Look up the current instance context in the cluster.
                        data.get(instanceAddress, new Handler<AsyncResult<String>>() {
                          @Override
                          public void handle(AsyncResult<String> result) {
                            if (result.succeeded() && result.result() != null) {
                              deployInstance(DefaultInstanceContext.fromJson(new JsonObject(result.result())), counter);
                            } else {
                              counter.succeed();
                            }
                          }
                        });
                      } else {
                        counter.succeed();
                      }
                    }
                  });
                }
              }
            }
          });
        } else {
          task.complete();
        }
      }
    });
  }

}
