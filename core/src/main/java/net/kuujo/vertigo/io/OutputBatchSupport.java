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

import net.kuujo.vertigo.io.batch.OutputBatch;

import org.vertx.java.core.Handler;

/**
 * Support for creating output batches.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The type to which the support belongs.
 */
public interface OutputBatchSupport<T extends OutputBatchSupport<T>> {

  /**
   * Creates a new output batch with a random identifier.
   *
   * @param handler A handler to be called once the batch has been created.
   * @return The called object.
   */
  T batch(Handler<OutputBatch> handler);

  /**
   * Creates a new output batch.
   *
   * @param id The unique output batch identifier.
   * @param handler A handler to be called once the batch has been created.
   * @return The called object.
   */
  T batch(String id, Handler<OutputBatch> handler);

}
