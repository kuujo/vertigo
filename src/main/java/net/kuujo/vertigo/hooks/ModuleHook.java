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

import java.util.UUID;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

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
  private String module;
  private JsonObject config;
  private int instances = 1;
  @JsonIgnore private String deploymentId;
  @JsonIgnore private String address = UUID.randomUUID().toString();

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
    this.module = moduleName;
    this.config = config;
    this.instances = instances;
  }

  @JsonGetter("config")
  private String getConfigEncoded() {
    return config != null ? config.encode() : null;
  }

  @JsonSetter("config")
  private void setConfigEncoded(String encoded) {
    if (encoded != null) {
      config = new JsonObject(encoded);
    }
  }

  @Override
  protected void deploy(Container container, Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    container.deployModule(module, new JsonObject().putString("address", address)
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

}
