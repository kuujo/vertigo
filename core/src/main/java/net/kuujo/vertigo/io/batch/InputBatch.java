/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.io.batch;

import net.kuujo.vertigo.io.Input;
import net.kuujo.vertigo.io.InputGroupSupport;

import org.vertx.java.core.Handler;

/**
 * Receivable batch of messages.<p>
 *
 * Input batches are received when an output batch is created and sent.
 * Each input batch has a beginning and end, and Vertigo guarantees that
 * a batch will not receive messages until its start handler is called
 * and will not receive messages after its end handler is called.
 *
 * @author Jordan Halterman
 */
public interface InputBatch extends Batch<InputBatch>, Input<InputBatch>, InputGroupSupport<InputBatch> {

  /**
   * Registers a start handler on the batch.
   *
   * @param handler A handler to be called when the batch is started.
   * @return The input batch.
   */
  InputBatch startHandler(Handler<Void> handler);

  /**
   * Registers an end handler on the batch.
   *
   * @param handler A handler to be called when the batch is ended.
   * @return The input batch.
   */
  InputBatch endHandler(Handler<Void> handler);

}
