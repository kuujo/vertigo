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
import net.kuujo.vertigo.worker.BasicWorker;

/**
 * Primary Vert.igo API.
 *
 * This is the primary API for creating Vertigo objects within component
 * implementations. When implementing a new component by extending the
 * {@link VertigoVerticle} base class, an {@link Vertigo} instance will be
 * made available as a protected member. This should be used to instantiate any
 * feeders, workers, or executors that are used by the component implementation.
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
   * Gets the component instance context.
   *
   * The instance context can be used to retrieve useful information about an
   * entire network.
   *
   * @return
   *   The instance context.
   */
  InstanceContext getContext();

  /**
   * Creates a new network.
   *
   * @param address
   *   The network address.
   * @return
   *   A new network instance.
   */
  Network createNetwork(String address);

  /**
   * Creates a basic feeder.
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
   * Creates a basic worker.
   *
   * @return
   *   A new worker instance.
   */
  BasicWorker createWorker();

}
