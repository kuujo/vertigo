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
package net.kuujo.vertigo.testtools;

import java.util.UUID;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.feeder.StreamFeeder;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A test feeder that checks that a message was failed.
 *
 * @author Jordan Halterman
 */
public class TestFailingFeeder extends Verticle {

  /**
   * Creates a fail checking feeder definition.
   *
   * @param data
   *   The data to evaluate.
   * @return
   *   A component definition.
   */
  public static net.kuujo.vertigo.network.Verticle createDefinition(JsonObject data) {
    return new net.kuujo.vertigo.network.Verticle(UUID.randomUUID().toString())
        .setMain(TestFailingFeeder.class.getName()).setConfig(data);
  }

  @Override
  public void start() {
    Vertigo vertigo = new Vertigo(this);
    vertigo.createStreamFeeder().start(new Handler<AsyncResult<StreamFeeder>>() {
      @Override
      public void handle(AsyncResult<StreamFeeder> result) {
        if (result.failed()) {
          container.logger().error(result.cause());
        }
        else {
          StreamFeeder feeder = result.result();
          feeder.emit(container.config(), new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              assertTrue(result.failed());
              testComplete();
            }
          });
        }
      }
    });
  }

}
