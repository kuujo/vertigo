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
package com.blankstyle.vine.context;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Context;
import com.blankstyle.vine.Serializeable;

/**
 * A worker context.
 *
 * @author Jordan Halterman
 */
public interface WorkerContext extends Context<WorkerContext>, Serializeable<JsonObject> {

  /**
   * Gets the worker address.
   *
   * @return
   *   The worker address.
   */
  public String getAddress();

  /**
   * Sets the worker address.
   *
   * @param address
   *   The worker address.
   * @return
   *   The called worker context instance.
   */
  public WorkerContext setAddress(String address);

  /**
   * Gets the parent seed context.
   *
   * @return
   *   The parent seed context.
   */
  public SeedContext getContext();

  /**
   * Sets the parent seed context.
   *
   * @param context
   *   The parent seed context.
   * @return
   *   The called worker context instance.
   */
  public WorkerContext setContext(SeedContext context);

}
