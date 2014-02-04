/*
 * Copyright 2013 the original author or authors.
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import net.kuujo.vertigo.auditor.AuditorVerticle;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.context.impl.ContextBuilder;
import net.kuujo.vertigo.network.Network;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

/**
 * A base class for cluster implementations.
 *
 * @author Jordan Halterman
 */
abstract class AbstractCluster implements Cluster {
  protected final EventBus eventBus;
  protected final Logger logger;

  protected AbstractCluster(EventBus eventBus, Logger logger) {
    this.eventBus = eventBus;
    this.logger = logger;
  }

  /**
   * Deploys a verticle to the cluster.
   *
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle configuration.
   * @param instances
   *   The number of instances to deploy.
   * @param doneHandler
   *   A handler to be called once deployment is complete.
   */
  protected abstract void deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to specific nodes in the cluster.
   *
   * @param nodes
   *   A set of nodes to which to deploy the verticle.
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle configuration.
   * @param instances
   *   The number of instances to deploy.
   * @param doneHandler
   *   A handler to be called once deployment is complete.
   */
  protected abstract void deployVerticleTo(Set<String> nodes, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle configuration.
   * @param instances
   *   The number of instances to deploy.
   * @param multiThreaded
   *   Indicates whether the worker is multi-threaded.
   * @param doneHandler
   *   A handler to be called once deployment is complete.
   */
  protected abstract void deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to specific nodes in the cluster.
   *
   * @param nodes
   *   A set of nodes to which to deploy the verticle.
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle configuration.
   * @param instances
   *   The number of instances to deploy.
   * @param multiThreaded
   *   Indicates whether the worker is multi-threaded.
   * @param doneHandler
   *   A handler to be called once deployment is complete.
   */
  protected abstract void deployWorkerVerticleTo(Set<String> nodes, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to the cluster.
   *
   * @param moduleName
   *   The module name.
   * @param config
   *   The module configuration.
   * @param instances
   *   The number of instances to deploy.
   * @param doneHandler
   *   A handler to be called once deployment is complete.
   */
  protected abstract void deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to specific nodes in the cluster.
   *
   * @param nodes
   *   A set of nodes to which to deploy the module.
   * @param moduleName
   *   The module name.
   * @param config
   *   The module configuration.
   * @param instances
   *   The number of instances to deploy.
   * @param doneHandler
   *   A handler to be called once deployment is complete.
   */
  protected abstract void deployModuleTo(Set<String> nodes, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  @Override
  public void deployNetwork(Network network) {
    deployNetwork(network, null);
  }

  @Override
  public void deployNetwork(Network network, final Handler<AsyncResult<NetworkContext>> doneHandler) {
    final NetworkContext context = ContextBuilder.buildContext(network);
    final Future<NetworkContext> future = new DefaultFutureResult<NetworkContext>().setHandler(doneHandler);
    doDeployNetwork(context, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          future.setResult(context);
        }
      }
    });
  }

  /**
   * Deploys the network.
   */
  private void doDeployNetwork(final NetworkContext context, final Handler<AsyncResult<Void>> doneHandler) {
    doDeployNetwork(context, new DefaultFutureResult<Void>().setHandler(doneHandler));
  }

