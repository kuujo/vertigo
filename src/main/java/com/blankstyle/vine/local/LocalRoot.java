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

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.platform.Container;

import com.blankstyle.vine.BasicFeeder;
import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.RootException;
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.eventbus.vine.VineVerticle;

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

  protected static final String VINE_VERTICLE_CLASS = VineVerticle.class.getName();

  protected String address = "vine.root";

  protected Vertx vertx;

  protected Container container;

  private Map<String, String> deploymentMap = new HashMap<String, String>();

  public LocalRoot(String address, Vertx vertx, Container container) {
    this.address = address;
    this.vertx = vertx;
    this.container = container;
  }

  public LocalRoot(String address) {
    this.address = address;
  }

  public LocalRoot(Vertx vertx, Container container) {
    this.vertx = vertx;
    this.container = container;
  }

  @Override
  public Root setAddress(String address) {
    this.address = address;
    return this;
  }

  @Override
  public String getAddress() {
    return address;
  }

  @Override
  public Root setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public Vertx getVertx() {
    return vertx;
  }

  @Override
  public Root setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  public Container getContainer() {
    return container;
  }

  @Override
  public void deploy(VineDefinition definition) {
    deploy(definition, null);
  }

  @Override
  public void deploy(VineDefinition definition, final Handler<AsyncResult<Feeder>> feedHandler) {
    final Future<Feeder> future = new DefaultFutureResult<Feeder>();
    future.setHandler(feedHandler);

    final String address = definition.getAddress();
    if (deploymentMap.containsKey(address)) {
      future.setFailure(new RootException("Cannot redeploy vine."));
      return;
    }

    if (feedHandler == null) {
      container.deployVerticle(VINE_VERTICLE_CLASS, definition.serialize());
    }
    else {
      container.deployVerticle(VINE_VERTICLE_CLASS, definition.serialize(), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.succeeded()) {
            deploymentMap.put(address, result.result());
            future.setResult(new BasicFeeder(address, vertx.eventBus()));
          }
          else {
            future.setFailure(result.cause());
          }
        }
      });
    }
  }

}
