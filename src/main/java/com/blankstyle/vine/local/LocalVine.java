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
package com.blankstyle.vine.local;

import java.util.Collection;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.Seed;
import com.blankstyle.vine.Vine;

/**
 * A local vine implementation.
 *
 * @author Jordan Halterman
 */
public class LocalVine extends Vine {

  private Vertx vertx;

  private Container container;

  public LocalVine(JsonObject context) {
    super(context);
  }

  /**
   * Creates a new vine from a JSON context.
   *
   * @param context
   *   The JSON object context.
   * @return
   *   A new vine.
   */
  public static Vine create(JsonObject context) {
    return new LocalVine(context);
  }

  @Override
  public void setVertx(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public void setContainer(Container container) {
    this.container = container;
  }

  @Override
  public Container getContainer() {
    return container;
  }

  @Override
  public void deploy(Collection<Seed> seeds) {
    
  }

  @Override
  public void deploy(Collection<Seed> seeds, Handler<AsyncResult<String>> doneHandler) {
    
  }

}
