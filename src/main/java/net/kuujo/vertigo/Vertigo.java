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
import net.kuujo.vertigo.feeder.PollingFeeder;
import net.kuujo.vertigo.feeder.StreamFeeder;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.rpc.BasicExecutor;
import net.kuujo.vertigo.rpc.PollingExecutor;
import net.kuujo.vertigo.rpc.StreamExecutor;
import net.kuujo.vertigo.worker.Worker;

/**
 * Primary Vert.igo API.
 *
 * This API mimics the core Vertx API. It allows users to create common Vertigo
 * feeders, workers, and executors from within a VertigoVerticle implementation.
 *
 * @author Jordan Halterman
 */
public interface Vertigo {

  /**
   * Sets the component instance context.
   *
   * @param context
   *   An instance context.
   */
  void setContext(InstanceContext context);

  /**
   * Creates a network.
   *
   * @param address
   *   The network address.
   * @return
   *   A new network instance.
   */
  Network createNetwork(String address);

  /**
   * Creates a feeder.
   *
   * @return
   *   A new feeder instance.
   */
  BasicFeeder createFeeder();

  /**
   * Creates a basic feeder.
   *
   * @return
   *   A new basic feeder instance.
   */
  BasicFeeder createBasicFeeder();

  /**
   * Creates a polling feeder.
   *
   * @return
   *   A new poll feeder instance.
   */
  PollingFeeder createPollingFeeder();

  /**
   * Creates a stream feeder.
   *
   * @return
   *   A new stream feeder instance.
   */
  StreamFeeder createStreamFeeder();

  /**
   * Creates a basic executor.
   *
   * @return
   *   A new basic executor instance.
   */
  BasicExecutor createExecutor();

  /**
   * Creates a basic executor.
   *
   * @return
   *   A new basic executor instance.
   */
  BasicExecutor createBasicExecutor();

  /**
   * Creates a polling executor.
   *
   * @return
   *   A new polling executor instance.
   */
  PollingExecutor createPollingExecutor();

  /**
   * Creates a stream executor.
   *
   * @return
   *   A new stream executor instance.
   */
  StreamExecutor createStreamExecutor();

  /**
   * Creates a worker.
   *
   * @return
   *   A new worker instance.
   */
  Worker createWorker();

}
