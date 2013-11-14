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

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.serializer.Serializable;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A deployable (verticle/module) hook.
 *
 * @author Jordan Halterman
 */
abstract class DeployableHook implements ComponentHook, Serializable {
  protected String address;
  protected EventBus eventBus;

  /**
   * Deploys the deployable.
   */
  protected abstract void deploy(Container container, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys the deployable.
   */
  protected abstract void undeploy(Container container, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Indicates whether the deployable is currently deployed.
   */
  protected abstract boolean deployed();

  @Override
  public void handleStart(Component<?> component) {
    address = UUID.randomUUID().toString();
    eventBus = component.getVertx().eventBus();
    deploy(component.getContainer(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          eventBus.send(address, new JsonObject().putString("event", "start"));
        }
      }
    });
  }

  @Override
  public void handleReceive(String id) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "receive").putString("id", id));
    }
  }

  @Override
  public void handleAck(String id) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "ack").putString("id", id));
    }
  }

  @Override
  public void handleFail(String id) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "fail").putString("id", id));
    }
  }

  @Override
  public void handleEmit(String id) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "emit").putString("id", id));
    }
  }

  @Override
  public void handleAcked(String id) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "acked").putString("id", id));
    }
  }

  @Override
  public void handleFailed(String id) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "failed").putString("id", id));
    }
  }

  @Override
  public void handleTimeout(String id) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "timeout").putString("id", id));
    }
  }

  @Override
  public void handleStop(Component<?> component) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "stop"));
      undeploy(component.getContainer(), new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          // Do nothing.
        }
      });
    }
  }

}
