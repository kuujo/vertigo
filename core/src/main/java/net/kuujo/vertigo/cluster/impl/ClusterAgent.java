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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.Config;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.impl.ContextBuilder;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;
import net.kuujo.vertigo.network.manager.NetworkManager;
import net.kuujo.vertigo.util.Configs;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.serialization.SerializationException;
import net.kuujo.vertigo.util.serialization.Serializer;
import net.kuujo.vertigo.util.serialization.SerializerFactory;
import net.kuujo.xync.Xync;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Vertigo cluster agent bus verticle.<p>
 *
 * If deploying this verticle directly (and not as a module) the
 * verticle should always be deployed as a worker verticle. If the
 * current Vert.x instance is a Hazelcast clustered instance, the
 * cluster agent will use Hazelcast to coordinate deployments.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ClusterAgent extends Xync {
  private static final Serializer configSerializer = SerializerFactory.getSerializer(Config.class);

  @Override
  protected void handleClusterMessage(final Message<JsonObject> message) {
    String action = message.body().getString("action");
    if (action != null) {
      switch (action) {
        case "list":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doListNetworks(message);
          } else {
            super.handleClusterMessage(message);
          }
          break;
        case "check":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doIsDeployedNetwork(message);
          } else {
            super.handleClusterMessage(message);
          }
          break;
        case "load":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doLoadNetwork(message);
          } else {
            super.handleClusterMessage(message);
          }
          break;
        case "deploy":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doDeployNetwork(message);
          } else {
            super.handleClusterMessage(message);
          }
          break;
        case "undeploy":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doUndeployNetwork(message);
          } else {
            super.handleClusterMessage(message);
          }
          break;
        default:
          super.handleClusterMessage(message);
      }
    }
  }

  @Override
  protected void handleNodeMessage(final Message<JsonObject> message) {
    String action = message.body().getString("action");
    if (action != null) {
      switch (action) {
        case "list":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doListNetworks(message);
          } else {
            super.handleClusterMessage(message);
          }
          break;
        case "check":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doIsDeployedNetwork(message);
          } else {
            super.handleNodeMessage(message);
          }
          break;
        case "load":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doLoadNetwork(message);
          } else {
            super.handleNodeMessage(message);
          }
          break;
        case "deploy":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doDeployNetwork(message);
          } else {
            super.handleNodeMessage(message);
          }
          break;
        case "undeploy":
          if (message.body().containsField("type") && message.body().getString("type").equals("network")) {
            doUndeployNetwork(message);
          } else {
            super.handleNodeMessage(message);
          }
          break;
        default:
          super.handleNodeMessage(message);
      }
    }
  }

  /**
   * Checks whether a network is deployed.
   */
  private void doIsDeployedNetwork(final Message<JsonObject> message) {
    String network = message.body().getString("network");
    if (network == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No network name specified."));
    } else {
      String scontext = manager.<String, String>getMap(String.format("%s.%s", cluster, network)).get(String.format("%s.%s", cluster, network));
      if (scontext == null) {
        message.reply(new JsonObject().putString("status", "ok").putBoolean("result", false));
      } else {
        NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(scontext));
        platform.isDeployed(context.address(), new Handler<AsyncResult<Boolean>>() {
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
   * Loads all network configurations.
   */
  private void doListNetworks(final Message<JsonObject> message) {
    Set<String> networks = manager.<String>getSet(String.format("cluster.%s.networks", cluster));
    JsonArray results = new JsonArray();
    for (String network : networks) {
      String scontext = manager.<String, String>getMap(String.format("%s.%s", cluster, network)).get(String.format("%s.%s", cluster, network));
      if (scontext != null) {
        results.add(new JsonObject(scontext));
      }
    }
    message.reply(new JsonObject().putString("status", "ok").putArray("result", results));
  }

  /**
   * Loads a network configuration.
   */
  private void doLoadNetwork(final Message<JsonObject> message) {
    String network = message.body().getString("network");
    if (network == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No network name specified."));
    } else {
      String scontext = manager.<String, String>getMap(String.format("%s.%s", cluster, network)).get(String.format("%s.%s", cluster, network));
      if (scontext == null) {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Network not deployed."));
      } else {
        message.reply(new JsonObject().putString("status", "ok").putObject("result", new JsonObject(scontext)));
      }
    }
  }

  /**
   * Handles deployment of a network.
   */
  private void doDeployNetwork(final Message<JsonObject> message) {
    Object network = message.body().getValue("network");
    if (network != null) {
      if (network instanceof String) {
        doDeployNetwork((String) network, message);
      } else if (network instanceof JsonObject) {
        JsonObject jsonNetwork = (JsonObject) network;
        try {
          NetworkConfig config = configSerializer.deserializeObject(jsonNetwork, NetworkConfig.class);
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
    doDeployNetwork(new DefaultNetworkConfig(name), message);
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
    String scontext = manager.<String, String>getMap(String.format("%s.%s", cluster, network.getName())).get(String.format("%s.%s", cluster, network.getName()));

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

    // If the network's manager isn't already deployed in the network's cluster
    // then deploy the manager.
    platform.isDeployed(context.address(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else if (result.result()) {
          // If the network manager is already deployed in the network's cluster then
          // simply merge and update the network's configuration.
          doDeployNetwork(context, message);
        } else {
          // If the network manager hasn't yet been deployed then deploy the manager
          // and then update the network's configuration.
          platform.deployVerticleAs(context.address(), NetworkManager.class.getName(), new JsonObject().putString("cluster", address).putString("address", context.address()), 1, true, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
              } else {
                manager.<String>getSet(String.format("cluster.%s.networks", cluster)).add(context.name());
                doDeployNetwork(context, message);
              }
            }
          });
        }
      }
    });
  }

  /**
   * Deploys a network from context.
   */
  private void doDeployNetwork(final NetworkContext context, final Message<JsonObject> message) {
    final WrappedWatchableMap<String, String> data = new WrappedWatchableMap<String, String>(context.address(), manager.<String, String>getMap(context.address()), vertx);

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
   * Handles undeployment of a network.
   */
  private void doUndeployNetwork(final Message<JsonObject> message) {
    Object network = message.body().getValue("network");
    if (network != null) {
      if (network instanceof String) {
        doUndeployNetwork((String) network, message);
      } else if (network instanceof JsonObject) {
        JsonObject jsonNetwork = (JsonObject) network;
        try {
          NetworkConfig config = configSerializer.deserializeObject(jsonNetwork, NetworkConfig.class);
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
    final WrappedWatchableMap<String, String> data = new WrappedWatchableMap<String, String>(String.format("%s.%s", cluster, name), manager.<String, String>getMap(String.format("%s.%s", cluster, name)), vertx);
    String scontext = data.get(String.format("%s.%s", cluster, name));
    if (scontext == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Network is not deployed."));
    } else {
      // Determine whether the network's manager is deployed.
      final NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(scontext));
      platform.isDeployed(context.address(), new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else if (!result.result()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "Network is not deployed."));
          } else {
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
                      platform.undeployVerticleAs(context.address(), new Handler<AsyncResult<Void>>() {
                        @Override
                        public void handle(AsyncResult<Void> result) {
                          if (result.failed()) {
                            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                          } else {
                            // We can be nice and unset the status key since the manager was undeployed :-)
                            manager.<String>getSet(String.format("cluster.%s.networks", cluster)).remove(context.name());
                            data.remove(context.status());
                            message.reply(new JsonObject().putString("status", "ok"));
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
                  data.remove(context.address());
                }
              }
            });
          }
        }
      });
    }
  }

  /**
   * Undeploys a network by configuration.
   */
  private void doUndeployNetwork(final NetworkConfig network, final Message<JsonObject> message) {
    // First we need to load the network's context from the cluster.
    final WrappedWatchableMap<String, String> data = new WrappedWatchableMap<String, String>(String.format("%s.%s", cluster, network.getName()), manager.<String, String>getMap(String.format("%s.%s", cluster, network.getName())), vertx);
    String scontext = data.get(String.format("%s.%s", cluster, network.getName()));
    if (scontext == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Network is not deployed."));
    } else {
      // Determine whether the network's manager is deployed.
      final NetworkContext context = DefaultNetworkContext.fromJson(new JsonObject(scontext));
      platform.isDeployed(context.address(), new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else if (!result.result()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", "Network is not deployed."));
          } else {
            // If the network's manager is deployed then unmerge the given
            // configuration from the configuration of the network that's
            // already running. If the resulting configuration is a no-component
            // network then that indicates that the entire network is being undeployed.
            // Otherwise, we simply update the existing configuration and allow the
            // network's manager to handle deployment and undeployment of components.
            NetworkConfig updatedConfig = Configs.unmergeNetworks(context.config(), network);
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
                        platform.undeployVerticleAs(context.address(), new Handler<AsyncResult<Void>>() {
                          @Override
                          public void handle(AsyncResult<Void> result) {
                            if (result.failed()) {
                              message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                            } else {
                              // We can be nice and unset the status key since the manager was undeployed :-)
                              data.remove(context.status());
                              message.reply(new JsonObject().putString("status", "ok"));
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
                    data.remove(context.address());
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
                    data.put(context.address(), DefaultNetworkContext.toJson(context).encode());
                  }
                }
              });
            }
          }
        }
      });
    }
  }

  private static class WrappedWatchableMap<K, V> implements Map<K, V> {
    private final String name;
    private final Map<K, V> map;
    private final EventBus eventBus;
    private final Map<MapEvent.Type, Map<Handler<MapEvent<K, V>>, Handler<Message<JsonObject>>>> watchHandlers = new HashMap<>();

    public WrappedWatchableMap(String name, Map<K, V> map, Vertx vertx) {
      this.name = name;
      this.map = map;
      this.eventBus = vertx.eventBus();
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
      return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
      return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
      return map.get(key);
    }

    @Override
    public V put(K key, V value) {
      V result = map.put(key, value);
      eventBus.publish(String.format("%s.%s.%s", name, key, MapEvent.Type.CHANGE.toString()), new JsonObject()
          .putString("type", MapEvent.Type.CHANGE.toString())
          .putValue("key", key)
          .putValue("value", value));
      String event = result == null ? MapEvent.Type.CREATE.toString() : MapEvent.Type.UPDATE.toString();
      eventBus.publish(String.format("%s.%s.%s", name, key, event), new JsonObject()
          .putString("type", event)
          .putValue("key", key)
          .putValue("value", value));
      return result;
    }

    @Override
    public V remove(Object key) {
      V result = map.remove(key);
      eventBus.publish(String.format("%s.%s.%s", name, key, MapEvent.Type.CHANGE.toString()), new JsonObject()
          .putString("type", MapEvent.Type.CHANGE.toString())
          .putValue("key", key)
          .putValue("value", result));
      eventBus.publish(String.format("%s.%s.%s", name, key, MapEvent.Type.DELETE.toString()), new JsonObject()
          .putString("type", MapEvent.Type.DELETE.toString())
          .putValue("key", key)
          .putValue("value", result));
      return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
      map.putAll(m);
    }

    @Override
    public void clear() {
      map.clear();
    }

    @Override
    public Set<K> keySet() {
      return map.keySet();
    }

    @Override
    public Collection<V> values() {
      return map.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
      return map.entrySet();
    }

    public void watch(final K key, final MapEvent.Type event, final Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
      if (event == null) {
        final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(4).setHandler(doneHandler);
        addWatcher(key, MapEvent.Type.CHANGE, handler, counter);
        addWatcher(key, MapEvent.Type.CREATE, handler, counter);
        addWatcher(key, MapEvent.Type.UPDATE, handler, counter);
        addWatcher(key, MapEvent.Type.DELETE, handler, counter);
      } else {
        addWatcher(key, event, handler, doneHandler);
      }
    }

    private void addWatcher(K key, final MapEvent.Type event, final Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
      Map<Handler<MapEvent<K, V>>, Handler<Message<JsonObject>>> watchHandlers = this.watchHandlers.get(event);
      if (watchHandlers == null) {
        watchHandlers = new HashMap<>();
        this.watchHandlers.put(event, watchHandlers);
      }

      Handler<Message<JsonObject>> wrappedHandler;
      if (!watchHandlers.containsKey(handler)) {
        wrappedHandler = new Handler<Message<JsonObject>>() {
          @Override
          public void handle(Message<JsonObject> message) {
            K key = message.body().getValue("key");
            V value = message.body().getValue("value");
            handler.handle(new MapEvent<K, V>(event, key, value));
          }
        };
        watchHandlers.put(handler, wrappedHandler);
      } else {
        wrappedHandler = watchHandlers.get(handler);
      }
      eventBus.registerHandler(String.format("%s.%s.%s", name, key, event.toString()), wrappedHandler, doneHandler);
    }

    public void unwatch(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
      if (event == null) {
        final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(4).setHandler(doneHandler);
        removeWatcher(key, MapEvent.Type.CHANGE, handler, counter);
        removeWatcher(key, MapEvent.Type.CREATE, handler, counter);
        removeWatcher(key, MapEvent.Type.UPDATE, handler, counter);
        removeWatcher(key, MapEvent.Type.DELETE, handler, counter);
      } else {
        removeWatcher(key, event, handler, doneHandler);
      }
    }

    private void removeWatcher(K key, MapEvent.Type event, Handler<MapEvent<K, V>> handler, Handler<AsyncResult<Void>> doneHandler) {
      Map<Handler<MapEvent<K, V>>, Handler<Message<JsonObject>>> watchHandlers = this.watchHandlers.get(event);
      if (watchHandlers == null || !watchHandlers.containsKey(handler)) {
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      } else {
        Handler<Message<JsonObject>> wrappedHandler = watchHandlers.remove(handler);
        eventBus.unregisterHandler(String.format("%s.%s.%s", name, key, event.toString()), wrappedHandler, doneHandler);
      }
    }

  }

}
