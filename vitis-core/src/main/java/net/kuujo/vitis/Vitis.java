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
import net.kuujo.vitis.node.worker.Worker;

/**
 * Primary Vitis API.
 *
 * This API mimics the core Vertx API. It allows users to create common Vitis
 * feeders, workers, and executors from within a VitisVerticle implementation.
 *
 * @author Jordan Halterman
 */
public interface Vitis {

  public void setContext(WorkerContext context);

  @SuppressWarnings("rawtypes")
  public PollFeeder createPollFeeder();

  @SuppressWarnings("rawtypes")
  public StreamFeeder createStreamFeeder();

  @SuppressWarnings("rawtypes")
  public EmbeddedFeeder createEmbeddedFeeder();

  @SuppressWarnings("rawtypes")
  public EmbeddedStreamFeeder createEmbeddedStreamFeeder();

  public RichPollFeeder createRichPollFeeder();

  public RichStreamFeeder createRichStreamFeeder();

  public ReliablePollFeeder createReliablePollFeeder();

  public ReliableEmbeddedFeeder createReliableEmbeddedFeeder();

  public ReliableStreamFeeder createReliableStreamFeeder();

  public ReliableEmbeddedStreamFeeder createReliableEmbeddedStreamFeeder();

  public Worker createWorker();

}
