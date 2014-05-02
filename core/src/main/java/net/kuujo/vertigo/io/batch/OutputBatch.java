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

import net.kuujo.vertigo.io.Output;
import net.kuujo.vertigo.io.OutputGroupSupport;

/**
 * Sendable batch of messages.<p>
 *
 * Output batches represent collections of messages during a time window.
 * Each port may only have one batch associated with it at any given time,
 * and only once the batch has been ended can the next batch be created.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface OutputBatch extends Batch<OutputBatch>, Output<OutputBatch>, OutputGroupSupport<OutputBatch> {

  /**
   * Ends the batch.
   */
  void end();

}
