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
package net.kuujo.vertigo.network.impl;

import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.component.ModuleConfig;
import net.kuujo.vertigo.component.VerticleConfig;
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.io.selector.Selector;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.util.Observer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Default active network implementation.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultActiveNetwork implements ActiveNetwork, Observer<NetworkContext> {
  private NetworkConfig network;
  private ClusterManager cluster;

  public DefaultActiveNetwork(NetworkConfig network, ClusterManager cluster) {
    this.network = network;
    this.cluster = cluster;
  }

  @Override
  public void update(NetworkContext context) {
    network = context.config();
  }

  @Override
  public NetworkConfig getConfig() {
    return network;
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(T component) {
    return addComponent(component, null);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(T component, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    network.addComponent(component);
    cluster.deployNetwork(network, doneHandler);
    return component;
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain) {
    return addComponent(name, moduleOrMain, null, 1, null);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config) {
    return addComponent(name, moduleOrMain, config, 1, null);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, int instances) {
    return addComponent(name, moduleOrMain, null, instances, null);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config, int instances) {
    return addComponent(name, moduleOrMain, config, instances, null);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addComponent(name, moduleOrMain, null, 1, doneHandler);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addComponent(name, moduleOrMain, config, 1, doneHandler);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, int instances, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addComponent(name, moduleOrMain, null, instances, doneHandler);
  }

  @Override
  public <T extends ComponentConfig<T>> T addComponent(String name, String moduleOrMain, JsonObject config, int instances, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    T component = network.addComponent(name, moduleOrMain, config, instances);
    cluster.deployNetwork(network, doneHandler);
    return component;
  }

  @Override
  public <T extends ComponentConfig<T>> T removeComponent(T component) {
    return removeComponent(component.getName(), null);
  }

  @Override
  public <T extends ComponentConfig<T>> T removeComponent(T component, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return removeComponent(component.getName(), doneHandler);
  }

  @Override
  public <T extends ComponentConfig<T>> T removeComponent(String name) {
    return removeComponent(name, null);
  }

  @Override
  public <T extends ComponentConfig<T>> T removeComponent(String name, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // Undeploy a single component by creating a copy of the network with the
    // component as its only element. When then network is undeployed, the component
    // will be removed from the network.
    T component = network.removeComponent(name);
    NetworkConfig undeploy = new DefaultNetworkConfig(network.getName());
    undeploy.addComponent(component);
    cluster.undeployNetwork(undeploy, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<ActiveNetwork>(DefaultActiveNetwork.this).setHandler(doneHandler);
        }
      }
    });
    return component;
  }

  @Override
  public ModuleConfig addModule(ModuleConfig module) {
    return addModule(module, null);
  }

  @Override
  public ModuleConfig addModule(ModuleConfig module, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    network.addModule(module);
    cluster.deployNetwork(network, doneHandler);
    return module;
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName) {
    return addModule(name, moduleName, null, 1, null);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config) {
    return addModule(name, moduleName, config, 1, null);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, int instances) {
    return addModule(name, moduleName, null, instances, null);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config, int instances) {
    return addModule(name, moduleName, config, instances, null);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addModule(name, moduleName, null, 1, doneHandler);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addModule(name, moduleName, config, 1, doneHandler);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, int instances, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addModule(name, moduleName, null, instances, doneHandler);
  }

  @Override
  public ModuleConfig addModule(String name, String moduleName, JsonObject config, int instances, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    ModuleConfig module = network.addModule(name, moduleName, config, instances);
    cluster.deployNetwork(network, doneHandler);
    return module;
  }

  @Override
  public ModuleConfig removeModule(ModuleConfig module) {
    return removeModule(module.getName(), null);
  }

  @Override
  public ModuleConfig removeModule(ModuleConfig module, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return removeModule(module.getName(), doneHandler);
  }

  @Override
  public ModuleConfig removeModule(String name) {
    return removeModule(name, null);
  }

  @Override
  public ModuleConfig removeModule(String name, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // Undeploy a single module by creating a copy of the network with the
    // module as its only element. When then network is undeployed, the module
    // will be removed from the network.
    ModuleConfig module = network.removeModule(name);
    NetworkConfig undeploy = new DefaultNetworkConfig(network.getName());
    undeploy.addModule(module);
    cluster.undeployNetwork(undeploy, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<ActiveNetwork>(DefaultActiveNetwork.this).setHandler(doneHandler);
        }
      }
    });
    return module;
  }

  @Override
  public VerticleConfig addVerticle(VerticleConfig verticle) {
    return addVerticle(verticle, null);
  }

  @Override
  public VerticleConfig addVerticle(VerticleConfig verticle, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    network.addVerticle(verticle);
    cluster.deployNetwork(network, doneHandler);
    return verticle;
  }

  @Override
  public VerticleConfig addVerticle(String name, String main) {
    return addVerticle(name, main, null, 1, null);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config) {
    return addVerticle(name, main, config, 1, null);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, int instances) {
    return addVerticle(name, main, null, instances, null);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config, int instances) {
    return addVerticle(name, main, config, instances, null);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addVerticle(name, main, null, 1, doneHandler);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addVerticle(name, main, config, 1, doneHandler);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, int instances, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return addVerticle(name, main, null, instances, doneHandler);
  }

  @Override
  public VerticleConfig addVerticle(String name, String main, JsonObject config, int instances, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    VerticleConfig verticle = network.addVerticle(name, main, config, instances);
    cluster.deployNetwork(network, doneHandler);
    return verticle;
  }

  @Override
  public VerticleConfig removeVerticle(VerticleConfig verticle) {
    return removeVerticle(verticle, null);
  }

  @Override
  public VerticleConfig removeVerticle(VerticleConfig verticle, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return removeVerticle(verticle, doneHandler);
  }

  @Override
  public VerticleConfig removeVerticle(String name) {
    return removeVerticle(name, null);
  }

  @Override
  public VerticleConfig removeVerticle(String name, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // Undeploy a single verticle by creating a copy of the network with the
    // verticle as its only element. When then network is undeployed, the verticle
    // will be removed from the network.
    VerticleConfig verticle = network.removeVerticle(name);
    NetworkConfig undeploy = new DefaultNetworkConfig(network.getName());
    undeploy.addVerticle(verticle);
    cluster.undeployNetwork(undeploy, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<ActiveNetwork>(DefaultActiveNetwork.this).setHandler(doneHandler);
        }
      }
    });
    return verticle;
  }

  @Override
  public ConnectionConfig createConnection(ConnectionConfig connection) {
    return createConnection(connection, null);
  }

  @Override
  public ConnectionConfig createConnection(String source, String target) {
    ConnectionConfig connection = network.createConnection(source, target);
    cluster.deployNetwork(network);
    return connection;
  }

  @Override
  public ConnectionConfig createConnection(String source, String target, Selector selector) {
    return createConnection(source, target, selector, null);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in) {
    return createConnection(source, out, target, in, null, null);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in, Selector selector) {
    return createConnection(source, out, target, in, selector, null);
  }

  @Override
  public ConnectionConfig createConnection(ConnectionConfig connection, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    network.createConnection(connection);
    cluster.deployNetwork(network, doneHandler);
    return connection;
  }

  @Override
  public ConnectionConfig createConnection(String source, String target, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return createConnection(source, target, doneHandler);
  }

  @Override
  public ConnectionConfig createConnection(String source, String target, Selector selector, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    ConnectionConfig connection = network.createConnection(source, target, selector);
    cluster.deployNetwork(network, doneHandler);
    return connection;
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    return createConnection(source, out, target, in, null, doneHandler);
  }

  @Override
  public ConnectionConfig createConnection(String source, String out, String target, String in, Selector selector, Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    ConnectionConfig connection = network.createConnection(source, out, target, in, selector);
    cluster.deployNetwork(network, doneHandler);
    return connection;
  }

  @Override
  public ActiveNetwork destroyConnection(ConnectionConfig connection) {
    return destroyConnection(connection, null);
  }

  @Override
  public ActiveNetwork destroyConnection(ConnectionConfig connection, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
     // Undeploy a single connection by creating a copy of the network with the
    // connection as its only element. When then network is undeployed, the connection
    // will be removed from the network.
    network.destroyConnection(connection);
    NetworkConfig undeploy = new DefaultNetworkConfig(network.getName());
    undeploy.createConnection(connection);
    cluster.undeployNetwork(undeploy, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<ActiveNetwork>(DefaultActiveNetwork.this).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ActiveNetwork destroyConnection(String source, String target) {
    return destroyConnection(source, target, null);
  }

  @Override
  public ActiveNetwork destroyConnection(String source, String out, String target, String in) {
    return destroyConnection(source, out, target, in, null);
  }

  @Override
  public ActiveNetwork destroyConnection(String source, String target, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // Undeploy a single connection by creating a copy of the network with the
    // connection as its only element. When then network is undeployed, the connection
    // will be removed from the network.
    network.destroyConnection(source, target);
    NetworkConfig undeploy = new DefaultNetworkConfig(network.getName());
    undeploy.createConnection(source, target);
    cluster.undeployNetwork(undeploy, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<ActiveNetwork>(DefaultActiveNetwork.this).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  @Override
  public ActiveNetwork destroyConnection(String source, String out, String target, String in, final Handler<AsyncResult<ActiveNetwork>> doneHandler) {
    // Undeploy a single connection by creating a copy of the network with the
    // connection as its only element. When then network is undeployed, the connection
    // will be removed from the network.
    network.destroyConnection(source, target);
    NetworkConfig undeploy = new DefaultNetworkConfig(network.getName());
    undeploy.createConnection(source, out, target, in);
    cluster.undeployNetwork(undeploy, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<ActiveNetwork>(result.cause()).setHandler(doneHandler);
        }
        else {
          new DefaultFutureResult<ActiveNetwork>(DefaultActiveNetwork.this).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

}
