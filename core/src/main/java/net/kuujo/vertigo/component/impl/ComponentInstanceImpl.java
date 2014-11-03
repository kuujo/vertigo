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
package net.kuujo.vertigo.component.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.component.ComponentInstance;
import net.kuujo.vertigo.component.PartitionContext;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.input.impl.InputCollectorImpl;
import net.kuujo.vertigo.output.impl.OutputCollectorImpl;
import net.kuujo.vertigo.util.CountingCompletionHandler;

/**
 * Component partition implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentInstanceImpl implements ComponentInstance {
  private final Vertx vertx;
  private final PartitionContext context;
  private final InputCollector input;
  private final OutputCollector output;
  private final Logger logger;

  public ComponentInstanceImpl(Vertx vertx, PartitionContext context) {
    this.vertx = vertx;
    this.context = context;
    this.input = new InputCollectorImpl(vertx, context.input());
    this.output = new OutputCollectorImpl(vertx, context.output());
    this.logger = LoggerFactory.getLogger(String.format("%s-%s", ComponentInstance.class.getName(), context.id()));
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public PartitionContext context() {
    return context;
  }

  @Override
  public InputCollector input() {
    return input;
  }

  @Override
  public OutputCollector output() {
    return output;
  }

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  public ComponentInstance start() {
    return start(null);
  }

  @Override
  public ComponentInstance start(Handler<AsyncResult<Void>> doneHandler) {
    CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(2).setHandler(doneHandler);
    output.open(counter);
    input.open(counter);
    return this;
  }

  @Override
  public void stop() {
    stop(null);
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(2).setHandler(doneHandler);
    input.close(counter);
    output.close(counter);
  }

}
