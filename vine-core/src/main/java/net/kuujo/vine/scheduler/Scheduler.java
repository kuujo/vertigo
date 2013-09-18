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
package net.kuujo.vine.scheduler;

import java.util.Collection;

import net.kuujo.vine.Stem;
import net.kuujo.vine.context.WorkerContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A task scheduler.
 *
 * @author Jordan Halterman
 */
public interface Scheduler {

  /**
   * Assigns a worker to a machine.
   *
   * @param context
   *   The worker context.
   * @param stems
   *   A collection of stems.
   * @param resultHandler
   *   A handler to be invoked with the address of the stem to which the
   *   worker was assigned.
   */
  public void assign(WorkerContext context, Collection<Stem> stems, Handler<AsyncResult<String>> resultHandler);

}
