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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A recurring rich embedded feeder implementation.
 *
 * @author Jordan Halterman
 */
public class RecurringRichEmbeddedFeeder implements RichEmbeddedFeeder {

  @Override
  public RichEmbeddedFeeder setDefaultTimeout(long timeout) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder feed(JsonObject data,
      Handler<AsyncResult<Void>> ackHandler) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder feed(JsonObject data, long timeout,
      Handler<AsyncResult<Void>> ackHandler) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder feed(JsonObject data, String tag,
      Handler<AsyncResult<Void>> ackHandler) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder feed(JsonObject data, String tag, long timeout,
      Handler<AsyncResult<Void>> ackHandler) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder setFeedQueueMaxSize(long maxSize) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder feed(JsonObject data) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder feed(JsonObject data, String tag) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder feedHandler(Handler<RichEmbeddedFeeder> handler) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder ackHandler(Handler<JsonObject> ackHandler) {
    return this;
  }

  @Override
  public RichEmbeddedFeeder failHandler(Handler<JsonObject> failHandler) {
    return this;
  }

}
