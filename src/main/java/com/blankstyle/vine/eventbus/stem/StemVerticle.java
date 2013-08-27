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
package com.blankstyle.vine.eventbus.stem;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.context.JsonStemContext;
import com.blankstyle.vine.context.JsonWorkerContext;
import com.blankstyle.vine.context.StemContext;
import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.eventbus.Action;
import com.blankstyle.vine.eventbus.Argument;
import com.blankstyle.vine.eventbus.ArgumentsDefinition;
import com.blankstyle.vine.eventbus.AsynchronousAction;
import com.blankstyle.vine.eventbus.CommandDispatcher;
import com.blankstyle.vine.eventbus.DefaultCommandDispatcher;
import com.blankstyle.vine.eventbus.JsonCommand;
import com.blankstyle.vine.eventbus.ReliableEventBus;
import com.blankstyle.vine.eventbus.SynchronousAction;
import com.blankstyle.vine.heartbeat.HeartBeatMonitor;

/**
 * A Vine stem verticle.
 *
 * @author Jordan Halterman
 */
public class StemVerticle extends BusModBase implements Handler<Message<JsonObject>> {

  private StemContext context;

  private CommandDispatcher dispatcher = new DefaultCommandDispatcher() {{
    registerAction(Register.NAME, Register.class);
    registerAction(Assign.NAME, Assign.class);
    registerAction(Release.NAME, Release.class);
  }};

  /**
   * A map of worker addresses to deployment IDs.
   */
  private Map<String, String> workers = new HashMap<String, String>();

  /**
   * A map of worker addresses to worker contexts.
   */
  private Map<String, WorkerContext> contexts = new HashMap<String, WorkerContext>();

  /**
   * A map of worker addresses to heartbeat addresses.
   */
  private Map<String, String> heartbeatMap = new HashMap<String, String>();

  private int heartbeatCounter;

  private HeartBeatMonitor heartbeatMonitor;

  @Override
  public void start() {
    context = new JsonStemContext(container.config());
    dispatcher.setVertx(vertx);
    dispatcher.setEventBus(vertx.eventBus());
    dispatcher.setContext(context);
    vertx.eventBus().registerHandler(context.getAddress(), this);
  }

  @Override
  public void handle(final Message<JsonObject> message) {
    dispatcher.dispatch(new JsonCommand(message.body()), new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.succeeded()) {
          message.reply(result.result());
        }
        else {
          message.reply(result.cause());
        }
      }
    });
  }

  /**
   * Registers a worker context.
   *
   * @param address
   *   The worker address.
   * @param context
   *   The worker context.
   */
  private void registerWorkerContext(String address, WorkerContext context) {
    contexts.put(address, context);
  }

  /**
   * Unregisters a worker context.
   *
   * @param address
   *   The worker address.
   */
  private void unregisterWorkerContext(String address) {
    if (contexts.containsKey(address)) {
      contexts.remove(address);
    }
  }

  /**
   * Registers a worker deployment ID.
   *
   * @param address
   *   The worker address.
   * @param deploymentID
   *   The worker deployment ID.
   */
  private void registerDeploymentID(String address, String deploymentID) {
    workers.put(address, deploymentID);
  }

  /**
   * Unregisters a worker deployment ID.
   *
   * @param address
   *   The worker address.
   */
  private void unregisterDeploymentID(String address) {
    if (workers.containsKey(address)) {
      workers.remove(address);
    }
  }

  /**
   * Gets a worker deployment ID.
   *
   * @param address
   *   The worker address.
   * @return
   *   A worker deployment ID.
   */
  private String getDeploymentID(String address) {
    if (workers.containsKey(address)) {
      return workers.get(address);
    }
    return null;
  }

  /**
   * Deploys a worker.
   *
   * @param context
   *   The worker context.
   */
  private void deployWorker(final WorkerContext context, Handler<AsyncResult<Object>> resultHandler) {
    final Future<Object> future = new DefaultFutureResult<Object>().setHandler(resultHandler);
    registerWorkerContext(context.getAddress(), context);
    container.deployWorkerVerticle(context.getContext().getDefinition().getMain(), context.serialize(), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          registerDeploymentID(context.getAddress(), result.result());
          future.setResult(true);
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

  /**
   * Undeploys a worker.
   *
   * @param context
   *   The worker context.
   */
  private void undeployWorker(WorkerContext context, Handler<AsyncResult<Object>> resultHandler) {
    final Future<Object> future = new DefaultFutureResult<Object>();
    unregisterWorkerContext(context.getAddress());
    String deploymentID = getDeploymentID(context.getAddress());
    if (deploymentID != null) {
      container.undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
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
      unregisterDeploymentID(deploymentID);
    }
    else {
      future.setResult(true);
    }
  }

  /**
   * Returns the next heartbeat address.
   */
  private String nextHeartBeatAddress() {
    heartbeatCounter++;
    return String.format("%s.heartbeat.%s", context.getAddress(), heartbeatCounter);
  }

  /**
   * A worker register action.
   *
   * This action is called when a new worker is started. The worker will call
   * this action and in response get a unique address to which to send heartbeats.
   *
   * @author Jordan Halterman
   */
  public class Register extends Action<StemContext> implements SynchronousAction<String> {

    public static final String NAME = "register";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<String>() {
        @Override
        public String name() {
          return "address";
        }
        @Override
        public boolean isValid(String value) {
          return value instanceof String;
        }
      });
    }};

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public String execute(Object[] args) {
      final String address = (String) args[0];
      String heartbeatAddress = nextHeartBeatAddress();
      heartbeatMap.put(address, heartbeatAddress);
      heartbeatMonitor.monitor(heartbeatAddress, new Handler<String>() {
        @Override
        public void handle(String hbAddress) {
          // TODO: Restart the worker if it dies.
        }
      });
      return heartbeatAddress;
    }

  }

  /**
   * Assign worker action.
   *
   * Arguments:
   * - worker: A JSON object representing worker context.
   *
   * @author Jordan Halterman
   */
  public class Assign extends Action<StemContext> implements AsynchronousAction<Void> {

    public static final String NAME = "assign";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<JsonObject>() {
        @Override
        public String name() {
          return "worker";
        }
        @Override
        public boolean isValid(JsonObject value) {
          return value instanceof JsonObject;
        }
      });
    }};

    public Assign(Vertx vertx, ReliableEventBus eventBus, StemContext context) {
      super(vertx, eventBus, context);
    }

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public void execute(Object[] args, Handler<AsyncResult<Object>> resultHandler) {
      WorkerContext context = new JsonWorkerContext((JsonObject) args[0]);
      deployWorker(context, resultHandler);
    }

  }

  /**
   * Release worker assignment action.
   *
   * @author Jordan Halterman
   */
  public class Release extends Action<StemContext> implements AsynchronousAction<Void> {

    public static final String NAME = "release";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<JsonObject>() {
        @Override
        public String name() {
          return "seed";
        }
        @Override
        public boolean isValid(JsonObject value) {
          return value instanceof JsonObject;
        }
      });
    }};

    public Release(Vertx vertx, ReliableEventBus eventBus, StemContext context) {
      super(vertx, eventBus, context);
    }

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public void execute(Object[] args, Handler<AsyncResult<Object>> resultHandler) {
      WorkerContext context = new JsonWorkerContext((JsonObject) args[0]);
      undeployWorker(context, resultHandler);
    }

  }

}
