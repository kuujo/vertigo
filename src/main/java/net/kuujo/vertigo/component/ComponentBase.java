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
package net.kuujo.vertigo.component;

import java.util.ArrayList;
import java.util.List;

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.output.DefaultOutputCollector;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.heartbeat.DefaultHeartbeatEmitter;
import net.kuujo.vertigo.heartbeat.HeartbeatEmitter;
import net.kuujo.vertigo.input.DefaultInputCollector;
import net.kuujo.vertigo.input.InputCollector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * An abstract component.
 *
 * @author Jordan Halterman
 */
public abstract class ComponentBase implements Component {
  protected final Vertx vertx;
  protected final EventBus eventBus;
  protected final Container container;
  protected final Logger logger;
  protected final InstanceContext context;
  protected final String instanceId;
  protected final String address;
  protected final String networkAddress;
  protected final List<String> auditors;
  protected final String broadcastAddress;
  protected final HeartbeatEmitter heartbeat;
  protected final InputCollector input;
  protected final OutputCollector output;

  protected ComponentBase(Vertx vertx, Container container, InstanceContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.container = container;
    this.logger = container.logger();
    this.context = context;
    this.instanceId = context.id();
    this.address = context.getComponent().getAddress();
    NetworkContext networkContext = context.getComponent().getNetwork();
    networkAddress = networkContext.getAddress();
    List<String> auditorAddresses = networkContext.getAuditors();
    auditors = new ArrayList<String>();
    for (String auditorAddress : auditorAddresses) {
      auditors.add(auditorAddress);
    }
    broadcastAddress = networkContext.getBroadcastAddress();
    heartbeat = new DefaultHeartbeatEmitter(vertx);
    input = new DefaultInputCollector(vertx, container, context);
    output = new DefaultOutputCollector(vertx, container, context);
  }

  @Override
  public JsonObject config() {
    return context.getComponent().getConfig();
  }

  @Override
  public InstanceContext context() {
    return context;
  }

  /**
   * Sets up the component.
   */
  protected void setup() {
    setup(null);
  }

  /**
   * Sets up the component.
   */
  protected void setup(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    if (doneHandler != null) {
      future.setHandler(doneHandler);
    }

    setupHeartbeat(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          setupInput(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                future.setFailure(result.cause());
              }
              else {
                setupOutput(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      future.setFailure(result.cause());
                    }
                    else {
                      ready(new Handler<AsyncResult<Void>>() {
                        @Override
                        public void handle(AsyncResult<Void> result) {
                          if (result.failed()) {
                            future.setFailure(result.cause());
                          }
                          else {
                            future.setResult(null);
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
      }
    });
  }

  /**
   * Sets up the heartbeat.
   */
  private void setupHeartbeat(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    if (doneHandler != null) {
      future.setHandler(doneHandler);
    }

    eventBus.sendWithTimeout(networkAddress, new JsonObject().putString("action", "register").putString("address", address), 10000, new Handler<AsyncResult<Message<String>>>() {
      @Override
      public void handle(AsyncResult<Message<String>> result) {
        if (result.succeeded()) {
          String heartbeatAddress = result.result().body();
          heartbeat.setAddress(heartbeatAddress);
          heartbeat.setInterval(context.getComponent().getHeartbeatInterval());
          heartbeat.start();
          future.setResult(null);
        }
        else {
          future.setFailure(new VertigoException("Failed to fetch heartbeat address from network."));
        }
      }
    });
  }

  /**
   * Sets up component input.
   */
  private void setupInput(Handler<AsyncResult<Void>> doneHandler) {
    input.start(doneHandler);
  }

  /**
   * Sets up component output.
   */
  private void setupOutput(Handler<AsyncResult<Void>> doneHandler) {
    output.start(doneHandler);
  }

  /**
   * Indicates to the network that the component is ready.
   */
  private void ready(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    if (doneHandler != null) {
      future.setHandler(doneHandler);
    }

    eventBus.send(networkAddress, new JsonObject().putString("action", "ready").putString("id", instanceId), new Handler<Message<Void>>() {
      @Override
      public void handle(Message<Void> message) {
        future.setResult(null);
      }
    });
  }

}
