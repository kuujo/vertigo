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
package net.kuujo.vitis.eventbus.vitis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vitis.Seed;
import net.kuujo.vitis.VineException;
import net.kuujo.vitis.context.SeedContext;
import net.kuujo.vitis.context.VitisContext;
import net.kuujo.vitis.context.NodeContext;
import net.kuujo.vitis.context.VineContext;
import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.definition.MalformedDefinitionException;
import net.kuujo.vitis.definition.VineDefinition;
import net.kuujo.vitis.heartbeat.DefaultHeartBeatMonitor;
import net.kuujo.vitis.heartbeat.HeartBeatMonitor;
import net.kuujo.vitis.remote.RemoteSeed;
import net.kuujo.vitis.scheduler.Scheduler;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * A Vine root verticle.
 *
 * @author Jordan Halterman
 */
public class VitisVerticle extends BusModBase implements Handler<Message<JsonObject>> {

  /**
   * The root address.
   */
  private String address;

  private static final String DEFAULT_ADDRESS = "vine.root";

  /**
   * A root context.
   */
  private VitisContext context;

  /**
   * A worker scheduler.
   */
  private Scheduler scheduler;

  /**
   * A map of stem addresses to heartbeat address.
   */
  private Map<String, String> heartbeatMap = new HashMap<String, String>();

  /**
   * A private counter for creating unique heartbeat addresses.
   */
  private int heartbeatCounter;

  /**
   * A heartbeat monitor for tracking whether stems are alive.
   */
  private HeartBeatMonitor heartbeatMonitor = new DefaultHeartBeatMonitor();

  /**
   * A map of stem addresses to stem contexts.
   */
  private Map<String, SeedContext> stems = new HashMap<String, SeedContext>();

  /**
   * A map of context addresses to contexts.
   */
  private Map<String, VineContext> contexts = new HashMap<String, VineContext>();

  /**
   * A map of worker addresses to stem addresses.
   */
  private Map<String, String> deploymentMap = new HashMap<String, String>();

  @Override
  public void setVertx(Vertx vertx) {
    super.setVertx(vertx);
    heartbeatMonitor.setVertx(vertx).setEventBus(vertx.eventBus());
  }

  @Override
  public void start() {
    super.start();
    context = new VitisContext(config);
    address = getOptionalStringConfig("address", DEFAULT_ADDRESS);
    logger.info(String.format("Starting stem at %s.", address));
    String schedulerClass = getOptionalStringConfig("scheduler", "net.kuujo.vine.scheduler.RandomScheduler");
    try {
      scheduler = (Scheduler) Class.forName(schedulerClass).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      container.logger().error(String.format("Invalid scheduler class %s.", schedulerClass));
    }
    vertx.eventBus().registerHandler(address, this);
  }

  @Override
  public void handle(final Message<JsonObject> message) {
    String action = getMandatoryString("action", message);

    if (action == null) {
      sendError(message, "An action must be specified.");
      return;
    }

    switch (action) {
      case "register":
        doRegister(message);
        break;
      case "deploy":
        doDeploy(message);
        break;
      case "undeploy":
        doUndeploy(message);
        break;
      default:
        sendError(message, String.format("Invalid action %s.", action));
    }
  }

  /**
   * Registers a stem with the root.
   *
   * Replies with a string representing the stem heartbeat address.
   */
  private void doRegister(final Message<JsonObject> message) {
    final SeedContext stemContext = new SeedContext(getMandatoryObject("register", message));
    final String address = stemContext.getAddress();
    String heartbeatAddress = nextHeartBeatAddress();
    heartbeatMap.put(address, heartbeatAddress);
    heartbeatMonitor.monitor(heartbeatAddress, new Handler<String>() {
      @Override
      public void handle(String hbAddress) {
        unregisterStem(stemContext);
      }
    });
    registerStem(stemContext);
    message.reply(new JsonObject().putString("address", heartbeatAddress));
  }

