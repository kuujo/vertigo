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
package net.kuujo.vevent.node.feeder;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A fallible embedded feeder implementation.
 *
 * @author Jordan Halterman
 */
public class FallibleEmbeddedFeeder implements BasicFeeder<FallibleEmbeddedFeeder>, EmbeddedFeeder<FallibleEmbeddedFeeder> {

  @Override
  public FallibleEmbeddedFeeder setFeedQueueMaxSize(long maxSize) {
    return this;
  }

  @Override
  public FallibleEmbeddedFeeder feedHandler(
      Handler<FallibleEmbeddedFeeder> handler) {
    return this;
  }

  @Override
  public FallibleEmbeddedFeeder ackHandler(Handler<JsonObject> ackHandler) {
    return this;
  }

  @Override
  public FallibleEmbeddedFeeder failHandler(Handler<JsonObject> failHandler) {
    return this;
  }

  @Override
  public FallibleEmbeddedFeeder feed(JsonObject data) {
    return this;
  }

  @Override
  public FallibleEmbeddedFeeder feed(JsonObject data, String tag) {
    return this;
  }

}
