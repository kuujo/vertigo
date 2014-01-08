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
import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A deployable (verticle/module) hook.
 *
 * @author Jordan Halterman
 */
abstract class DeployableHook implements ComponentHook {
  protected String address;
  protected @JsonIgnore EventBus eventBus;

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
    eventBus = component.vertx().eventBus();
    deploy(component.container(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          eventBus.send(address, new JsonObject().putString("event", "start"));
        }
      }
    });
  }

  @Override
  public void handleReceive(MessageId messageId) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "receive").putObject("id", messageId.toJson()));
    }
  }

  @Override
  public void handleAck(MessageId messageId) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "ack").putObject("id", messageId.toJson()));
    }
  }

  @Override
  public void handleFail(MessageId messageId) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "fail").putObject("id", messageId.toJson()));
    }
  }

  @Override
  public void handleEmit(MessageId messageId) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "emit").putObject("id", messageId.toJson()));
    }
  }

  @Override
  public void handleAcked(MessageId messageId) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "acked").putObject("id", messageId.toJson()));
    }
  }

  @Override
  public void handleFailed(MessageId messageId) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "failed").putObject("id", messageId.toJson()));
    }
  }

  @Override
  public void handleTimeout(MessageId messageId) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "timeout").putObject("id", messageId.toJson()));
    }
  }

  @Override
  public void handleStop(Component<?> component) {
    if (deployed()) {
      eventBus.send(address, new JsonObject().putString("event", "stop"));
      undeploy(component.container(), new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          // Do nothing.
        }
      });
    }
  }

}
