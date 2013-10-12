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
package net.kuujo.vertigo.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.WorkerContext;
import net.kuujo.vertigo.definition.ComponentDefinition;
import net.kuujo.vertigo.heartbeat.DefaultHeartbeatMonitor;
import net.kuujo.vertigo.heartbeat.HeartbeatMonitor;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * An abstract network coordinator.
 *
 * @author Jordan Halterman
 */
abstract class AbstractCoordinator extends BusModBase implements Handler<Message<JsonObject>> {

  protected NetworkContext context;

  protected Map<String, String> deploymentMap = new HashMap<>();

  protected Map<String, WorkerContext> contextMap = new HashMap<>();

  protected Map<String, HeartbeatMonitor> heartbeats = new HashMap<>();

  protected Set<String> workers = new HashSet<>();

  protected Map<String, Message<JsonObject>> ready = new HashMap<>();

  protected String authDeploymentId;

  @Override
  public void start() {
    super.start();
    context = new NetworkContext(config);
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
    switch (action) {
      case "register":
        doRegister(message);
        break;
      case "unregister":
        doUnregister(message);
        break;
      case "shutdown":
        doShutdown(message);
        break;
      case "redeploy":
        doRedeployAll(message);
        break;
      case "ready":
        doReady(message);
        break;
      default:
        sendError(message, String.format("Invalid action %s.", action));
        break;
    }
  }

  /**
   * Deploys the network.
   */
  private void doDeploy() {
    // TODO: The number of ackers is configurable, but the auditor
    // verticle needs to be refactored to better support this.
    // So, for now we just deploy a single auditor verticle.
    JsonObject ackConfig = new JsonObject().putString("address", context.auditAddress())
      .putString("broadcast", context.broadcastAddress())
      .putBoolean("enabled", context.getDefinition().ackingEnabled())
      .putNumber("expire", context.getDefinition().ackExpire());

    deployVerticle(Auditor.class.getName(), ackConfig, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            logger.error("Failed to deploy authenticator verticle.", result.cause());
            container.exit();
          }
          else {
            authDeploymentId = result.result();
            new RecursiveDeployer(context).deploy(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  container.logger().error("Failed to deploy network.", result.cause());
                  container.exit();
                }
              }
            });
          }
        }
    });
  }

  /**
   * Creates a unique heartbeat address.
   */
  private String createHeartbeatAddress() {
    return UUID.randomUUID().toString();
  }

  /**
   * Registers a heartbeat.
   */
  private void doRegister(Message<JsonObject> message) {
    final String address = getMandatoryString("address", message);
    String heartbeatAddress = createHeartbeatAddress();
    HeartbeatMonitor monitor = new DefaultHeartbeatMonitor(heartbeatAddress, vertx);
    monitor.listen(new Handler<String>() {
      @Override
      public void handle(String heartbeatAddress) {
        if (heartbeats.containsKey(address)) {
          heartbeats.remove(address);
          doRedeploy(address);
        }
      }
    });
    heartbeats.put(address, monitor);
    message.reply(heartbeatAddress);
  }

  /**
   * Unregisters a heartbeat.
   */
  private void doUnregister(Message<JsonObject> message) {
    final String address = getMandatoryString("address", message);
    if (heartbeats.containsKey(address)) {
      HeartbeatMonitor monitor = heartbeats.get(address);
      monitor.unlisten();
      heartbeats.remove(address);
    }
    doRedeploy(address);
  }

  /**
   * Redeploys a worker.
   */
  private void doRedeploy(final String address) {
    if (deploymentMap.containsKey(address)) {
      String deploymentID = deploymentMap.get(address);
      undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          deploymentMap.remove(address);
          if (contextMap.containsKey(address)) {
            final WorkerContext context = contextMap.get(address);
            deployVerticle(context.getComponentContext().getDefinition().main(), context.serialize(), new Handler<AsyncResult<String>>() {
              @Override
              public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                  deploymentMap.put(context.address(), result.result());
                }
                else {
                  container.logger().error("Failed to redeploy worker at " + address + ".");
                }
              }
            });
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
        if (authDeploymentId != null) {
          undeployVerticle(authDeploymentId);
        }
        message.reply(result.succeeded());
      }
    });
  }

  /**
   * Redeploys the entire network.
   */
  private void doRedeployAll(final Message<JsonObject> message) {
    new RecursiveDeployer(context).undeploy(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        new RecursiveDeployer(context).deploy(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            message.reply(result.succeeded());
          }
        });
      }
    });
  }

  /**
   * Indicates that a component instance is ready.
   */
  private void doReady(Message<JsonObject> message) {
    ready.put(getMandatoryString("address", message), message);
    if (ready.size() == workers.size()) {
      for (Message<JsonObject> replyMessage : ready.values()) {
        replyMessage.reply();
      }
    }
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
      Collection<ComponentContext> components = context.getComponentContexts();
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
      Collection<ComponentContext> components = context.getComponentContexts();
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
        final Future<String> future = new DefaultFutureResult<String>();
        future.setHandler(resultHandler);
        Collection<WorkerContext> workers = context.getWorkerContexts();
        RecursiveWorkerDeployer deployer = new RecursiveWorkerDeployer(workers);
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
        final Future<Void> future = new DefaultFutureResult<Void>();
        future.setHandler(doneHandler);
        Collection<WorkerContext> workers = context.getWorkerContexts();
        RecursiveWorkerDeployer executor = new RecursiveWorkerDeployer(workers);
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
     * A network worker deployer.
     */
    private class RecursiveWorkerDeployer extends RecursiveContextDeployer<WorkerContext> {

      public RecursiveWorkerDeployer(Collection<WorkerContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doDeploy(final WorkerContext context, Handler<AsyncResult<String>> resultHandler) {
        workers.add(context.address());

        final Future<String> future = new DefaultFutureResult<String>();
        contextMap.put(context.address(), context);
        future.setHandler(resultHandler);

        ComponentDefinition definition = context.getComponentContext().getDefinition();
        if (definition.type().equals(ComponentDefinition.VERTICLE)) {
          deployVerticle(definition.main(), context.serialize(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.succeeded()) {
                deploymentMap.put(context.address(), result.result());
                future.setResult(result.result());
              }
              else {
                future.setFailure(result.cause());
              }
            }
          });
        }
        else if (definition.type().equals(ComponentDefinition.MODULE)) {
          deployModule(definition.module(), context.serialize(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.succeeded()) {
                deploymentMap.put(context.address(), result.result());
                future.setResult(result.result());
              }
              else {
                future.setFailure(result.cause());
              }
            }
          });
        }
      }

      @Override
      protected void doUndeploy(WorkerContext context, Handler<AsyncResult<Void>> resultHandler) {
        final Future<Void> future = new DefaultFutureResult<Void>();
        future.setHandler(resultHandler);
        String address = context.address();
        if (deploymentMap.containsKey(address)) {
          String deploymentID = deploymentMap.get(address);
          ComponentDefinition definition = context.getComponentContext().getDefinition();
          if (definition.type().equals(ComponentDefinition.VERTICLE)) {
            undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  future.setFailure(result.cause());
                }
                else {
                  future.setResult(result.result());
                }
              }
            });
          }
          else if (definition.type().equals(ComponentDefinition.MODULE)) {
            undeployModule(deploymentID, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  future.setFailure(result.cause());
                }
                else {
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
