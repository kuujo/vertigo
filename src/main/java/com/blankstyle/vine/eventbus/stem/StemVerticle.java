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
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.context.StemContext;
import com.blankstyle.vine.context.WorkerContext;
import com.blankstyle.vine.heartbeat.HeartBeatMonitor;

/**
 * A Vine stem verticle.
 *
 * @author Jordan Halterman
 */
public class StemVerticle extends BusModBase implements Handler<Message<JsonObject>> {

  private StemContext context;

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
    context = new StemContext(container.config());
    vertx.eventBus().registerHandler(context.getAddress(), this);
  }

  @Override
  public void handle(final Message<JsonObject> message) {
    String action = getMandatoryString("address", message);

    if (action == null) {
      sendError(message, "An action must be specified.");
    }

    switch (action) {
      case "register":
        doRegister(message);
        break;
      case "assign":
        doAssign(message);
        break;
      case "release":
        doRelease(message);
        break;
      default:
        sendError(message, String.format("Invalid action %s.", action));
    }
  }

  /**
   * Registers a context.
   */
  private void doRegister(final Message<JsonObject> message) {
    final String address = getMandatoryString("address", message);
    String heartbeatAddress = nextHeartBeatAddress();
    heartbeatMap.put(address, heartbeatAddress);
    heartbeatMonitor.monitor(heartbeatAddress, new Handler<String>() {
      @Override
      public void handle(String hbAddress) {
        // TODO: Restart the worker if it dies.
      }
    });
    message.reply(heartbeatAddress);
  }

  /**
   * Assigns a context to the stem.
   */
  private void doAssign(final Message<JsonObject> message) {
    WorkerContext context = new WorkerContext(message.body());
    deployWorker(context, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.succeeded()) {
          message.reply(result.result());
        }
        else {
          sendError(message, "Failed to deploy worker(s).");
        }
      }
    });
  }

  /**
   * Releases a context from the stem.
   */
  private void doRelease(final Message<JsonObject> message) {
    WorkerContext context = new WorkerContext(message.body());
    undeployWorker(context, new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.succeeded()) {
          message.reply(result.result());
        }
        else {
          sendError(message, "Failed to undeploy worker(s).");
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
  private void deployWorker(final WorkerContext context, Handler<AsyncResult<Boolean>> resultHandler) {
    final Future<Boolean> future = new DefaultFutureResult<Boolean>().setHandler(resultHandler);
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
  private void undeployWorker(WorkerContext context, Handler<AsyncResult<Boolean>> resultHandler) {
    final Future<Boolean> future = new DefaultFutureResult<Boolean>();
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

}
