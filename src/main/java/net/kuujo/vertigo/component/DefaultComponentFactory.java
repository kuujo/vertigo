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
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T extends Component<T>> Component<T> createComponent(InstanceContext<T> context) {
    Class<T> type = context.getComponent().getType();
    if (Feeder.class.isAssignableFrom(type)) {
      return (Component<T>) createFeeder((InstanceContext<Feeder>) context);
    }
    else if (Executor.class.isAssignableFrom(type)) {
      return (Component<T>) createExecutor((InstanceContext<Executor>) context);
    }
    else if (Worker.class.isAssignableFrom(type)) {
      return (Component<T>) createWorker((InstanceContext<Worker>) context);
    }
    else if (Filter.class.isAssignableFrom(type)) {
      return (Component<T>) createFilter((InstanceContext<Filter>) context);
    }
    else if (Splitter.class.isAssignableFrom(type)) {
      return (Component<T>) createSplitter((InstanceContext<Splitter>) context);
    }
    else if (Aggregator.class.isAssignableFrom(type)) {
      return (Component<T>) new BasicAggregator(vertx, container, context);
    }
    else {
      throw new IllegalArgumentException("Invalid component type.");
    }
  }

  @Override
  public Feeder createFeeder(InstanceContext<Feeder> context) {
    return new BasicFeeder(vertx, container, context);
  }

  @Override
  public Executor createExecutor(InstanceContext<Executor> context) {
    return new BasicExecutor(vertx, container, context);
  }

  @Override
  public Worker createWorker(InstanceContext<Worker> context) {
    return new BasicWorker(vertx, container, context);
  }

  @Override
  public Filter createFilter(InstanceContext<Filter> context) {
    return new BasicFilter(vertx, container, context);
  }

  @Override
  public Splitter createSplitter(InstanceContext<Splitter> context) {
    return new BasicSplitter(vertx, container, context);
  }

  @Override
  public <T> Aggregator<T> createAggregator(InstanceContext<Aggregator<T>> context) {
    return new BasicAggregator<T>(vertx, container, context);
  }

}
