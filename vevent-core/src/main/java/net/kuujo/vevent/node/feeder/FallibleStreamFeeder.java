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
 * A fallible stream feeder implementation.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("rawtypes")
public class FallibleStreamFeeder implements StreamFeeder<StreamFeeder> {

  @Override
  public StreamFeeder setFeedQueueMaxSize(long maxSize) {
    return this;
  }

  @Override
  public boolean feedQueueFull() {
    return false;
  }

  @Override
  public StreamFeeder drainHandler(Handler<Void> handler) {
    return this;
  }

  @Override
  public StreamFeeder feed(JsonObject data) {
    return this;
  }

  @Override
  public StreamFeeder feed(JsonObject data, String tag) {
    return this;
  }

}
