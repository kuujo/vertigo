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
package net.kuujo.vertigo.hooks;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.serializer.SerializationException;

/**
 * A module hook.
 *
 * The module hook can be used to deploy a module along side each component
 * instance to which the hook hooks. The deployed hook module will receive
 * notifications via the event bus. Hook module implementations should
 * extend the abstract {@link HookVerticle} class.
 *
 * @author Jordan Halterman
 */
public class ModuleHook extends DeployableHook {
  private String moduleName;
  private JsonObject config;
  private int instances = 1;
  private String deploymentId;
  private String address;

  public ModuleHook() {
  }

  public ModuleHook(String moduleName) {
    this(moduleName, new JsonObject(), 1);
  }

  public ModuleHook(String moduleName, int instances) {
    this(moduleName, new JsonObject(), instances);
  }

  public ModuleHook(String moduleName, JsonObject config) {
    this(moduleName, config, 1);
  }

  public ModuleHook(String moduleName, JsonObject config, int instances) {
    this.moduleName = moduleName;
    this.config = config;
    this.instances = instances;
  }

  @Override
  protected void deploy(Container container, Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    container.deployModule(moduleName, new JsonObject().putString("address", address)
        .putObject("config", config), instances, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          deploymentId = result.result();
          future.setResult(null);
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

  @Override
  protected void undeploy(Container container, Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    if (deploymentId != null) {
      container.undeployModule(deploymentId, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          deploymentId = null;
          if (result.succeeded()) future.setResult(null); else future.setFailure(result.cause());
        }
      });
    }
  }

  @Override
  protected boolean deployed() {
    return deploymentId != null;
  }

  @Override
  public JsonObject getState() {
    return new JsonObject().putString("module", moduleName)
        .putNumber("instances", instances).putObject("config", config);
  }

  @Override
  public void setState(JsonObject state) throws SerializationException {
    this.moduleName = state.getString("module");
    this.config = state.getObject("config", new JsonObject());
    this.instances = state.getInteger("instances", 1);
  }

}
