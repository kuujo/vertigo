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
package net.kuujo.vertigo.coordinator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.auditor.AuditorVerticle;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.events.Events;
import net.kuujo.vertigo.serializer.SerializationException;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * An abstract network coordinator.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings({"unchecked", "rawtypes"})
abstract class AbstractCoordinator extends BusModBase implements Handler<Message<JsonObject>> {
  protected NetworkContext context;
  protected Logger logger;
  protected Events events;
  protected Map<String, String> deploymentMap = new HashMap<>();
  protected Map<String, InstanceContext<?>> contextMap = new HashMap<>();
  protected Set<String> instances = new HashSet<>();
  protected Set<String> auditorDeploymentIds = new HashSet<>();

  @Override
  public void start() {
    super.start();
    events = new Events(eb);
    context = NetworkContext.fromJson(config);
    logger = LoggerFactory.getLogger(String.format("%s-%s", getClass().getName(), context.address()));
    eb.registerHandler(context.address(), this);
    doDeploy();
  }

  /**
   * Deploys a verticle.
   *
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle configuration.
   */
  protected abstract void deployVerticle(String main, JsonObject config);

  /**
   * Deploys a verticle.
   *
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle configuration.
   * @param doneHandler
   *   An async handler to be invoked once complete.
   */
  protected abstract void deployVerticle(String main, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle.
   *
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle configuration.
   * @param multiThreaded
   *   Whether the verticle is multi-threaded.
   */
  protected abstract void deployWorkerVerticle(String main, JsonObject config, boolean multiThreaded);

  /**
   * Deploys a worker verticle.
   *
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle configuration.
   * @param multiThreaded
   *   Whether the verticle is multi-threaded.
   * @param doneHandler
   *   An async handler to be called once complete.
   */
  protected abstract void deployWorkerVerticle(String main, JsonObject config, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module.
   *
   * @param moduleName
   *   The module name.
   * @param config
   *   The module configuration.
   */
  protected abstract void deployModule(String moduleName, JsonObject config);

  /**
   * Deploys a module.
   *
   * @param moduleName
   *   The module name.
   * @param config
   *   The module configuration.
   * @param doneHandler
   *   An async handler to be invoked once complete.
   */
  protected abstract void deployModule(String moduleName, JsonObject config, Handler<AsyncResult<String>> doneHandler);

  /**
   * Undeploys a verticle.
   *
   * @param deploymentId
   *   The deployment ID.
   */
  protected abstract void undeployVerticle(String deploymentId);

  /**
   * Undeploys a verticle.
   *
   * @param deploymentId
   *   The deployment ID.
   * @param doneHandler
   *   An async handler to be invoked once complete.
   */
  protected abstract void undeployVerticle(String deploymentId, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a module.
   *
   * @param deploymentId
   *   The deployment ID.
   */
  protected abstract void undeployModule(String deploymentId);

  /**
   * Undeploys a module.
   *
   * @param deploymentId
   *   The deployment ID.
   * @param doneHandler
   *   An async handler to be invoked once complete.
   */
  protected abstract void undeployModule(String deploymentId, Handler<AsyncResult<Void>> doneHandler);

  @Override
  public void handle(Message<JsonObject> message) {
    String action = getMandatoryString("action", message);
    if (action != null) {
      switch (action) {
        case "shutdown":
          doShutdown(message);
          break;
        default:
          sendError(message, String.format("Invalid action %s.", action));
          break;
      }
    }
  }

  /**
   * Deploys the network.
   */
  private void doDeploy() {
    if (context.isAckingEnabled()) {
      recursiveDeployAuditors(copyList(context.auditors()), new DefaultFutureResult<Void>().setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            logger.error("Failed to deploy auditor verticle.", result.cause());
            container.exit();
          }
          else {
            new RecursiveDeployer(context).deploy(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  container.logger().error("Failed to deploy network.", result.cause());
                  container.exit();
                }
                else {
                  events.trigger(Events.Network.Deploy.class, context.address(), context);
                }
              }
            });
          }
        }
      }));
    }
    else {
      new RecursiveDeployer(context).deploy(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            container.logger().error("Failed to deploy network.", result.cause());
            container.exit();
          }
          else {
            events.trigger(Events.Network.Deploy.class, context.address(), context);
          }
        }
      });
    }
  }

  /**
   * Copies a list to a new list.
   */
  private static <T> List<T> copyList(List<T> list) {
    List<T> newList = new ArrayList<>();
    for (T item : list) {
      newList.add(item);
    }
    return newList;
  }

  /**
   * Recursively deploys network auditors.
   */
  private void recursiveDeployAuditors(final List<String> auditors, final Future<Void> future) {
    if (auditors.size() > 0) {
      final String address = auditors.iterator().next();
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("Deploying network auditor: %s", address));
      }
      JsonObject auditorConfig = new JsonObject()
        .putString(AuditorVerticle.ADDRESS, address)
        .putNumber(AuditorVerticle.TIMEOUT, context.messageTimeout());
      deployVerticle(AuditorVerticle.class.getName(), auditorConfig, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            future.setFailure(result.cause());
          }
          else {
            auditorDeploymentIds.add(result.result());
            auditors.remove(address);
            if (auditors.size() > 0) {
              recursiveDeployAuditors(auditors, future);
            }
            else {
              future.setResult(null);
            }
          }
        }
      });
    }
  }

  /**
   * Shuts down the network.
   */
  private void doShutdown(final Message<JsonObject> message) {
    new RecursiveDeployer(context).undeploy(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        for (String address : auditorDeploymentIds) {
          undeployVerticle(address);
        }
        events.trigger(Events.Network.Shutdown.class, context.address(), context);
        message.reply(result.succeeded());
      }
    });
  }

  /**
   * Recursively deploys all network components.
   *
   * @author Jordan Halterman
   */
  private class RecursiveDeployer {

    private NetworkContext context;

    public RecursiveDeployer(NetworkContext context) {
      this.context = context;
    }

    /**
     * Deploys the network.
     *
     * @param doneHandler
     *   A handler to be invoked once the network is deployed.
     */
    public void deploy(Handler<AsyncResult<Void>> doneHandler) {
      List<ComponentContext> components = new ArrayList<>();
      for (ComponentContext<?> component : context.components()) {
        components.add(component);
      }
      RecursiveComponentDeployer deployer = new RecursiveComponentDeployer(components);
      deployer.deploy(doneHandler);
    }

    /**
     * Undeploys the network.
     *
     * @param doneHandler
     *   A handler to be invoked once the network is undeployed.
     */
    public void undeploy(Handler<AsyncResult<Void>> doneHandler) {
      List<ComponentContext> components = new ArrayList<>();
      for (ComponentContext<?> component : context.components()) {
        components.add(component);
      }
      RecursiveComponentDeployer deployer = new RecursiveComponentDeployer(components);
      deployer.undeploy(doneHandler);
    }

    /**
     * An abstract context deployer.
     *
     * @param <T> The context type.
     */
    private abstract class RecursiveContextDeployer<T> {
      protected Iterator<T> iterator;
      protected Future<Void> future;

      protected Handler<AsyncResult<String>> assignHandler = new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.succeeded()) {
            if (iterator.hasNext()) {
              doDeploy(iterator.next(), assignHandler);
            }
            else {
              future.setResult(null);
            }
          }
          else {
            future.setFailure(result.cause());
          }
        }
      };

      protected Handler<AsyncResult<Void>> releaseHandler = new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.succeeded()) {
            if (iterator.hasNext()) {
              doUndeploy(iterator.next(), releaseHandler);
            }
            else {
              future.setResult(null);
            }
          }
          else {
            future.setFailure(result.cause());
          }
        }
      };

      public RecursiveContextDeployer(Collection<T> contexts) {
        this.iterator = contexts.iterator();
      }

      /**
       * Deploys a network.
       *
       * @param doneHandler
       *   The handler to invoke once deployment is complete.
       */
      public void deploy(Handler<AsyncResult<Void>> doneHandler) {
        this.future = new DefaultFutureResult<Void>();
        future.setHandler(doneHandler);
        if (iterator.hasNext()) {
          doDeploy(iterator.next(), assignHandler);
        }
        else {
          future.setResult(null);
        }
      }

      /**
       * Undeploys a network.
       *
       * @param doneHandler
       *   The handler to invoke once deployment is complete.
       */
      public void undeploy(Handler<AsyncResult<Void>> doneHandler) {
        this.future = new DefaultFutureResult<Void>();
        future.setHandler(doneHandler);
        if (iterator.hasNext()) {
          doUndeploy(iterator.next(), releaseHandler);
        }
        else {
          future.setResult(null);
        }
      }

      /**
       * Deploys a context.
       */
      protected abstract void doDeploy(T context, Handler<AsyncResult<String>> doneHandler);

      /**
       * Undeploys a context.
       */
      protected abstract void doUndeploy(T context, Handler<AsyncResult<Void>> doneHandler);

    }

    /**
     * A network component deployer.
     */
    private class RecursiveComponentDeployer extends RecursiveContextDeployer<ComponentContext> {

      public RecursiveComponentDeployer(Collection<ComponentContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doDeploy(ComponentContext context, Handler<AsyncResult<String>> resultHandler) {
        if (logger.isInfoEnabled()) {
          logger.info(String.format("Deploying component %s", context.address()));
        }
        final Future<String> future = new DefaultFutureResult<String>().setHandler(resultHandler);
        RecursiveInstanceDeployer deployer = new RecursiveInstanceDeployer(context.instances());
        deployer.deploy(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.succeeded()) {
              future.setResult("");
            }
            else {
              future.setFailure(result.cause());
            }
          }
        });
      }

      @Override
      protected void doUndeploy(ComponentContext context, Handler<AsyncResult<Void>> doneHandler) {
        if (logger.isInfoEnabled()) {
          logger.info(String.format("Undeploying component %s", context.address()));
        }
        final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
        RecursiveInstanceDeployer executor = new RecursiveInstanceDeployer(context.instances());
        executor.undeploy(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.succeeded()) {
              future.setResult(null);
            }
            else {
              future.setFailure(result.cause());
            }
          }
        });
      }
    }

    /**
     * A network component instance deployer.
     */
    private class RecursiveInstanceDeployer extends RecursiveContextDeployer<InstanceContext<?>> {

      public RecursiveInstanceDeployer(Collection<InstanceContext<?>> contexts) {
        super(contexts);
      }

      @Override
      protected void doDeploy(final InstanceContext<?> context, Handler<AsyncResult<String>> resultHandler) {
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Deploying instance %s", context.address()));
        }
        instances.add(context.address());

        final Future<String> future = new DefaultFutureResult<String>();
        contextMap.put(context.address(), context);
        future.setHandler(resultHandler);

        JsonObject config;
        try {
          config = InstanceContext.toJson(context);
        }
        catch (SerializationException e) {
          future.setFailure(e);
          return;
        }

        if (context.component().isVerticle()) {
          if (context.<VerticleContext>component().isWorker()) {
            deployWorkerVerticle(context.<VerticleContext>component().main(), config, context.<VerticleContext>component().isMultiThreaded(), new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                  deploymentMap.put(context.address(), result.result());
                  future.setResult(result.result());
                }
                else {
                  events.trigger(Events.Component.Deploy.class, context.component().address(), context);
                  future.setFailure(result.cause());
                }
              }
            });
          }
          else {
            deployVerticle(context.<VerticleContext>component().main(), config, new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                  deploymentMap.put(context.address(), result.result());
                  future.setResult(result.result());
                }
                else {
                  events.trigger(Events.Component.Deploy.class, context.component().address(), context);
                  future.setFailure(result.cause());
                }
              }
            });
          }
        }
        else if (context.component().isModule()) {
          deployModule(context.<ModuleContext>component().module(), config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.succeeded()) {
                deploymentMap.put(context.address(), result.result());
                future.setResult(result.result());
              }
              else {
                events.trigger(Events.Component.Deploy.class, context.component().address(), context);
                future.setFailure(result.cause());
              }
            }
          });
        }
      }

      @Override
      protected void doUndeploy(final InstanceContext<?> context, Handler<AsyncResult<Void>> resultHandler) {
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Undeploying instance %s", context.address()));
        }
        final Future<Void> future = new DefaultFutureResult<Void>();
        future.setHandler(resultHandler);
        String id = context.address();
        if (deploymentMap.containsKey(id)) {
          String deploymentID = deploymentMap.get(id);
          if (context.component().isVerticle()) {
            undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  future.setFailure(result.cause());
                }
                else {
                  events.trigger(Events.Component.Shutdown.class, context.component().address(), context);
                  future.setResult(result.result());
                }
              }
            });
          }
          else if (context.component().isModule()) {
            undeployModule(deploymentID, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  future.setFailure(result.cause());
                }
                else {
                  events.trigger(Events.Component.Shutdown.class, context.component().address(), context);
                  future.setResult(result.result());
                }
              }
            });
          }
        }
      }
    }

  }

}