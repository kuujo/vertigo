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
package net.kuujo.vevent.java;

import net.kuujo.vevent.feeder.Feeder;

import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * An abstract root verticle.
 *
 * @author Jordan Halterman
 */
abstract class RootVerticle extends Verticle {

  private Feeder feeder;

  /**
   * Creates a root feeder instance.
   *
   * @return
   *   A new root feeder instance.
   */
  abstract Feeder createFeeder();

  @Override
  public void start() {
    feeder = createFeeder();
  }

  /**
   * Primary method for feeding data.
   */
  protected abstract void feed();

  /**
   * Emits data to all output streams.
   *
   * @param data
   *   The data to emit.
   */
  protected void emit(final JsonObject data) {
    feeder.feed(data, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          if (!feeder.feedQueueFull()) {
            feed();
          }
        }
        else {
          emit(data);
        }
      }
    });
  }

  /**
   * Emits data to all output streams.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A message tag.
   */
  protected void emit(final JsonObject data, final String tag) {
    feeder.feed(data, tag, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          if (!feeder.feedQueueFull()) {
            feed();
          }
        }
        else {
          emit(data, tag);
        }
      }
    });
  }

}
