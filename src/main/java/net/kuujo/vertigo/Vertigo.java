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
package net.kuujo.vertigo;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

import net.kuujo.vertigo.aggregator.Aggregator;
import net.kuujo.vertigo.aggregator.BasicAggregator;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.BasicFeeder;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.filter.BasicFilter;
import net.kuujo.vertigo.filter.Filter;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.rpc.BasicExecutor;
import net.kuujo.vertigo.rpc.Executor;
import net.kuujo.vertigo.splitter.BasicSplitter;
import net.kuujo.vertigo.splitter.Splitter;
import net.kuujo.vertigo.worker.BasicWorker;
import net.kuujo.vertigo.worker.Worker;

/**
 * Primary Vert.igo API.
 *
 * This is the primary API for creating Vertigo objects within component
 * implementations. This should be used to instantiate any feeders, workers, or
 * executors that are used by the component implementation.
 *
 * @author Jordan Halterman
 */
public final class Vertigo {
  private Vertx vertx;
  private Container container;
  private InstanceContext context;

  public Vertigo(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public Vertigo(Vertx vertx, Container container) {
    JsonObject config = container.config();
    if (config != null && config.getFieldNames().contains("__context__")) {
      JsonObject contextInfo = config.getObject("__context__");
      context = InstanceContext.fromJson(contextInfo);
    }
    this.vertx = vertx;
    this.container = container;
  }

  /**
   * Indicates whether this verticle was deployed as a component instance.
   *
   * @return
   *  Indicates whether this verticle is a Vertigo component instance.
   */
  public boolean isComponent() {
    return context != null;
  }

  /**
   * Returns the current Vertigo instance context (if any).
   *
   * @return
   *   The current Vertigo instance context.
   */
  public InstanceContext getContext() {
    return context;
  }

  /**
   * Creates a new network.
   *
   * @param address
   *   The network address.
   * @return
   *   A new network instance.
   */
  public Network createNetwork(String address) {
    return new Network(address);
  }

  /**
   * Creates a basic feeder.
   *
   * @return
   *   A new feeder instance.
   */
  public Feeder createFeeder() {
    return new BasicFeeder(vertx, container, context);
  }

  /**
   * Creates a basic executor.
   *
   * @return
   *   A new executor instance.
   */
  public Executor createExecutor() {
    return new BasicExecutor(vertx, container, context);
  }

  /**
   * Creates a worker.
   *
   * @return
   *   A new worker instance.
   */
  public Worker createWorker() {
    return new BasicWorker(vertx, container, context);
  }

  /**
   * Creates a filter.
   *
   * @return
   *   A new filter instance.
   */
  public Filter createFilter() {
    return new BasicFilter(vertx, container, context);
  }

  /**
   * Creates a splitter.
   *
   * @return
   *   A new splitter instance.
   */
  public Splitter createSplitter() {
    return new BasicSplitter(vertx, container, context);
  }

  /**
   * Creates an aggregator.
   *
   * @return
   *   A new aggregator instance.
   */
  public <T> Aggregator<T> createAggregator() {
    return new BasicAggregator<T>(vertx, container, context);
  }

}
