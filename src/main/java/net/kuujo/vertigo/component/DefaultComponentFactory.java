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

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.feeder.BasicFeeder;
import net.kuujo.vertigo.rpc.Executor;
import net.kuujo.vertigo.rpc.BasicExecutor;
import net.kuujo.vertigo.worker.Worker;
import net.kuujo.vertigo.worker.BasicWorker;
import net.kuujo.vertigo.filter.Filter;
import net.kuujo.vertigo.filter.BasicFilter;
import net.kuujo.vertigo.splitter.Splitter;
import net.kuujo.vertigo.splitter.BasicSplitter;
import net.kuujo.vertigo.aggregator.Aggregator;
import net.kuujo.vertigo.aggregator.BasicAggregator;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * A default component factory implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultComponentFactory implements ComponentFactory {
  private Vertx vertx;
  private Container container;

  public DefaultComponentFactory() {
  }

  public DefaultComponentFactory(Vertx vertx, Container container) {
    setVertx(vertx);
    setContainer(container);
  }

  @Override
  public ComponentFactory setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public ComponentFactory setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  public Feeder createFeeder(InstanceContext context) {
    return new BasicFeeder(vertx, container, context);
  }

  @Override
  public Executor createExecutor(InstanceContext context) {
    return new BasicExecutor(vertx, container, context);
  }

  @Override
  public Worker createWorker(InstanceContext context) {
    return new BasicWorker(vertx, container, context);
  }

  @Override
  public Filter createFilter(InstanceContext context) {
    return new BasicFilter(vertx, container, context);
  }

  @Override
  public Splitter createSplitter(InstanceContext context) {
    return new BasicSplitter(vertx, container, context);
  }

  @Override
  public <T> Aggregator<T> createAggregator(InstanceContext context) {
    return new BasicAggregator<T>(vertx, container, context);
  }

}
