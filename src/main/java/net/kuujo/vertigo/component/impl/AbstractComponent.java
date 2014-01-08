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
package net.kuujo.vertigo.component.impl;

import java.util.ArrayList;
import java.util.List;

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.output.impl.DefaultOutputCollector;
import net.kuujo.vertigo.message.schema.MessageSchema;
import net.kuujo.vertigo.acker.Acker;
import net.kuujo.vertigo.acker.DefaultAcker;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.coordinator.heartbeat.HeartbeatEmitter;
import net.kuujo.vertigo.coordinator.heartbeat.impl.DefaultHeartbeatEmitter;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.input.impl.DefaultInputCollector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Container;

/**
 * A base component.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractComponent<T extends Component<T>> implements Component<T> {
  protected final Vertx vertx;
  protected final EventBus eventBus;
  protected final Container container;
  protected final Logger logger;
  protected final InstanceContext<T> context;
  protected final Acker acker;
  protected final String instanceId;
  protected final String address;
  protected final String networkAddress;
  protected final HeartbeatEmitter heartbeat;
  protected final InputCollector input;
  protected final OutputCollector output;
  protected final List<ComponentHook> hooks = new ArrayList<>();

  private InputHook inputHook = new InputHook() {
    @Override
    public void handleStart(InputCollector subject) {
      // Do nothing. This hook is called elsewhere.
    }
    @Override
    public void handleReceive(MessageId id) {
      for (ComponentHook hook : hooks) {
        hook.handleReceive(id);
      }
    }
    @Override
    public void handleAck(MessageId id) {
      for (ComponentHook hook : hooks) {
        hook.handleAck(id);
      }
    }
    @Override
    public void handleFail(MessageId id) {
      for (ComponentHook hook : hooks) {
        hook.handleFail(id);
      }
    }
    @Override
    public void handleStop(InputCollector subject) {
      // Do nothing. This hook is called elsewhere.
    }
  };

  private OutputHook outputHook = new OutputHook() {
    @Override
    public void handleStart(OutputCollector subject) {
      // Do nothing. This hook is called elsewhere.
    }
    @Override
    public void handleEmit(MessageId id) {
      for (ComponentHook hook : hooks) {
        hook.handleEmit(id);
      }
    }
    @Override
    public void handleAcked(MessageId id) {
      for (ComponentHook hook : hooks) {
        hook.handleAcked(id);
      }
    }
    @Override
    public void handleFailed(MessageId id) {
      for (ComponentHook hook : hooks) {
        hook.handleFailed(id);
      }
    }
    @Override
    public void handleTimeout(MessageId id) {
      for (ComponentHook hook : hooks) {
        hook.handleTimeout(id);
      }
    }
    @Override
    public void handleStop(OutputCollector subject) {
      // Do nothing. This hook is called elsewhere.
    }
  };

  protected AbstractComponent(Vertx vertx, Container container, InstanceContext<T> context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.container = container;
    this.logger = LoggerFactory.getLogger(context.address());
    this.context = context;
    this.acker = new DefaultAcker(context.address(), eventBus);
    this.instanceId = context.address();
    this.address = context.componentContext().address();
    NetworkContext networkContext = context.componentContext().networkContext();
    networkAddress = networkContext.address();
    heartbeat = new DefaultHeartbeatEmitter(vertx);
    input = new DefaultInputCollector(vertx, container, context, acker);
    output = new DefaultOutputCollector(vertx, container, context, acker);
    for (ComponentHook hook : context.<ComponentContext<?>>componentContext().hooks()) {
      addHook(hook);
    }
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public Vertx getVertx() {
    return vertx();
  }

  @Override
  public Container container() {
    return container;
  }

  @Override
  public Container getContainer() {
    return container();
  }

  @Override
  public InputCollector input() {
    return input;
  }

  @Override
  public InputCollector getInput() {
    return input();
  }

  @Override
  public OutputCollector output() {
    return output;
  }

  @Override
  public OutputCollector getOutput() {
    return output();
  }

  @Override
  public InstanceContext<T> context() {
    return context;
  }

  @Override
  public InstanceContext<T> getContext() {
    return context();
  }

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T addHook(ComponentHook hook) {
    if (hooks.isEmpty()) {
      input.addHook(inputHook);
      output.addHook(outputHook);
    }
    hooks.add(hook);
    return (T) this;
  }

  /**
   * Calls start hooks.
   */
  private void hookStart() {
    for (ComponentHook hook : hooks) {
      hook.handleStart(this);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public T declareSchema(MessageSchema schema) {
    input.declareSchema(schema);
    return (T) this;
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
          acker.start(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                future.setFailure(result.cause());
              }
              else {
                output.start(new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      future.setFailure(result.cause());
                    }
                    else {
                      input.start(new Handler<AsyncResult<Void>>() {
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
          heartbeat.setInterval(context.componentContext().heartbeatInterval());
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
          future.setResult((T) AbstractComponent.this);
        }
      }
    });
    return (T) this;
  }

}