  /**
   * Iteratively deploys the network.
   */
  private void doDeployNetwork(final NetworkContext context, final Future<Void> future) {
    logger.info(String.format("Deploying network '%s'", context.address()));
    doDeployAuditors(context.auditors(), context.messageTimeout(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          doDeployComponents(context.componentContexts(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                future.setFailure(result.cause());
              }
              else {
                future.setResult((Void) null);
              }
            }
          });
        }
      }
    });
  }

  /**
   * Deploys all network auditors.
   */
  private void doDeployAuditors(final Collection<String> auditors, final long messageTimeout, final Handler<AsyncResult<Void>> doneHandler) {
    doDeployAuditors(auditors.iterator(), messageTimeout, new DefaultFutureResult<Void>().setHandler(doneHandler));
  }

  /**
   * Iteratively deploys network auditors.
   */
  private void doDeployAuditors(final Iterator<String> iterator, final long messageTimeout, final Future<Void> future) {
    if (iterator.hasNext()) {
      String address = iterator.next();
      logger.info(String.format("Deploying auditor '%s'", address));
      deployVerticle(AuditorVerticle.class.getName(), new JsonObject()
          .putString("address", address).putNumber("timeout", messageTimeout), 1, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            future.setFailure(result.cause());
          }
          else {
            doDeployAuditors(iterator, messageTimeout, future);
          }
        }
      });
    }
    else {
      future.setResult((Void) null);
    }
  }

  /**
   * Deploys all network components.
   */
  private void doDeployComponents(final Collection<ComponentContext<?>> components, final Handler<AsyncResult<Void>> doneHandler) {
    doDeployComponents(components.iterator(), new DefaultFutureResult<Void>().setHandler(doneHandler));
  }

  /**
   * Iteratively deploys network components.
   */
  private void doDeployComponents(final Iterator<ComponentContext<?>> iterator, final Future<Void> future) {
    if (iterator.hasNext()) {
      ComponentContext<?> context = iterator.next();
      logger.info(String.format("Deploying component '%s'", context.address()));
      doDeployInstances(context.instanceContexts(), new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            future.setFailure(result.cause());
          }
          else {
            doDeployComponents(iterator, future);
          }
        }
      });
    }
    else {
      future.setResult((Void) null);
    }
  }

  /**
   * Deploys all component instances.
   */
  @SuppressWarnings("rawtypes")
  private <T extends Component> void doDeployInstances(final Collection<InstanceContext<T>> instances, final Handler<AsyncResult<Void>> doneHandler) {
    doDeployInstances(instances.iterator(), new DefaultFutureResult<Void>().setHandler(doneHandler));
  }

  /**
   * Iteratively deploys component instances.
   */
  @SuppressWarnings("rawtypes")
  private <T extends Component> void doDeployInstances(final Iterator<InstanceContext<T>> iterator, final Future<Void> future) {
    if (iterator.hasNext()) {
      InstanceContext<T> context = iterator.next();
      logger.info(String.format("Deploying '%s' instance %d", context.componentContext().address(), context.number()));
      if (context.componentContext().isModule()) {
        deployModule(context.<ModuleContext>componentContext().module(), InstanceContext.toJson(context), 1, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            if (result.failed()) {
              future.setFailure(result.cause());
            }
            else {
              doDeployInstances(iterator, future);
            }
          }
        });
      }
      else if (((VerticleContext) context.componentContext()).isWorker()) {
        deployWorkerVerticle(context.<VerticleContext>componentContext().main(), InstanceContext.toJson(context), 1,
            context.<VerticleContext>componentContext().isMultiThreaded(), new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            if (result.failed()) {
              future.setFailure(result.cause());
            }
            else {
              doDeployInstances(iterator, future);
            }
          }
        });
      }
      else {
        deployVerticle(context.<VerticleContext>componentContext().main(), InstanceContext.toJson(context), 1,
            new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            if (result.failed()) {
              future.setFailure(result.cause());
            }
            else {
              doDeployInstances(iterator, future);
            }
          }
        });
      }
    }
    else {
      future.setResult((Void) null);
    }
  }

  @Override
  public void deploy(Network network) {
    deployNetwork(network, null);
  }

  @Override
  public void deploy(Network network, Handler<AsyncResult<NetworkContext>> doneHandler) {
    deployNetwork(network, doneHandler);
  }

  @Override
  public void shutdownNetwork(NetworkContext context) {
    shutdownNetwork(context, null);
  }

  @Override
  public void shutdownNetwork(final NetworkContext context, Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    doUndeployComponents(context.componentContexts(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          final Throwable cause = result.cause();
          doUndeployAuditors(context.auditors(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              future.setFailure(cause);
            }
          });
        }
        else {
          doUndeployAuditors(context.auditors(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                future.setFailure(result.cause());
              }
              else {
                future.setResult((Void) null);
              }
            }
          });
        }
      }
    });
  }

  @Override
  public void shutdown(NetworkContext context) {
    shutdownNetwork(context, null);
  }

  @Override
  public void shutdown(NetworkContext context, Handler<AsyncResult<Void>> doneHandler) {
    shutdownNetwork(context, doneHandler);
  }

  /**
   * Undeploys network components.
   */
  private void doUndeployComponents(final Collection<ComponentContext<?>> components, final Handler<AsyncResult<Void>> doneHandler) {
    doUndeployComponents(components.iterator(), null, new DefaultFutureResult<Void>().setHandler(doneHandler));
  }

  /**
   * Iteratively undeploys network components. If an error occurs while attempting
   * to undeploy a component, we still attempt to undeploy all of the components
   * before failing the shutdown.
   */
  private void doUndeployComponents(final Iterator<ComponentContext<?>> iterator, final Throwable cause, final Future<Void> future) {
    if (iterator.hasNext()) {
      ComponentContext<?> context = iterator.next();
      doUndeployInstances(context.instanceContexts(), new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            doUndeployComponents(iterator, result.cause(), future);
          }
          else {
            doUndeployComponents(iterator, cause, future);
          }
        }
      });
    }
    else if (cause != null) {
      future.setFailure(cause);
    }
    else {
      future.setResult((Void) null);
    }
  }

  /**
   * Undeploys component instances.
   */
  @SuppressWarnings("rawtypes")
  private <T extends Component> void doUndeployInstances(final Collection<InstanceContext<T>> instances, final Handler<AsyncResult<Void>> doneHandler) {
    doUndeployInstances(instances.iterator(), null, new DefaultFutureResult<Void>().setHandler(doneHandler));
  }

  /**
   * Iteratively undeploys component instances. If an error occurs while attempting
   * to undeploy an instance, we still attempt to undeploy all of the instances
   * before failing the shutdown.
   */
  @SuppressWarnings("rawtypes")
  private <T extends Component> void doUndeployInstances(final Iterator<InstanceContext<T>> iterator, final Throwable cause, final Future<Void> future) {
    if (iterator.hasNext()) {
      eventBus.sendWithTimeout(iterator.next().id(), new JsonObject().putString("action", "shutdown"), 10000, new Handler<AsyncResult<Message<JsonObject>>>() {
        @Override
        public void handle(AsyncResult<Message<JsonObject>> result) {
          if (result.failed()) {
            doUndeployInstances(iterator, result.cause(), future);
          }
          else {
            doUndeployInstances(iterator, cause, future);
          }
        }
      });
    }
    else if (cause != null) {
      future.setFailure(cause);
    }
    else {
      future.setResult((Void) null);
    }
  }

  /**
   * Undeploys network auditors.
   */
  private void doUndeployAuditors(final Collection<String> auditors, final Handler<AsyncResult<Void>> doneHandler) {
    doUndeployAuditors(auditors.iterator(), null, new DefaultFutureResult<Void>().setHandler(doneHandler));
  }

  /**
   * Iteratively undeploys network auditors. If an error occurs while attempting
   * to undeploy an auditor, we still attempt to undeploy all of the auditors
   * before failing the shutdown.
   */
  private void doUndeployAuditors(final Iterator<String> iterator, final Throwable cause, final Future<Void> future) {
    if (iterator.hasNext()) {
      eventBus.sendWithTimeout(iterator.next(), new JsonObject().putString("action", "shutdown"), 10000, new Handler<AsyncResult<Message<JsonObject>>>() {
        @Override
        public void handle(AsyncResult<Message<JsonObject>> result) {
          if (result.failed()) {
            doUndeployAuditors(iterator, result.cause(), future);
          }
          else {
            doUndeployAuditors(iterator, cause, future);
          }
        }
      });
    }
    else if (cause != null) {
      future.setFailure(cause);
    }
    else {
      future.setResult((Void) null);
    }
  }

}