  /**
   * Creates a list of stems.
   */
  private List<Seed> createStemList() {
    List<Seed> stems = new ArrayList<Seed>();
    Set<String> keys = this.stems.keySet();
    Iterator<String> addresses = keys.iterator();
    while (addresses.hasNext()) {
      stems.add(new RemoteSeed(addresses.next(), vertx, container));
    }
    return stems;
  }

  /**
   * Deploys a vine definition.
   */
  private void doDeploy(final Message<JsonObject> message) {
    JsonObject def = getMandatoryObject("deploy", message);
    if (def == null) {
      sendError(message, "Invalid definition.");
      return;
    }

    VineDefinition definition = new VineDefinition(def);

    try {
      final VineContext context = definition.createContext();
      RecursiveScheduler scheduler = new RecursiveScheduler(context, createStemList());
      scheduler.assign(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.succeeded()) {
            contexts.put(context.getAddress(), context);
            message.reply(new JsonObject().putObject("context", context.serialize()));
          }
          else {
            sendError(message, "Failed to assign workers.");
          }
        }
      });
    } catch (MalformedDefinitionException e) {
      sendError(message, e.getMessage());
    }
  }

  /**
   * Undeploys a vine definition.
   */
  private void doUndeploy(final Message<JsonObject> message) {
    final String address = getMandatoryString("undeploy", message);
    if (!contexts.containsKey(address)) {
      sendError(message, String.format("Invalid vine address %s.", address));
    }
    else {
      VineContext context = contexts.get(address);
      RecursiveScheduler scheduler = new RecursiveScheduler(context, createStemList());
      scheduler.release(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.succeeded()) {
            contexts.remove(address);
            message.reply(new JsonObject().putString("address", address));
          }
          else {
            sendError(message, "Failed to release workers.");
          }
        }
      });
    }
  }

  /**
   * Registers a stem context.
   *
   * @param context
   *   The stem context.
   */
  private void registerStem(SeedContext context) {
    stems.put(context.getAddress(), context);
  }

  /**
   * Unregisters a stem context.
   *
   * @param context
   *   The stem context.
   */
  private void unregisterStem(SeedContext context) {
    if (stems.containsKey(context.getAddress())) {
      stems.remove(context.getAddress());
    }
  }

  /**
   * Returns the next heartbeat address.
   */
  private String nextHeartBeatAddress() {
    return String.format("%s.heartbeat.%d", context.getAddress(), ++heartbeatCounter);
  }

  /**
   * Recursively deploys all vine elements.
   *
   * @author Jordan Halterman
   */
  private class RecursiveScheduler {

    private VineContext context;

    private Collection<Seed> stems;

    public RecursiveScheduler(VineContext context, Collection<Seed> stems) {
      this.context = context;
      this.stems = stems;
    }

    /**
     * Assigns the vine.
     *
     * @param doneHandler
     *   A handler to be invoked once the vine is deployed.
     */
    public void assign(Handler<AsyncResult<Void>> doneHandler) {
      Collection<NodeContext> seeds = context.getSeedContexts();
      RecursiveSeedScheduler executor = new RecursiveSeedScheduler(seeds);
      executor.assign(doneHandler);
    }

    /**
     * Releases the vine.
     *
     * @param doneHandler
     *   A handler to be invoked once the vine is deployed.
     */
    public void release(Handler<AsyncResult<Void>> doneHandler) {
      Collection<NodeContext> seeds = context.getSeedContexts();
      RecursiveSeedScheduler executor = new RecursiveSeedScheduler(seeds);
      executor.release(doneHandler);
    }

    /**
     * An abstract context deployer.
     *
     * @param <T> The context type.
     */
    private abstract class RecursiveContextScheduler<T> {
      protected Iterator<T> iterator;
      protected Future<Void> future;

      protected Handler<AsyncResult<String>> assignHandler = new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.succeeded()) {
            if (iterator.hasNext()) {
              doAssign(iterator.next(), assignHandler);
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
              doRelease(iterator.next(), releaseHandler);
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

      public RecursiveContextScheduler(Collection<T> contexts) {
        this.iterator = contexts.iterator();
      }

      /**
       * Assigns a vine.
       *
       * @param doneHandler
       *   The handler to invoke once deployment is complete.
       */
      public void assign(Handler<AsyncResult<Void>> doneHandler) {
        this.future = new DefaultFutureResult<Void>();
        future.setHandler(doneHandler);
        if (iterator.hasNext()) {
          doAssign(iterator.next(), assignHandler);
        }
        else {
          future.setResult(null);
        }
      }

      /**
       * Releases a vine.
       *
       * @param doneHandler
       *   The handler to invoke once deployment is complete.
       */
      public void release(Handler<AsyncResult<Void>> doneHandler) {
        this.future = new DefaultFutureResult<Void>();
        future.setHandler(doneHandler);
        if (iterator.hasNext()) {
          doRelease(iterator.next(), releaseHandler);
        }
        else {
          future.setResult(null);
        }
      }

      /**
       * Assigns a context to a stem.
       *
       * @param context
       *   The context to deploy.
       * @param doneHandler
       *   A handler to be invoked once deployment is complete.
       */
      protected abstract void doAssign(T context, Handler<AsyncResult<String>> doneHandler);

      /**
       * Releases a context from a stem.
       *
       * @param context
       *   The context to deploy.
       * @param doneHandler
       *   A handler to be invoked once deployment is complete.
       */
      protected abstract void doRelease(T context, Handler<AsyncResult<Void>> doneHandler);

    }

    /**
     * A vine seed deployer.
     */
    private class RecursiveSeedScheduler extends RecursiveContextScheduler<NodeContext> {

      public RecursiveSeedScheduler(Collection<NodeContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doAssign(NodeContext context, Handler<AsyncResult<String>> resultHandler) {
        final Future<String> future = new DefaultFutureResult<String>();
        future.setHandler(resultHandler);
        Collection<WorkerContext> workers = context.getWorkerContexts();
        RecursiveWorkerScheduler executor = new RecursiveWorkerScheduler(workers);
        executor.assign(new Handler<AsyncResult<Void>>() {
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
      protected void doRelease(NodeContext context, Handler<AsyncResult<Void>> doneHandler) {
        final Future<Void> future = new DefaultFutureResult<Void>();
        future.setHandler(doneHandler);
        Collection<WorkerContext> workers = context.getWorkerContexts();
        RecursiveWorkerScheduler executor = new RecursiveWorkerScheduler(workers);
        executor.release(new Handler<AsyncResult<Void>>() {
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
     * A vine worker deployer.
     */
    private class RecursiveWorkerScheduler extends RecursiveContextScheduler<WorkerContext> {

      public RecursiveWorkerScheduler(Collection<WorkerContext> contexts) {
        super(contexts);
      }

      @Override
      protected void doAssign(final WorkerContext context, Handler<AsyncResult<String>> resultHandler) {
        final Future<String> future = new DefaultFutureResult<String>();
        future.setHandler(resultHandler);

        scheduler.assign(context, stems, new Handler<AsyncResult<String>>() {
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
      protected void doRelease(WorkerContext context, Handler<AsyncResult<Void>> resultHandler) {
        final Future<Void> future = new DefaultFutureResult<Void>();
        future.setHandler(resultHandler);
        String address = context.getAddress();
        String stemAddress = deploymentMap.get(address);
        if (stemAddress != null) {
          Iterator<Seed> iterStem = stems.iterator();
          boolean released = false;
          while (iterStem.hasNext()) {
            Seed stem = iterStem.next();
            if (stem.getAddress() == stemAddress) {
              stem.release(address, resultHandler);
              released = true;
            }
          }

          if (!released) {
            future.setResult(null);
          }
        }
        else {
          future.setFailure(new VineException("Worker has not yet been deployed."));
        }
      }
    }

  }

}
