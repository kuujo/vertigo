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
package net.kuujo.vevent.feeder;

import net.kuujo.vevent.context.RootContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/**
 * A basic feeder.
 *
 * @author Jordan Halterman
 */
public class BasicFeeder implements Feeder {

  private EventBus eventBus;

  private RootContext context;

  public BasicFeeder(EventBus eventBus, RootContext context) {
    this.eventBus = eventBus;
    this.context = context;
  }

  @Override
  public Feeder feedHandler(Handler<Feeder> handler) {
    return this;
  }

  @Override
  public Feeder feed(JsonObject data) {
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler) {
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, String tag) {
    return this;
  }

  @Override
  public Feeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> doneHandler) {
    return this;
  }

}
