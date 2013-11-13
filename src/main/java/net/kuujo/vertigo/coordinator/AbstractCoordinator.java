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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kuujo.vertigo.auditor.Auditor;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.events.Events;
import net.kuujo.vertigo.heartbeat.DefaultHeartbeatMonitor;
import net.kuujo.vertigo.heartbeat.HeartbeatMonitor;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

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
  protected Events events;
  protected Map<String, String> deploymentMap = new HashMap<>();
  protected Map<String, InstanceContext> contextMap = new HashMap<>();
  protected Map<String, HeartbeatMonitor> heartbeats = new HashMap<>();
  protected Set<String> workers = new HashSet<>();
  protected Map<String, Message<JsonObject>> ready = new HashMap<>();
  protected Set<String> auditorDeploymentIds = new HashSet<>();

  @Override
  public void start() {
    super.start();
    events = new Events(eb);
    try {
      context = Serializer.deserialize(config);
    }
    catch (SerializationException e) {
      logger.error(e);
    }
    eb.registerHandler(context.getAddress(), this);
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
    recursiveDeployAuditors(context.getAuditors(), new DefaultFutureResult<Void>().setHandler(new Handler<AsyncResult<Void>>() {
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
                events.trigger(Events.Network.Deploy.class, context.getAddress(), context);
              }
            }
          });
        }
      }
    }));
  }

  /**
   * Recursively deploys network auditors.
   */
  private void recursiveDeployAuditors(final List<String> auditors, final Future<Void> future) {
    if (auditors.size() > 0) {
      final String address = auditors.iterator().next();
      JsonObject auditorConfig = new JsonObject().putString("address", address)
        .putString("broadcast", context.getBroadcastAddress())
        .putBoolean("enabled", context.isAckingEnabled())
        .putNumber("expire", context.getAckExpire())
        .putNumber("delay", context.getAckDelay());
      deployVerticle(Auditor.class.getName(), auditorConfig, new Handler<AsyncResult<String>>() {
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
  private void doRedeploy(final String id) {
    if (deploymentMap.containsKey(id)) {
      String deploymentID = deploymentMap.get(id);
      undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          deploymentMap.remove(id);
          if (contextMap.containsKey(id)) {
            final InstanceContext context = contextMap.get(id);
            if (context.getComponent().isModule()) {
              deployModule(((ModuleContext) context.getComponent()).getModule(), Serializer.serialize(context), new Handler<AsyncResult<String>>() {
                @Override
                public void handle(AsyncResult<String> result) {
                  if (result.succeeded()) {
                    deploymentMap.put(context.id(), result.result());
                  }
                  else {
                    container.logger().error(String.format("Failed to deploy %s instance %s.", context.getComponent().getAddress(), context.id()));
                  }
                }
              });
            }
            else if (context.getComponent().isVerticle()) {
              deployVerticle(((VerticleContext) context.getComponent()).getMain(), Serializer.serialize(context), new Handler<AsyncResult<String>>() {
                @Override
                public void handle(AsyncResult<String> result) {
                  if (result.succeeded()) {
                    deploymentMap.put(context.id(), result.result());
                  }
                  else {
                    container.logger().error(String.format("Failed to deploy %s instance %s.", context.getComponent().getAddress(), context.id()));
                  }
                }
              });
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
        events.trigger(Events.Network.Shutdown.class, context.getAddress(), context);
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
    String id = getMandatoryString("id", message);
    if (id != null) {
      ready.put(id, message);
      InstanceContext context = contextMap.get(id);
      events.trigger(Events.Component.Start.class, context.getComponent().getAddress(), context);
      if (ready.size() == workers.size()) {
        events.trigger(Events.Network.Start.class, this.context.getAddress(), this.context);
        for (Message<JsonObject> replyMessage : ready.values()) {
          replyMessage.reply();
        }
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
      Collection<ComponentContext> components = context.getComponents();
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
      Collection<ComponentContext> components = context.getComponents();
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
        final Future<String> future = new DefaultFutureResult<String>().setHandler(resultHandler);
        RecursiveInstanceDeployer deployer = new RecursiveInstanceDeployer(context.getInstances());
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
        final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
        RecursiveInstanceDeployer executor = new RecursiveInstanceDeployer(context.getInstances());
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
    private class RecursiveInstanceDeployer extends RecursiveContextDeployer<InstanceContext> {

      public RecursiveInstanceDeployer(Collection<InstanceContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doDeploy(final InstanceContext context, Handler<AsyncResult<String>> resultHandler) {
        workers.add(context.id());

        final Future<String> future = new DefaultFutureResult<String>();
        contextMap.put(context.id(), context);
        future.setHandler(resultHandler);

        JsonObject config = context.getComponent().getConfig();
        if (config == null) {
          config = new JsonObject();
        }
        config.putObject("__context__", Serializer.serialize(context));

        if (context.getComponent().isVerticle()) {
          deployVerticle(((VerticleContext) context.getComponent()).getMain(), config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.succeeded()) {
                deploymentMap.put(context.id(), result.result());
                future.setResult(result.result());
              }
              else {
                events.trigger(Events.Component.Deploy.class, context.getComponent().getAddress(), context);
                future.setFailure(result.cause());
              }
            }
          });
        }
        else if (context.getComponent().isModule()) {
          deployModule(((ModuleContext) context.getComponent()).getModule(), config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.succeeded()) {
                deploymentMap.put(context.id(), result.result());
                future.setResult(result.result());
              }
              else {
                events.trigger(Events.Component.Deploy.class, context.getComponent().getAddress(), context);
                future.setFailure(result.cause());
              }
            }
          });
        }
      }

      @Override
      protected void doUndeploy(final InstanceContext context, Handler<AsyncResult<Void>> resultHandler) {
        final Future<Void> future = new DefaultFutureResult<Void>();
        future.setHandler(resultHandler);
        String id = context.id();
        if (deploymentMap.containsKey(id)) {
          String deploymentID = deploymentMap.get(id);
          if (context.getComponent().isVerticle()) {
            undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  future.setFailure(result.cause());
                }
                else {
                  events.trigger(Events.Component.Shutdown.class, context.getComponent().getAddress(), context);
                  future.setResult(result.result());
                }
              }
            });
          }
          else if (context.getComponent().isModule()) {
            undeployModule(deploymentID, new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  future.setFailure(result.cause());
                }
                else {
                  events.trigger(Events.Component.Shutdown.class, context.getComponent().getAddress(), context);
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
