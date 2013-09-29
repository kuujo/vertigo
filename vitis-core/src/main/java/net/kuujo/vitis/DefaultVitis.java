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
import net.kuujo.vitis.node.feeder.BasicEmbeddedFeeder;
import net.kuujo.vitis.node.feeder.BasicEmbeddedStreamFeeder;
import net.kuujo.vitis.node.feeder.BasicPollFeeder;
import net.kuujo.vitis.node.feeder.BasicStreamFeeder;
import net.kuujo.vitis.node.feeder.EmbeddedFeeder;
import net.kuujo.vitis.node.feeder.EmbeddedStreamFeeder;
import net.kuujo.vitis.node.feeder.PollFeeder;
import net.kuujo.vitis.node.feeder.ReliableEmbeddedFeeder;
import net.kuujo.vitis.node.feeder.ReliableEmbeddedStreamFeeder;
import net.kuujo.vitis.node.feeder.ReliablePollFeeder;
import net.kuujo.vitis.node.feeder.ReliableStreamFeeder;
import net.kuujo.vitis.node.feeder.RichPollFeeder;
import net.kuujo.vitis.node.feeder.RichStreamFeeder;
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
  @SuppressWarnings("rawtypes")
  public PollFeeder createPollFeeder() {
    return new BasicPollFeeder(vertx, container, context);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public StreamFeeder createStreamFeeder() {
    return new BasicStreamFeeder(vertx, container, context);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public EmbeddedFeeder createEmbeddedFeeder() {
    return new BasicEmbeddedFeeder(vertx, container, context);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public EmbeddedStreamFeeder createEmbeddedStreamFeeder() {
    return new BasicEmbeddedStreamFeeder(vertx, container, context);
  }

  @Override
  public RichPollFeeder createRichPollFeeder() {
    return new RichPollFeeder(vertx, container, context);
  }

  @Override
  public RichStreamFeeder createRichStreamFeeder() {
    return new RichStreamFeeder(vertx, container, context);
  }

  @Override
  public ReliablePollFeeder createReliablePollFeeder() {
    return new ReliablePollFeeder(vertx, container, context);
  }

  @Override
  public ReliableEmbeddedFeeder createReliableEmbeddedFeeder() {
    return new ReliableEmbeddedFeeder(vertx, container, context);
  }

  @Override
  public ReliableStreamFeeder createReliableStreamFeeder() {
    return new ReliableStreamFeeder(vertx, container, context);
  }

  @Override
  public ReliableEmbeddedStreamFeeder createReliableEmbeddedStreamFeeder() {
    return new ReliableEmbeddedStreamFeeder(vertx, container, context);
  }

  @Override
  public Worker createWorker() {
    return new BasicWorker(vertx, container, context);
  }

}
