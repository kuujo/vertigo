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
import net.kuujo.vitis.messaging.DefaultJsonMessage;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A fallible feeder implementation.
 *
 * @author Jordan Halterman
 */
public class FallibleFeeder extends AbstractFeeder implements BasicFeeder<FallibleFeeder> {

  protected long maxQueueSize = 1000;

  protected FallibleFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public FallibleFeeder setFeedQueueMaxSize(long maxSize) {
    this.maxQueueSize = maxSize;
    return this;
  }

  @Override
  public FallibleFeeder feed(JsonObject data) {
    output.emit(DefaultJsonMessage.create(data));
    return this;
  }

  @Override
  public FallibleFeeder feed(JsonObject data, String tag) {
    output.emit(DefaultJsonMessage.create(data, tag));
    return this;
  }

}
