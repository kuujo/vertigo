/*
 * Copyright 2014 the original author or authors.
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
import java.util.concurrent.TimeUnit;

import net.kuujo.vertigo.cluster.data.MapEvent;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * Hazelcast cluster verticle.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class HazelcastCluster extends Verticle {
  private HazelcastInstance hazelcast;
  private String nodeID;
  private String address;

  private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String action = message.body().getString("action");
      if (action != null) {
        switch (action) {
          case "check":
            doCheck(message);
            break;
          case "deploy":
            doDeploy(message);
            break;
          case "undeploy":
            doUndeploy(message);
            break;
          case "id":
            doGenerateId(message);
            break;
          default:
            String type = message.body().getString("type");
            if (type == null) {
              message.reply(new JsonObject().putString("status", "error").putString("message", "No data type specified."));
              return;
            }

            switch (type) {
              case "map":
                switch (action) {
                  case "put":
                    doMapPut(message);
                    break;
                  case "get":
                    doMapGet(message);
                    break;
                  case "remove":
                    doMapRemove(message);
                    break;
                  case "contains":
                    doMapContainsKey(message);
                    break;
                  case "keys":
                    doMapKeys(message);
                    break;
                  case "values":
                    doMapValues(message);
                    break;
                  case "empty":
                    doMapIsEmpty(message);
                    break;
                  case "clear":
                    doMapClear(message);
                    break;
                  case "size":
                    doMapSize(message);
                    break;
                  case "watch":
                    doMapWatch(message);
                    break;
                  case "unwatch":
                    doMapUnwatch(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
              case "list":
                switch (action) {
                  case "add":
                    doListAdd(message);
                    break;
                  case "remove":
                    doListRemove(message);
                    break;
                  case "contains":
                    doListContains(message);
                    break;
                  case "size":
                    doListSize(message);
                    break;
                  case "empty":
                    doListIsEmpty(message);
                    break;
                  case "clear":
                    doListClear(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
              case "set":
                switch (action) {
                  case "add":
                    doSetAdd(message);
                    break;
                  case "remove":
                    doSetRemove(message);
                    break;
                  case "contains":
                    doSetContains(message);
                    break;
                  case "size":
                    doSetSize(message);
                    break;
                  case "empty":
                    doSetIsEmpty(message);
                    break;
                  case "clear":
                    doSetClear(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
              case "queue":
                switch (action) {
                  case "add":
                    doQueueAdd(message);
                    break;
                  case "remove":
                    doQueueRemove(message);
                    break;
                  case "contains":
                    doQueueContains(message);
                    break;
                  case "empty":
                    doQueueIsEmpty(message);
                    break;
                  case "size":
                    doQueueSize(message);
                    break;
                  case "clear":
                    doQueueClear(message);
                    break;
                  case "offer":
                    doQueueOffer(message);
                    break;
                  case "element":
                    doQueueElement(message);
                    break;
                  case "poll":
                    doQueuePoll(message);
                    break;
                  case "peek":
                    doQueuePeek(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
              case "lock":
                switch (action) {
                  case "lock":
                    doLockLock(message);
                    break;
                  case "try":
                    doLockTry(message);
                    break;
                  case "unlock":
                    doLockUnlock(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
              case "id":
                switch (action) {
                  case "next":
                    doGenerateId(message);
                    break;
                  default:
                    message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
                    break;
                }
                break;
            }
            message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
            break;
        }
      }
    }
  };

  @Override
  @SuppressWarnings("deprecation")
  public void start(final Future<Void> startResult) {
    hazelcast = Hazelcast.getDefaultInstance();
    nodeID = container.config().getString("id");
    address = container.config().getString("address");
    vertx.eventBus().registerHandler(address, messageHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          startResult.setFailure(result.cause());
        } else {
          startResult.setResult((Void) null);
        }
      }
    });
  }

  /**
   * Checks whether a deployment is deployed.
   */
  private void doCheck(final Message<JsonObject> message) {
    String deploymentID = message.body().getString("id");
    IMap<String, String> deployments = hazelcast.getMap("__vertigo.deployments");
    String nodeID = deployments.get(deploymentID);
    if (nodeID != null) {
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", true));
    } else {
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", false));
    }
  }

  /**
   * Handles a deployment.
   */
  private void doDeploy(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    switch (type) {
      case "module":
        doDeployModule(message);
        break;
      case "verticle":
        doDeployVerticle(message);
        break;
    }
  }

  /**
   * Handles deployment of a module.
   */
  private void doDeployModule(final Message<JsonObject> message) {
    final IMap<String, String> deployments = hazelcast.getMap("__vertigo.deployments");
    final IMap<String, String> nodeDeployments = hazelcast.getMap("__vertigo.deployments." + nodeID);

    final String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
      return;
    }

    if (deployments.containsKey(deploymentID)) {
      message.reply(new JsonObject().putString("status", "ok").putString("id", deploymentID));
      return;
    }

    String module = message.body().getString("module");
    if (module == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module name specified."));
      return;
    }

    JsonObject config = message.body().getObject("config");
    int instances = message.body().getInteger("instances", 1);

    // Set the deployment ID to prevent other nodes from attempting to deploy the deployment.
    deployments.put(deploymentID, nodeID);

    container.deployModule(module, config, instances, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          // Unset the deployment ID if the deployment failed.
          deployments.remove(deploymentID);
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          nodeDeployments.put(deploymentID, result.result());
          message.reply(new JsonObject().putString("status", "ok").putString("id", deploymentID));
        }
      }
    });
  }

  /**
   * Handles deployment of a verticle.
   */
  private void doDeployVerticle(final Message<JsonObject> message) {
    final IMap<String, String> deployments = hazelcast.getMap("__vertigo.deployments");
    final IMap<String, String> nodeDeployments = hazelcast.getMap("__vertigo.deployments." + nodeID);

    final String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
      return;
    }

    if (deployments.containsKey(deploymentID)) {
      message.reply(new JsonObject().putString("status", "ok").putString("id", deploymentID));
      return;
    }

    String main = message.body().getString("main");
    if (main == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No verticle main specified."));
      return;
    }

    JsonObject config = message.body().getObject("config");
    int instances = message.body().getInteger("instances", 1);
    boolean worker = message.body().getBoolean("worker", false);

    // Set the deployment ID to prevent other nodes from attempting to deploy the deployment.
    deployments.put(deploymentID, nodeID);

    if (worker) {
      boolean multiThreaded = message.body().getBoolean("multi-threaded", false);
      container.deployWorkerVerticle(main, config, instances, multiThreaded, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            // Unset the deployment ID if the deployment failed.
            deployments.remove(deploymentID);
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            nodeDeployments.put(deploymentID, result.result());
            message.reply(new JsonObject().putString("status", "ok").putString("id", deploymentID));
          }
        }
      });
    } else {
      container.deployVerticle(main, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            // Unset the deployment ID if the deployment failed.
            deployments.remove(deploymentID);
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            nodeDeployments.put(deploymentID, result.result());
            message.reply(new JsonObject().putString("status", "ok").putString("id", deploymentID));
          }
        }
      });
    }
  }

  /**
   * Handles an undeployment.
   */
  private void doUndeploy(final Message<JsonObject> message) {
    final IMap<String, String> deployments = hazelcast.getMap("__vertigo.deployments");
    final IMap<String, String> nodeDeployments = hazelcast.getMap("__vertigo.deployments." + nodeID);

    final String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
      return;
    }

    final String type = message.body().getString("type");
    if (type == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment type specified."));
      return;
    }

    if (!deployments.containsKey(deploymentID)) {
      message.reply(new JsonObject().putString("status", "ok"));
    } else if (!deployments.get(deploymentID).equals(nodeID)) {
      vertx.eventBus().sendWithTimeout(String.format("__CLUSTER__.%s", nodeID), message.body(), 30000, new Handler<AsyncResult<Message<JsonObject>>>() {
        @Override
        public void handle(AsyncResult<Message<JsonObject>> result) {
          if (result.failed()) {
            message.fail(((ReplyException) result.cause()).failureCode(), result.cause().getMessage());
          } else {
            message.reply(result.result().body());
          }
        }
      });
    } else {
      String internalID = nodeDeployments.get(deploymentID);
      if (internalID == null) {
        deployments.remove(deploymentID);
        message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to resolve deployment ID."));
      } else if (type.equals("module")) {
        container.undeployModule(internalID, new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
            } else {
              message.reply(new JsonObject().putString("status", "ok"));
            }
          }
        });
      } else if (type.equals("verticle")) {
        container.undeployVerticle(internalID, new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
            } else {
              message.reply(new JsonObject().putString("status", "ok"));
            }
          }
        });
      } else {
        message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid deployment type."));
      }
    }
  }

  /**
   * Handles generating a cluster-wide unique ID.
   */
  private void doGenerateId(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }
    try {
      Object result = hazelcast.getIdGenerator(name).getId();
      message.reply(new JsonObject().putString("status", "ok").putValue("id", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster map put command.
   */
  private void doMapPut(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object key = message.body().getValue("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      Object result = hazelcast.getMap(name).put(key, value);
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster map get command.
   */
  private void doMapGet(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final String key = message.body().getString("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    try {
      Object result = hazelcast.getMap(name).get(key);
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster map remove command.
   */
  private void doMapRemove(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final String key = message.body().getString("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    try {
      Object result = hazelcast.getMap(name).remove(key);
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster exists command.
   */
  private void doMapContainsKey(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final String key = message.body().getString("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    try {
      boolean result = hazelcast.getMap(name).containsKey(key);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles map keys command.
   */
  private void doMapKeys(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Set<Object> result = hazelcast.getMap(name).keySet();
      message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.toArray())));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles map values command.
   */
  private void doMapValues(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Collection<Object> result = hazelcast.getMap(name).values();
      message.reply(new JsonObject().putString("status", "ok").putArray("result", new JsonArray(result.toArray())));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles cluster map is empty command.
   */
  private void doMapIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      boolean result = hazelcast.getMap(name).isEmpty();
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Counts the number of items in a map.
   */
  private void doMapSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      int result = hazelcast.getMap(name).size();
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Clears all items in a map.
   */
  private void doMapClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      hazelcast.getMap(name).clear();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a cluster watch command.
   */
  private void doMapWatch(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    String key = message.body().getString("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    String address = message.body().getString("address");
    if (address == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No address specified."));
      return;
    }

    String sevent = message.body().getString("event");
    MapEvent.Type event = null;
    if (sevent != null) {
      try {
        event = MapEvent.Type.parse(sevent);
      } catch (IllegalArgumentException e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
        return;
      }
    }

    IMap<String, String> watchers = hazelcast.getMap(String.format("%s.__watchers", name));
    String swatchers = watchers.get(key);
    JsonObject jsonWatchers = swatchers != null ? new JsonObject(swatchers) : new JsonObject();

    if (!jsonWatchers.containsField(address)) {
      jsonWatchers.putArray(address, new JsonArray());
    }

    // Validate that all provided events are valid.
    JsonArray jsonWatcher = jsonWatchers.getArray(address);

    // Only add the event if it doesn't already exist.
    if (!jsonWatcher.contains(event)) {
      jsonWatcher.add(event.toString());
      watchers.put(key, jsonWatchers.encode());
    }

    message.reply(new JsonObject().putString("status", "ok"));
  }

  /**
   * Handles a cluster unwatch command.
   */
  private void doMapUnwatch(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    String key = message.body().getString("key");
    if (key == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No key specified."));
      return;
    }

    String address = message.body().getString("address");
    if (address == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No address specified."));
      return;
    }

    String sevent = message.body().getString("event");
    MapEvent.Type event = null;
    if (sevent != null) {
      try {
        event = MapEvent.Type.parse(sevent);
      } catch (IllegalArgumentException e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
        return;
      }
    }

    IMap<String, String> watchers = hazelcast.getMap(String.format("%s.__watchers", name));
    String swatchers = watchers.get(key);

    JsonObject jsonWatchers = swatchers != null ? new JsonObject(swatchers) : new JsonObject();

    // If the watcher doesn't exist then simply return ok.
    if (!jsonWatchers.containsField(address)) {
      message.reply(new JsonObject().putString("status", "ok"));
      return;
    }

    // Validate that all provided events are valid.
    JsonArray jsonWatcher = jsonWatchers.getArray(address);

    // Only remove the event if it actually exists.
    if (jsonWatcher.contains(event.toString())) {
      Iterator<Object> iter = jsonWatcher.iterator();
      while (iter.hasNext()) {
        if (iter.next().equals(event.toString())) {
          iter.remove();
        }
      }
      watchers.put(key, jsonWatchers.encode());
    }

    message.reply(new JsonObject().putString("status", "ok"));
  }

  /**
   * Handles a list addition.
   */
  private void doListAdd(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = hazelcast.getList(name).add(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a list removal.
   */
  private void doListRemove(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    if (message.body().containsField("index")) {
      final int index = message.body().getInteger("index");
      try {
        Object result = hazelcast.getList(name).remove(index);
        message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
      } catch (Exception e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
      }
    }
    else {
      final Object value = message.body().getValue("value");
      if (value == null) {
        message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      }
      else {
        try {
          boolean result = hazelcast.getList(name).remove(value);
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
        } catch (Exception e) {
          message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
        }
      }
    }
  }

  /**
   * Checks whether a list contains a value.
   */
  private void doListContains(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = hazelcast.getList(name).contains(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles cluster list is empty command.
   */
  private void doListIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      boolean result = hazelcast.getList(name).isEmpty();
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Counts the number of items in a list.
   */
  private void doListSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      int result = hazelcast.getList(name).size();
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Clears all items in a list.
   */
  private void doListClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      hazelcast.getList(name).clear();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a set addition.
   */
  private void doSetAdd(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = hazelcast.getSet(name).add(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a set removal.
   */
  private void doSetRemove(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
    }
    else {
      try {
        boolean result = hazelcast.getSet(name).remove(value);
        message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
      } catch (Exception e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
      }
    }
  }

  /**
   * Checks whether a set contains a value.
   */
  private void doSetContains(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = hazelcast.getSet(name).contains(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles cluster set is empty command.
   */
  private void doSetIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      boolean result = hazelcast.getSet(name).isEmpty();
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Counts the number of items in a set.
   */
  private void doSetSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      int result = hazelcast.getSet(name).size();
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Clears all items in a set.
   */
  private void doSetClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      hazelcast.getSet(name).clear();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue addition.
   */
  private void doQueueAdd(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = hazelcast.getQueue(name).add(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue removal.
   */
  private void doQueueRemove(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
    }
    else {
      try {
        boolean result = hazelcast.getQueue(name).remove(value);
        message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
      } catch (Exception e) {
        message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
      }
    }
  }

  /**
   * Checks whether a queue contains a value.
   */
  private void doQueueContains(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = hazelcast.getQueue(name).contains(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles cluster queue is empty command.
   */
  private void doQueueIsEmpty(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      boolean result = hazelcast.getQueue(name).isEmpty();
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Counts the number of items in a queue.
   */
  private void doQueueSize(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      int result = hazelcast.getQueue(name).size();
      message.reply(new JsonObject().putString("status", "ok").putNumber("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Clears all items in a queue.
   */
  private void doQueueClear(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      hazelcast.getQueue(name).clear();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue offer command.
   */
  private void doQueueOffer(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final Object value = message.body().getValue("value");
    if (value == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No value specified."));
      return;
    }

    try {
      boolean result = hazelcast.getQueue(name).offer(value);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue element command.
   */
  private void doQueueElement(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Object result = hazelcast.getQueue(name).element();
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue poll command.
   */
  private void doQueuePoll(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Object result = hazelcast.getQueue(name).poll();
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Handles a queue peek command.
   */
  private void doQueuePeek(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      Object result = hazelcast.getQueue(name).peek();
      message.reply(new JsonObject().putString("status", "ok").putValue("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Locks a lock.
   */
  private void doLockLock(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      hazelcast.getLock(name).lock();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Tries to lock a lock.
   */
  private void doLockTry(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    final long timeout = message.body().getLong("timeout", 30000);

    try {
      boolean result = hazelcast.getLock(name).tryLock(timeout, TimeUnit.MILLISECONDS);
      message.reply(new JsonObject().putString("status", "ok").putBoolean("result", result));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

  /**
   * Unlocks a lock.
   */
  private void doLockUnlock(final Message<JsonObject> message) {
    final String name = message.body().getString("name");
    if (name == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No name specified."));
      return;
    }

    try {
      hazelcast.getLock(name).unlock();
      message.reply(new JsonObject().putString("status", "ok"));
    } catch (Exception e) {
      message.reply(new JsonObject().putString("status", "error").putString("message", e.getMessage()));
    }
  }

}
