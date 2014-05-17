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
package net.kuujo.vertigo.cluster.manager.impl;

import java.io.File;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import net.kuujo.vertigo.cluster.manager.NodeManager;
import net.kuujo.vertigo.platform.ModuleInfo;
import net.kuujo.vertigo.platform.PlatformManager;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.hazelcast.core.MultiMap;

/**
 * Defualt node manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultNodeManager implements NodeManager {
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
  private final String node;
  private final String group;
  private final String cluster;
  private final Vertx vertx;
  private final PlatformManager platform;
  private final ClusterListener listener;
  private final MultiMap<String, String> nodes;
  private final MultiMap<String, String> groups;
  private final MultiMap<String, String> deployments;

  private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String action = message.body().getString("action");
      if (action != null) {
        switch (action) {
          case "installed":
            doInstalled(message);
            break;
          case "install":
            doInstall(message);
            break;
          case "uninstall":
            doUninstall(message);
            break;
          case "upload":
            doUpload(message);
            break;
          case "deploy":
            doDeploy(message);
            break;
          case "undeploy":
            doUndeploy(message);
            break;
          default:
            message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid action " + action));
            break;
        }
      }
    }
  };

  public DefaultNodeManager(String node, String group, String cluster, Vertx vertx, Container container, PlatformManager platform, ClusterListener listener, ClusterData data) {
    this.node = node;
    this.group = group;
    this.cluster = cluster;
    this.vertx = vertx;
    this.platform = platform;
    this.listener = listener;
    this.nodes = data.getMultiMap(String.format("nodes.%s", cluster));
    this.groups = data.getMultiMap(String.format("groups.%s", cluster));
    this.deployments = data.getMultiMap(String.format("deployments.%s", cluster));
  }

  @Override
  public String address() {
    return String.format("%s.%s.%s", cluster, group, node);
  }

  @Override
  public NodeManager start() {
    return start(null);
  }

  @Override
  public NodeManager start(Handler<AsyncResult<Void>> doneHandler) {
    if (!nodes.containsEntry(listener.nodeId(), node)) {
      nodes.put(listener.nodeId(), node);
    }
    if (!groups.containsEntry(group, node)) {
      groups.put(group, node);
    }
    vertx.eventBus().registerHandler(address(), messageHandler, doneHandler);
    return this;
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    nodes.remove(listener.nodeId(), node);
    groups.remove(group, node);
    vertx.eventBus().unregisterHandler(address(), messageHandler, doneHandler);
  }

  /**
   * Checks if a module is installed.
   */
  private void doInstalled(final Message<JsonObject> message) {
    String moduleName = message.body().getString("module");
    if (moduleName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module specified."));
      return;
    }

    platform.getModuleInfo(moduleName, new Handler<AsyncResult<ModuleInfo>>() {
      @Override
      public void handle(AsyncResult<ModuleInfo> result) {
        if (result.failed() || result.result() == null) {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", false));
        } else {
          message.reply(new JsonObject().putString("status", "ok").putBoolean("result", true));
        }
      }
    });
  }

  /**
   * Installs a module.
   */
  private void doInstall(final Message<JsonObject> message) {
    String moduleName = message.body().getString("module");
    if (moduleName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module specified."));
      return;
    }

    String uploadID = message.body().getString("upload");
    if (uploadID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No upload found."));
      return;
    }

    File modRoot = new File(TEMP_DIR, "vertx-zip-mods");
    File modZip = new File(modRoot, uploadID + ".zip");
    if (!modZip.exists()) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid upload."));
      return;
    }

    platform.installModule(modZip.getAbsolutePath(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok"));
        }
      }
    });
  }

  /**
   * Uninstalls a module.
   */
  private void doUninstall(final Message<JsonObject> message) {
    String moduleName = message.body().getString("module");
    if (moduleName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module specified."));
      return;
    }

    platform.uninstallModule(moduleName, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          message.reply(new JsonObject().putString("status", "ok"));
        }
      }
    });
  }

  /**
   * Uploads a file.
   */
  private void doUpload(final Message<JsonObject> message) {
    final String id = UUID.randomUUID().toString();
    final File modRoot = new File(TEMP_DIR, "vertx-zip-mods");
    vertx.fileSystem().mkdir(modRoot.getAbsolutePath(), true, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to create upload file."));
        } else {
          File modZip = new File(modRoot, id + ".zip");
          modZip.deleteOnExit();
          vertx.fileSystem().open(modZip.getAbsolutePath(), new Handler<AsyncResult<AsyncFile>>() {
            @Override
            public void handle(AsyncResult<AsyncFile> result) {
              if (result.failed()) {
                message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to create upload file."));
              } else {
                handleUpload(result.result(), id, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      message.reply(new JsonObject().putString("status", "error").putString("message", "Failed to register upload handler."));
                    } else {
                      message.reply(new JsonObject().putString("status", "ok").putString("id", id));
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
  }

  /**
   * Handles a file upload.
   */
  private void handleUpload(AsyncFile file, String address, Handler<AsyncResult<Void>> doneHandler) {
    vertx.eventBus().registerHandler(address, handleUpload(file, address), doneHandler);
  }

  /**
   * Handles a file upload.
   */
  private Handler<Message<Buffer>> handleUpload(final AsyncFile file, final String address) {
    final AtomicLong position = new AtomicLong();
    return new Handler<Message<Buffer>>() {
      @Override
      public void handle(final Message<Buffer> message) {
        final Handler<Message<Buffer>> handler = this;
        final Buffer buffer = message.body();
        if (buffer.length() > 0) {
          file.write(buffer, position.get(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                file.close();
                message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                vertx.eventBus().unregisterHandler(address, handler);
              } else {
                position.addAndGet(buffer.length());
                message.reply(new JsonObject().putString("status", "ok"));
              }
            }
          });
        } else {
          file.flush(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                vertx.eventBus().unregisterHandler(address, handler);
              } else {
                file.close(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
                      vertx.eventBus().unregisterHandler(address, handler);
                    } else {
                      vertx.eventBus().unregisterHandler(address, handler, new Handler<AsyncResult<Void>>() {
                        @Override
                        public void handle(AsyncResult<Void> result) {
                          message.reply(new JsonObject().putString("status", "ok"));
                        }
                      });
                    }
                  }
                });
              }
            }
          });
        }
      }
    };
  }

  /**
   * Deploys a module or verticle.
   */
  private void doDeploy(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment type specified."));
    } else {
      switch (type) {
        case "module":
          doDeployModule(message);
          break;
        case "verticle":
          doDeployVerticle(message);
          break;
        default:
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid deployment type."));
          break;
      }
    }
  }

  /**
   * Deploys a module
   */
  private void doDeployModule(final Message<JsonObject> message) {
    String moduleName = message.body().getString("module");
    if (moduleName == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No module name specified."));
      return;
    }

    JsonObject config = message.body().getObject("config");
    if (config == null) {
      config = new JsonObject();
    }
    int instances = message.body().getInteger("instances", 1);
    platform.deployModule(moduleName, config, instances, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
        } else {
          deployments.put(node, message.body().copy().putString("id", result.result()).encode());
          message.reply(new JsonObject().putString("status", "ok").putString("id", result.result()));
        }
      }
    });
  }

  /**
   * Deploys a verticle.
   */
  private void doDeployVerticle(final Message<JsonObject> message) {
    String main = message.body().getString("main");
    if (main == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No verticle main specified."));
      return;
    }

    JsonObject config = message.body().getObject("config");
    if (config == null) {
      config = new JsonObject();
    }
    int instances = message.body().getInteger("instances", 1);
    boolean worker = message.body().getBoolean("worker", false);
    if (worker) {
      boolean multiThreaded = message.body().getBoolean("multi-threaded", false);
      platform.deployWorkerVerticle(main, config, instances, multiThreaded, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            deployments.put(node, message.body().copy().putString("id", result.result()).encode());
            message.reply(new JsonObject().putString("status", "ok").putString("id", result.result()));
          }
        }
      });
    } else {
      platform.deployVerticle(main, config, instances, new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            deployments.put(node, message.body().copy().putString("id", result.result()).encode());
            message.reply(new JsonObject().putString("status", "ok").putString("id", result.result()));
          }
        }
      });
    }
  }

  /**
   * Undeploys a module or verticle.
   */
  private void doUndeploy(final Message<JsonObject> message) {
    String type = message.body().getString("type");
    if (type == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment type specified."));
    } else {
      switch (type) {
        case "module":
          doUndeployModule(message);
          break;
        case "verticle":
          doUndeployVerticle(message);
          break;
        default:
          message.reply(new JsonObject().putString("status", "error").putString("message", "Invalid deployment type " + type));
          break;
      }
    }
  }

  /**
   * Undeploys a module.
   */
  private void doUndeployModule(final Message<JsonObject> message) {
    String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      removeDeployment(deploymentID);
      platform.undeployModule(deploymentID, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok"));
          }
        }
      });
    }
  }

  /**
   * Undeploys a verticle.
   */
  private void doUndeployVerticle(final Message<JsonObject> message) {
    String deploymentID = message.body().getString("id");
    if (deploymentID == null) {
      message.reply(new JsonObject().putString("status", "error").putString("message", "No deployment ID specified."));
    } else {
      removeDeployment(deploymentID);
      platform.undeployVerticle(deploymentID, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
          } else {
            message.reply(new JsonObject().putString("status", "ok"));
          }
        }
      });
    }
  }

  /**
   * Removes a deployment from the deployments map.
   */
  private void removeDeployment(String deploymentID) {
    Collection<String> deployments = this.deployments.get(node);
    String deployment = null;
    for (String sdeployment : deployments) {
      JsonObject info = new JsonObject(sdeployment);
      if (info.getString("id").equals(deploymentID)) {
        deployment = sdeployment;
        break;
      }
    }
    if (deployment != null) {
      this.deployments.remove(node, deployment);
    }
  }

}
