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

import net.kuujo.vertigo.component.executor.BasicExecutor;
import net.kuujo.vertigo.component.executor.DefaultBasicExecutor;
import net.kuujo.vertigo.component.feeder.BasicFeeder;
import net.kuujo.vertigo.component.feeder.DefaultBasicFeeder;
import net.kuujo.vertigo.component.feeder.DefaultPollingFeeder;
import net.kuujo.vertigo.component.feeder.DefaultStreamFeeder;
import net.kuujo.vertigo.component.feeder.PollingFeeder;
import net.kuujo.vertigo.component.feeder.StreamFeeder;
import net.kuujo.vertigo.component.worker.BasicWorker;
import net.kuujo.vertigo.component.worker.Worker;
import net.kuujo.vertigo.context.WorkerContext;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * A default Vertigo implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultVertigo implements Vertigo {

  private Vertx vertx;

  private Container container;

  private WorkerContext context;

  public DefaultVertigo(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public void setContext(WorkerContext context) {
    this.context = context;
  }

  @Override
  public BasicFeeder createFeeder() {
    return new DefaultBasicFeeder(vertx, container, context);
  }

  @Override
  public BasicFeeder createBasicFeeder() {
    return new DefaultBasicFeeder(vertx, container, context);
  }

  @Override
  public PollingFeeder createPollingFeeder() {
    return new DefaultPollingFeeder(vertx, container, context);
  }

  @Override
  public StreamFeeder createStreamFeeder() {
    return new DefaultStreamFeeder(vertx, container, context);
  }

  @Override
  public BasicExecutor createExecutor() {
    return new DefaultBasicExecutor(vertx, container, context);
  }

  @Override
  public BasicExecutor createBasicExecutor() {
    return new DefaultBasicExecutor(vertx, container, context);
  }

  @Override
  public Worker createWorker() {
    return new BasicWorker(vertx, container, context);
  }

}
