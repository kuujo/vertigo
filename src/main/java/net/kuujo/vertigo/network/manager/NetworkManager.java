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
import static net.kuujo.vertigo.util.Config.parseCluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.impl.DefaultComponentContext;
import net.kuujo.vertigo.context.impl.DefaultInstanceContext;
import net.kuujo.vertigo.context.impl.DefaultNetworkContext;
import net.kuujo.vertigo.network.auditor.AuditorVerticle;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Vertigo network manager.
 *
 * @author Jordan Halterman
 */
public class NetworkManager extends BusModBase {
  private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
  private String address;
  private VertigoCluster cluster;
  private WatchableAsyncMap<String, String> data;
  private Set<String> ready = new HashSet<>();
  private NetworkContext currentContext;
  private final Map<String, Handler<MapEvent<String, String>>> watchHandlers = new HashMap<>();
  private final Handler<MapEvent<String, String>> watchHandler = new Handler<MapEvent<String, String>>() {
    @Override
    public void handle(MapEvent<String, String> event) {
      if (event.type().equals(MapEvent.Type.CREATE)) {
        handleCreate(DefaultNetworkContext.fromJson(new JsonObject(event.value())));
      } else if (event.type().equals(MapEvent.Type.UPDATE)) {
        handleUpdate(DefaultNetworkContext.fromJson(new JsonObject(event.value())));
      } else if (event.type().equals(MapEvent.Type.DELETE)) { 
        handleDelete(DefaultNetworkContext.fromJson(new JsonObject(event.value())));
      }
    }
  };

  @Override
  public void start(final Future<Void> startResult) {
    address = container.config().getString("address");
    if (address == null) {
      startResult.setFailure(new IllegalArgumentException("No address specified."));
      return;
    }

    try {
      cluster = parseCluster(container.config(), vertx, container);
    } catch (Exception e) {
      startResult.setFailure(e);
      return;
    }

    data = cluster.getMap(address);

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
                // Try to determine the current status of the network.
                for (ComponentContext<?> component : currentContext.components()) {
                  for (InstanceContext instance : component.instances()) {
                    data.containsKey(instance.status(), new Handler<AsyncResult<Boolean>>() {
                      @Override
                      public void handle(AsyncResult<Boolean> result) {
                        if (result.failed()) {
                          log.error(result.cause());
                        } else if (result.result()) {
                          handleReady(address);
                        } else {
                          handleUnready(address);
                        }
                      }
                    });
                  }
                }
              } else {
                NetworkManager.super.start(startResult);
              }
            }
          });
        }
      }
    });
  }

  /**
   * Handles the creation of the network.
   */
  private void handleCreate(final NetworkContext context) {
    currentContext = context;
    deployNetwork(context, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          log.error(result.cause());
        } else {
          log.info("Successfully deployed network " + context.address());
        }
      }
    });
  }

  /**
   * Handles the update of the network.
   */
  private void handleUpdate(final NetworkContext context) {
    if (currentContext != null) {
      final NetworkContext runningContext = currentContext;
      currentContext = context;

      // Undeploy any components that were removed from the network.
      final List<ComponentContext<?>> removedComponents = new ArrayList<>();
      for (ComponentContext<?> runningComponent : runningContext.components()) {
        if (context.component(runningComponent.name()) == null) {
          removedComponents.add(runningComponent);
        }
      }

      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(removedComponents.size());
      counter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            log.error(result.cause());
          } else {
            log.info("Successfully undeployed " + removedComponents.size() + " components");
          }

          // Deploy any components that were added to the network.
          final List<ComponentContext<?>> addedComponents = new ArrayList<>();
          for (ComponentContext<?> component : context.components()) {
            if (runningContext.component(component.name()) == null) {
              addedComponents.add(component);
            }
          }

          final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(addedComponents.size());
          counter.setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                log.error(result.cause());
              } else {
                log.info("Successfully deployed " + addedComponents.size() + " components");
                log.info("Updating network contexts");
                updateNetwork(currentContext, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      log.error(result.cause());
                    } else {
                      log.info("Successfully updated network contexts for " + address);
                    }
                  }
                });
              }
            }
          });
          deployComponents(addedComponents, counter);
        }
      });
      undeployComponents(removedComponents, counter);
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
          }
        }
      });
    }
  }

  /**
   * Handles the deletion of the network.
   */
  private void handleDelete(final NetworkContext context) {
    undeployNetwork(context, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          log.error(result.cause());
        } else {
          log.info("Successfully undeployed network " + context.address());
        }
      }
    });
  }

  /**
   * Called when a component instance is ready.
   */
  private void handleReady(String address) {
    ready.add(address);
    if (allReady()) {
      data.put(currentContext.status(), "ready");
    }
  }

  /**
   * Called when a component instance is unready.
   */
  private void handleUnready(String address) {
    ready.remove(address);
    if (!allReady()) {
      data.remove(currentContext.address());
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
    final CountingCompletionHandler<Void> complete = new CountingCompletionHandler<Void>(context.auditors().size());
    complete.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<NetworkContext>(result.cause()).setHandler(doneHandler);
        } else {
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
      }
    });
    deployAuditors(context.auditors(), context.messageTimeout(), complete);
  }

  /**
   * Deploys all network auditors.
   */
  private void deployAuditors(Set<String> auditors, long timeout, final CountingCompletionHandler<Void> complete) {
    for (String address : auditors) {
      cluster.deployVerticle(address, AuditorVerticle.class.getName(), new JsonObject().putString("address", address).putNumber("timeout", timeout), 1, new Handler<AsyncResult<String>>() {
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
   * Deploys a module component instance.
   */
  private void deployModule(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    cluster.deployModuleTo(instance.address(), instance.component().group(), instance.component().asModule().module(), buildConfig(instance, cluster), 1, new Handler<AsyncResult<String>>() {
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
   * Deploys a verticle component instance.
   */
  private void deployVerticle(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    cluster.deployVerticleTo(instance.address(), instance.component().group(), instance.component().asVerticle().main(),  buildConfig(instance, cluster), 1, new Handler<AsyncResult<String>>() {
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
   * Deploys a worker verticle deployment instance.
   */
  private void deployWorkerVerticle(final InstanceContext instance, final CountingCompletionHandler<Void> counter) {
    cluster.deployWorkerVerticleTo(instance.address(), instance.component().group(), instance.component().asVerticle().main(), buildConfig(instance, cluster), 1, instance.component().asVerticle().isMultiThreaded(), new Handler<AsyncResult<String>>() {
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
      }
    });
    undeployAuditors(context.auditors(), complete);
  }

  /**
   * Undeploys all network auditors.
   */
  private void undeployAuditors(Set<String> auditors, final CountingCompletionHandler<Void> complete) {
    for (String address : auditors) {
      cluster.undeployVerticle(address, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            complete.fail(result.cause());
          } else {
            complete.succeed();
          }
        }
      });
    }
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
   * Unwatches a component instance.
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
