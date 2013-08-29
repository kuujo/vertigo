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
package com.blankstyle.vine.remote;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Stem;
import com.blankstyle.vine.context.WorkerContext;

/**
 * A remote reference to a stem verticle.
 *
 * @author Jordan Halterman
 */
public class RemoteStem implements Stem {

  protected final String address;

  protected EventBus eventBus;

  public RemoteStem(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  @Override
  public void assign(WorkerContext context, final Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>();
    eventBus.send(address, context.serialize(), new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        future.setResult(null);
        doneHandler.handle(future);
      }
    });
  }

  @Override
  public void release(WorkerContext context) {
    
  }

}
