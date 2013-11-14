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
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.hooks.OutputHook;
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
 * A base component.
 *
 * @author Jordan Halterman
 */
public abstract class ComponentBase<T> implements Component<T> {
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
  protected final List<ComponentHook> hooks;

  private InputHook inputHook = new InputHook() {
    @Override
    public void start(InputCollector subject) {
      // Do nothing. This hook is called elsewhere.
    }
    @Override
    public void received(String id) {
      for (ComponentHook hook : hooks) {
        hook.received(id);
      }
    }
    @Override
    public void ack(String id) {
      for (ComponentHook hook : hooks) {
        hook.ack(id);
      }
    }
    @Override
    public void fail(String id) {
      for (ComponentHook hook : hooks) {
        hook.fail(id);
      }
    }
    @Override
    public void stop(InputCollector subject) {
      // Do nothing. This hook is called elsewhere.
    }
  };

  private OutputHook outputHook = new OutputHook() {
    @Override
    public void start(OutputCollector subject) {
      // Do nothing. This hook is called elsewhere.
    }
    @Override
    public void emit(String id) {
      for (ComponentHook hook : hooks) {
        hook.emit(id);
      }
    }
    @Override
    public void acked(String id) {
      for (ComponentHook hook : hooks) {
        hook.acked(id);
      }
    }
    @Override
    public void failed(String id) {
      for (ComponentHook hook : hooks) {
        hook.failed(id);
      }
    }
    @Override
    public void stop(OutputCollector subject) {
      // Do nothing. This hook is called elsewhere.
    }
  };

  protected ComponentBase(Vertx vertx, Container container, InstanceContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.container = container;
    this.logger = container.logger();
    this.context = context;
    this.hooks = context.getComponent().getComponentHooks();
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

    // Add input hooks to the input collector.
    input.addHook(inputHook);
    for (InputHook inputHook : context.getComponent().getInputHooks()) {
      input.addHook(inputHook);
    }

    output = new DefaultOutputCollector(vertx, container, context);

    // Add output hooks to the output collector.
    for (OutputHook outputHook : context.getComponent().getOutputHooks()) {
      output.addHook(outputHook);
    }
    output.addHook(outputHook);
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public Container getContainer() {
    return container;
  }

  @Override
  public InputCollector getInput() {
    return input;
  }

  @Override
  public OutputCollector getOutput() {
    return output;
  }

  @Override
  public InstanceContext getContext() {
    return context;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T addHook(ComponentHook hook) {
    hooks.add(hook);
    return (T) this;
  }

  /**
   * Calls start hooks.
   */
  private void hookStart() {
    for (ComponentHook hook : hooks) {
      hook.start(this);
    }
  }

  /**
   * Sets up the component.
   */
  private void setup(Handler<AsyncResult<Void>> doneHandler) {
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
          setupOutput(new Handler<AsyncResult<Void>>() {
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

  @Override
  @SuppressWarnings("unchecked")
  public T start() {
    start(new Handler<AsyncResult<T>>() {
      @Override
      public void handle(AsyncResult<T> result) {
        // Do nothing.
      }
    });
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T start(Handler<AsyncResult<T>> doneHandler) {
    final Future<T> future = new DefaultFutureResult<T>().setHandler(doneHandler);
    setup(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          hookStart();
          future.setResult((T) ComponentBase.this);
        }
      }
    });
    return (T) this;
  }

}
