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
import java.util.Iterator;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.BasicFeeder;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.VineException;
import com.blankstyle.vine.context.SeedContext;
import com.blankstyle.vine.context.StemContext;
import com.blankstyle.vine.context.VineContext;
import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.definition.MalformedDefinitionException;
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.eventbus.Actions;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.WrappedReliableEventBus;
import com.blankstyle.vine.eventbus.stem.StemVerticle;
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

  protected static final String STEM_VERTICLE_CLASS = StemVerticle.class.getName();

  protected static final long DEFAULT_TIMEOUT = 15000;

  protected String address = "vine.root";

  protected Vertx vertx;

  protected Container container;

  protected ReliableEventBus eventBus;

  protected String stemAddress;

  public LocalRoot(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
    EventBus eventBus = vertx.eventBus();
    if (eventBus instanceof ReliableEventBus) {
      this.eventBus = (ReliableEventBus) eventBus;
    }
    else {
      this.eventBus = new WrappedReliableEventBus(eventBus, vertx);
    }
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
  public void deploy(final VineDefinition vine, Handler<AsyncResult<Feeder>> handler) {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    future.setHandler(handler);

    // Deploy a local stem which will monitor the vine.
    deployLocalStem(vine, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        // If the result succeeded, deploy all seed workers via the stem.
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          // Create a vine context and deploy each seed.
          final String stemAddress = result.result();
          final VineContext context;
          try {
            context = vine.createContext();

            // The recursive executor will recursively execute the "assign"
            // command on all vine context elements.
            RecursiveExecutor executor = new RecursiveExecutor("assign", context, stemAddress);
            executor.execute(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> deployResult) {
                if (deployResult.succeeded()) {
                  // Once all the seed workers have been deployed, deploy the
                  // vine verticle for feeding the seeds.
                  deployVineVerticle(context, stemAddress, future);
                }
                else {
                  future.setFailure(deployResult.cause());
                }
              }
            });
          }
          catch (MalformedDefinitionException e) {
            future.setFailure(e);
          }
        }
      }
    });
  }

  @Override
  public void deploy(final VineDefinition vine, final long timeout, Handler<AsyncResult<Feeder>> handler) {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    future.setHandler(handler);

    // Deploy a local stem which will monitor the vine.
    deployLocalStem(vine, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        // If the result succeeded, deploy all seed workers via the stem.
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          // Create a vine context and deploy each seed.
          final String stemAddress = result.result();
          final VineContext context;
          try {
            context = vine.createContext();

            // The recursive executor will recursively execute the "assign"
            // command on all vine context elements.
            RecursiveExecutor executor = new RecursiveExecutor("assign", context, stemAddress).setTimeout(timeout);
            executor.execute(new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> deployResult) {
                if (deployResult.succeeded()) {
                  // Once all the seed workers have been deployed, deploy the
                  // vine verticle for feeding the seeds.
                  deployVineVerticle(context, stemAddress, future);
                }
                else {
                  future.setFailure(deployResult.cause());
                }
              }
            });
          }
          catch (MalformedDefinitionException e) {
            future.setFailure(e);
          }
        }
      }
    });
  }

  /**
   * Deploys a vine verticle.
   *
   * @param context
   *   The vine context.
   * @param stemAddress
   *   The stem address.
   * @param future
   *   A future result to be invoked with a vine reference.
   */
  private void deployVineVerticle(final VineContext context, final String stemAddress, final Future<Feeder> future) {
    container.deployVerticle(VINE_VERTICLE_CLASS, context.serialize(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          // Add the vine context to a shared map of contexts so that
          // vine worker addresses can be referenced for shutdown.
          storeContext(context);
          future.setResult(new BasicFeeder(context.getAddress(), eventBus, vertx));
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

  @Override
  public void shutdown(String address) {
    shutdown(address, DEFAULT_TIMEOUT, null);
  }

  @Override
  public void shutdown(String address, Handler<AsyncResult<Void>> doneHandler) {
    shutdown(address, DEFAULT_TIMEOUT, doneHandler);
  }

  @Override
  public void shutdown(String address, long timeout, Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    if (doneHandler != null) {
      future.setHandler(doneHandler);
    }

    final VineContext context = loadContext(address);
    if (context == null) {
      future.setFailure(new VineException("Invalid vine address."));
    }
    else if (stemAddress == null) {
      future.setFailure(new VineException("No local stem deployed."));
    }
    else {
      RecursiveExecutor executor = new RecursiveExecutor("release", context, stemAddress).setTimeout(timeout);
      executor.execute(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> deployResult) {
          if (deployResult.succeeded()) {
            // Add the vine context to a shared map of contexts so that
            // vine worker addresses can be referenced for shutdown.
            removeContext(context);
            future.setResult(null);
          }
          else {
            future.setFailure(deployResult.cause());
          }
        }
      });
    }
  }

  /**
   * Deploys a local stem whose sole task is to monitor this vine.
   */
  private void deployLocalStem(VineDefinition definition, Handler<AsyncResult<String>> handler) {
    // If a stem has already been deployed then return the stem address.
    if (stemAddress != null) {
      new DefaultFutureResult<String>().setHandler(handler).setResult(stemAddress);
    }
    else {
      final Future<String> future = new DefaultFutureResult<String>().setHandler(handler);
      final String address = String.format("%s.stem", definition.getAddress());
  
      // Create a stem context with an address specific to the vine being deployed.
      container.deployVerticle(STEM_VERTICLE_CLASS, new StemContext(new JsonObject().putString("address", address)).serialize(), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.succeeded()) {
            stemAddress = address;
            future.setResult(address);
          }
          else {
            future.setFailure(result.cause());
          }
        }
      });
    }
  }

  private void storeContext(VineContext context) {
    ConcurrentSharedMap<String, String> contexts = vertx.sharedData().getMap("vine.contexts");
    contexts.put(context.getAddress(), context.serialize().encode());
  }

  private VineContext loadContext(String address) {
    ConcurrentSharedMap<String, String> contexts = vertx.sharedData().getMap("vine.contexts");
    String encoded = contexts.get(address);
    if (encoded != null) {
      return new VineContext(new JsonObject(encoded));
    }
    return null;
  }

  private void removeContext(VineContext context) {
    ConcurrentSharedMap<String, String> contexts = vertx.sharedData().getMap("vine.contexts");
    contexts.remove(context.getAddress());
  }

  /**
   * Recursively deploys all vine elements.
   *
   * @author Jordan Halterman
   */
  private class RecursiveExecutor {

    String action;

    VineContext context;

    String address;

    long timeout;

    public RecursiveExecutor(String action, VineContext context, String address) {
      this.action = action;
      this.context = context;
      this.address = address;
    }

    /**
     * Sets the executor timeout.
     */
    public RecursiveExecutor setTimeout(long timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * Deploys the vine.
     *
     * @param doneHandler
     *   A handler to be invoked once the vine is deployed.
     */
    public void execute(Handler<AsyncResult<Void>> doneHandler) {
      Collection<SeedContext> seeds = context.getSeedContexts();
      RecursiveSeedExecutor executor = new RecursiveSeedExecutor(seeds);
      executor.execute(doneHandler);
    }

    /**
     * An abstract context deployer.
     *
     * @param <T> The context type.
     */
    private abstract class RecursiveContextExecutor<T> {
      protected Iterator<T> iterator;
      protected Future<Void> future;

      protected Handler<AsyncResult<Boolean>> handler = new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.succeeded()) {
            if (iterator.hasNext()) {
              doExecute(iterator.next(), handler);
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

      public RecursiveContextExecutor(Collection<T> contexts) {
        this.iterator = contexts.iterator();
      }

      /**
       * Executes a context action.
       *
       * @param doneHandler
       *   The handler to invoke once deployment is complete.
       */
      public void execute(Handler<AsyncResult<Void>> doneHandler) {
        this.future = new DefaultFutureResult<Void>();
        future.setHandler(doneHandler);
        if (iterator.hasNext()) {
          doExecute(iterator.next(), handler);
        }
        else {
          future.setResult(null);
        }
      }

      /**
       * Performs the execution of an action for a single context.
       *
       * @param action
       *   The action to execute.
       * @param context
       *   The context to deploy.
       * @param doneHandler
       *   A handler to be invoked once deployment is complete.
       */
      protected abstract void doExecute(T context, Handler<AsyncResult<Boolean>> doneHandler);

    }

    /**
     * A vine seed deployer.
     */
    private class RecursiveSeedExecutor extends RecursiveContextExecutor<SeedContext> {

      public RecursiveSeedExecutor(Collection<SeedContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doExecute(SeedContext context, Handler<AsyncResult<Boolean>> resultHandler) {
        final Future<Boolean> future = new DefaultFutureResult<Boolean>();
        future.setHandler(resultHandler);
        Collection<WorkerContext> workers = context.getWorkerContexts();
        RecursiveWorkerExecutor executor = new RecursiveWorkerExecutor(workers);
        executor.execute(new Handler<AsyncResult<Void>>() {
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
    private class RecursiveWorkerExecutor extends RecursiveContextExecutor<WorkerContext> {

      public RecursiveWorkerExecutor(Collection<WorkerContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doExecute(WorkerContext context, Handler<AsyncResult<Boolean>> resultHandler) {
        final Future<Boolean> future = new DefaultFutureResult<Boolean>();
        future.setHandler(resultHandler);

        if (timeout > 0) {
          eventBus.send(address, Actions.create(action, context.serialize()), timeout, new AsyncResultHandler<Message<JsonObject>>() {
            @Override
            public void handle(AsyncResult<Message<JsonObject>> result) {
              if (result.succeeded()) {
                JsonObject body = result.result().body();
                String error = body.getString("error");
                if (error != null) {
                  future.setFailure(new VineException(error));
                }
                else {
                  future.setResult(true);
                }
              }
              else {
                future.setFailure(result.cause());
              }
            }
          });
        }
        else {
          eventBus.send(address, Actions.create(action, context.serialize()), new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> result) {
              JsonObject body = result.body();
              String error = body.getString("error");
              if (error != null) {
                future.setFailure(new VineException(error));
              }
              else {
                future.setResult(true);
              }
            }
          });
        }
      }
    }

  }

}
