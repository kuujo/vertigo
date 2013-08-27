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
 * A root context.
 *
 * @author Jordan Halterman
 */
public interface RootContext extends Context<RootContext>, Serializeable<JsonObject> {

  /**
   * Returns the root address.
   *
   * @return
   *   The root address.
   */
  public String getAddress();

}
