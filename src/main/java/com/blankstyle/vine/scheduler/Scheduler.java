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
package com.blankstyle.vine.scheduler;

import java.util.Collection;

import com.blankstyle.vine.Stem;
import com.blankstyle.vine.context.WorkerContext;

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
   * @return
   *   The address of the machine to which the worker is assigned.
   */
  public String assign(WorkerContext context, Collection<Stem> stems);

}
