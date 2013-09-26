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
package net.kuujo.vevent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.kuujo.vevent.context.ComponentContext;
import net.kuujo.vevent.context.NetworkContext;
import net.kuujo.vevent.context.WorkerContext;
import net.kuujo.vevent.definition.ComponentDefinition;
import net.kuujo.vevent.definition.MalformedDefinitionException;
import net.kuujo.vevent.definition.NetworkDefinition;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * An abstract cluster.
 *
 * @author Jordan Halterman
 */
abstract class AbstractCluster implements Cluster {

  protected net.kuujo.via.cluster.Cluster cluster;

  // A map of worker addresses to deployment IDs.
  private Map<String, String> deploymentMap = new HashMap<String, String>();

  @Override
  public void deploy(NetworkDefinition network,
      Handler<AsyncResult<NetworkContext>> doneHandler) {
    final NetworkContext context;
    final Future<NetworkContext> future = new DefaultFutureResult<NetworkContext>().setHandler(doneHandler);
    try {
      context = network.createContext();
      RecursiveDeployer deployer = new RecursiveDeployer(context);
      deployer.deploy(new Handler<AsyncResult<Void>>() {
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
    } catch (MalformedDefinitionException e) {
      future.setFailure(e);
    }
  }

  @Override
  public void deploy(NetworkDefinition network, long timeout,
      Handler<AsyncResult<NetworkContext>> doneHandler) {
    deploy(network, doneHandler);
  }

  @Override
  public void shutdown(NetworkContext context) {
    RecursiveDeployer deployer = new RecursiveDeployer(context);
    deployer.undeploy(null);
  }

  @Override
  public void shutdown(NetworkContext context,
      Handler<AsyncResult<Void>> doneHandler) {
    RecursiveDeployer deployer = new RecursiveDeployer(context);
    deployer.undeploy(doneHandler);
  }

  @Override
  public void shutdown(NetworkContext context, long timeout,
      Handler<AsyncResult<Void>> doneHandler) {
    shutdown(context, doneHandler);
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
        final Future<String> future = new DefaultFutureResult<String>();
        future.setHandler(resultHandler);

        ComponentDefinition definition = context.getContext().getDefinition();
        cluster.deployVerticle(definition.getMain(), context.serialize(), new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            if (result.succeeded()) {
              deploymentMap.put(context.getAddress(), result.result());
              future.setResult(result.result());
            }
            else {
              future.setFailure(result.cause());
            }
          }
        });
      }

      @Override
      protected void doUndeploy(WorkerContext context, Handler<AsyncResult<Void>> resultHandler) {
        final Future<Void> future = new DefaultFutureResult<Void>();
        future.setHandler(resultHandler);
        String address = context.getAddress();
        if (deploymentMap.containsKey(address)) {
          String deploymentID = deploymentMap.get(address);
          cluster.undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
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
