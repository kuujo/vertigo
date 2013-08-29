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
package com.blankstyle.vine.local;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.BasicFeeder;
import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.RootException;
import com.blankstyle.vine.context.SeedContext;
import com.blankstyle.vine.context.VineContext;
import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.definition.MalformedDefinitionException;
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.eventbus.vine.VineVerticle;

/**
 * A local root implementation.
 *
 * This root does not communicate with a remote root and seeds are not assigned
 * to specific machines. Seeds are simply started using the local Vert.x
 * container.
 *
 * @author Jordan Halterman
 */
public class LocalRoot implements Root {

  protected static final String VINE_VERTICLE_CLASS = VineVerticle.class.getName();

  protected String address = "vine.root";

  protected Vertx vertx;

  protected Container container;

  private Map<String, String> deploymentMap = new HashMap<String, String>();

  public LocalRoot(String address, Vertx vertx, Container container) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
  }

  public LocalRoot(String address) {
    this.address = address;
  }

  public LocalRoot(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public Root setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public Root setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public Root setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  public Container getContainer() {
    return container;
  }

  @Override
  public void deploy(VineDefinition definition, final Handler<AsyncResult<Feeder>> feedHandler) throws MalformedDefinitionException {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    future.setHandler(feedHandler);

    final String address = definition.getAddress();
    if (deploymentMap.containsKey(address)) {
      future.setFailure(new RootException("Cannot redeploy vine."));
      return;
    }

    // Create a vine context.
    VineContext context = definition.createContext();
    RecursiveDeployer deployer = new RecursiveDeployer(context);
    deployer.deploy(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          deploymentMap.put(address, address);
          future.setResult(new BasicFeeder(address, vertx.eventBus()));
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

  /**
   * Recursively deploys all vine elements.
   *
   * @author Jordan Halterman
   */
  private class RecursiveDeployer {

    VineContext context;

    public RecursiveDeployer(VineContext context) {
      this.context = context;
    }

    /**
     * Deploys the vine.
     *
     * @param doneHandler
     *   A handler to be invoked once the vine is deployed.
     */
    public void deploy(Handler<AsyncResult<Void>> doneHandler) {
      Collection<SeedContext> seeds = context.getSeedContexts();
      RecursiveSeedDeployer deployer = new RecursiveSeedDeployer(seeds);
      deployer.deploy(doneHandler);
    }

    /**
     * An abstract context deployer.
     *
     * @param <T> The context type.
     */
    private abstract class RecursiveContextDeployer<T> {
      protected Iterator<T> iterator;
      protected Future<Void> future;

      protected Handler<AsyncResult<Boolean>> handler = new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.succeeded()) {
            if (iterator.hasNext()) {
              doDeploy(iterator.next(), handler);
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
       * Deploys the context, invoking a handler when complete.
       *
       * @param doneHandler
       *   The handler to invoke once deployment is complete.
       */
      public void deploy(Handler<AsyncResult<Void>> doneHandler) {
        this.future = new DefaultFutureResult<Void>();
        future.setHandler(doneHandler);
        if (iterator.hasNext()) {
          doDeploy(iterator.next(), handler);
        }
        else {
          future.setResult(null);
        }
      }

      /**
       * Performs the deployment of a single context.
       *
       * @param context
       *   The context to deploy.
       * @param doneHandler
       *   A handler to be invoked once deployment is complete.
       */
      protected abstract void doDeploy(T context, Handler<AsyncResult<Boolean>> doneHandler);

    }

    /**
     * A vine seed deployer.
     */
    private class RecursiveSeedDeployer extends RecursiveContextDeployer<SeedContext> {

      public RecursiveSeedDeployer(Collection<SeedContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doDeploy(SeedContext context, Handler<AsyncResult<Boolean>> resultHandler) {
        final Future<Boolean> future = new DefaultFutureResult<Boolean>();
        future.setHandler(resultHandler);
        Collection<WorkerContext> workers = context.getWorkerContexts();
        RecursiveWorkerDeployer deployer = new RecursiveWorkerDeployer(workers);
        deployer.deploy(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.succeeded()) {
              future.setResult(true);
            }
            else {
              future.setFailure(result.cause());
            }
          }
        });
      }
    }

    /**
     * A vine worker deployer.
     */
    private class RecursiveWorkerDeployer extends RecursiveContextDeployer<WorkerContext> {

      public RecursiveWorkerDeployer(Collection<WorkerContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doDeploy(WorkerContext context, Handler<AsyncResult<Boolean>> resultHandler) {
        final Future<Boolean> future = new DefaultFutureResult<Boolean>();
        future.setHandler(resultHandler);
        container.deployWorkerVerticle(context.getContext().getDefinition().getMain(), context.serialize(), 1, false, new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            if (result.succeeded()) {
              future.setResult(true);
            }
            else {
              future.setFailure(result.cause());
            }
          }
        });
      }
    }

  }

}
