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
package com.blankstyle.vine.impl;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.RootException;
import com.blankstyle.vine.VineContext;

/**
 * A local root implementation.
 *
 * This root does not communicate with a remote root and seeds are not assigned
 * to specific machines. Seeds are simply started using the local Vert.x
 * container.
 *
 * @author Jordan Halterman
 */
public class LocalRoot implements Root {

  protected static final String VINE_VERTICLE_CLASS = "com.blankstyle.vine.VineVerticle";

  protected Vertx vertx;

  private Container container;

  private Map<String, String> deploymentMap = new HashMap<String, String>();

  public LocalRoot(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
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
  public void deploy(VineContext context, final Handler<AsyncResult<Feeder>> feedHandler) {
    deploy(context.getAddress(), context, feedHandler);
  }

  @Override
  public void deploy(final String name, VineContext context, final Handler<AsyncResult<Feeder>> feedHandler) {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    future.setHandler(feedHandler);

    final String address = context.getAddress();
    if (deploymentMap.containsKey(address)) {
      future.setFailure(new RootException("Cannot redeploy vine."));
      return;
    }

    container.deployVerticle(VINE_VERTICLE_CLASS, context.toJsonObject(), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          deploymentMap.put(address, result.result());
          future.setResult(new DefaultFeeder(address, vertx.eventBus()));
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

}
