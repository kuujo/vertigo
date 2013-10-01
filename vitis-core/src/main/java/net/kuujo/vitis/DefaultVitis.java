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
package net.kuujo.vitis;

import net.kuujo.vitis.context.WorkerContext;
import net.kuujo.vitis.node.feeder.BasicFeeder;
import net.kuujo.vitis.node.feeder.DefaultBasicFeeder;
import net.kuujo.vitis.node.feeder.DefaultPollingFeeder;
import net.kuujo.vitis.node.feeder.DefaultStreamFeeder;
import net.kuujo.vitis.node.feeder.PollingFeeder;
import net.kuujo.vitis.node.feeder.StreamFeeder;
import net.kuujo.vitis.node.worker.BasicWorker;
import net.kuujo.vitis.node.worker.Worker;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * A default Vitis implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultVitis implements Vitis {

  private Vertx vertx;

  private Container container;

  private WorkerContext context;

  public DefaultVitis(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public void setContext(WorkerContext context) {
    this.context = context;
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
  public Worker createWorker() {
    return new BasicWorker(vertx, container, context);
  }

}
