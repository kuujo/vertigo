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

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.BasicFeeder;
import net.kuujo.vertigo.feeder.DefaultBasicFeeder;
import net.kuujo.vertigo.feeder.DefaultPollingFeeder;
import net.kuujo.vertigo.feeder.DefaultStreamFeeder;
import net.kuujo.vertigo.feeder.PollingFeeder;
import net.kuujo.vertigo.feeder.StreamFeeder;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.rpc.BasicExecutor;
import net.kuujo.vertigo.rpc.DefaultBasicExecutor;
import net.kuujo.vertigo.rpc.DefaultPollingExecutor;
import net.kuujo.vertigo.rpc.DefaultStreamExecutor;
import net.kuujo.vertigo.rpc.PollingExecutor;
import net.kuujo.vertigo.rpc.StreamExecutor;
import net.kuujo.vertigo.worker.BasicWorker;
import net.kuujo.vertigo.worker.Worker;

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
  private InstanceContext context;

  public DefaultVertigo(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public void setContext(InstanceContext context) {
    this.context = context;
  }

  @Override
  public Network createNetwork(String address) {
    return new Network(address);
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
  public PollingExecutor createPollingExecutor() {
    return new DefaultPollingExecutor(vertx, container, context);
  }

  @Override
  public StreamExecutor createStreamExecutor() {
    return new DefaultStreamExecutor(vertx, container, context);
  }

  @Override
  public Worker createWorker() {
    return new BasicWorker(vertx, container, context);
  }

}
