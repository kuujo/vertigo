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
package net.kuujo.vitis.node.feeder;

import net.kuujo.vitis.context.WorkerContext;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A fallible pull feeder.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("rawtypes")
public class FalliblePullFeeder extends AbstractFeeder implements PullFeeder<PullFeeder> {

  protected long maxQueueSize = 1000;

  protected Handler<PullFeeder> feedHandler;

  protected FalliblePullFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public PullFeeder setFeedQueueMaxSize(long maxSize) {
    maxQueueSize = maxSize;
    return this;
  }

  @Override
  public PullFeeder feedHandler(Handler<PullFeeder> handler) {
    this.feedHandler = handler;
    return this;
  }

  @Override
  public PullFeeder feed(JsonObject data) {
    return this;
  }

  @Override
  public PullFeeder feed(JsonObject data, String tag) {
    return this;
  }

}
