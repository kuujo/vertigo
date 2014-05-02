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

/**
 * Base interface for batches. Batches represent collections of messages over time.<p>
 *
 * As opposed to groups, batches are collections of messages that can be
 * sent to any target component instance. Batches simply delimit a group
 * of messages output from a given component during a time window.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 *
 * @param <T> The batch type.
 */
public interface Batch<T extends Batch<T>> {

  /**
   * Returns the unique batch ID.
   *
   * @return The unique batch ID.
   */
  String id();

}
