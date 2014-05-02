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
package net.kuujo.vertigo.io;

import net.kuujo.vertigo.io.batch.InputBatch;

import org.vertx.java.core.Handler;

/**
 * Support for receiving input batches.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The type to which the support belongs.
 */
public interface InputBatchSupport<T extends InputBatchSupport<T>> {

  /**
   * Registers a batch handler.
   *
   * @param handler The handler to register. This handler will be called
   *        whenever a new batch is started.
   * @return The called object.
   */
  T batchHandler(Handler<InputBatch> handler);

}
