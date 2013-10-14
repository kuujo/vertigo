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

import net.kuujo.vertigo.Component;
import net.kuujo.vertigo.component.executor.BasicExecutor;
import net.kuujo.vertigo.component.executor.DefaultBasicExecutor;
import net.kuujo.vertigo.java.VertigoVerticle;
import net.kuujo.vertigo.messaging.JsonMessage;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A test executor that checks output against input.
 *
 * @author Jordan Halterman
 */
public class TestResultCheckingExecutor extends VertigoVerticle {

  /**
   * Creates an ack checking feeder definition.
   *
   * @param input
   *   The input data.
   * @param output
   *   The expected output data.
   * @return
   *   A component definition.
   */
  public static Component createDefinition(JsonObject input, JsonObject output) {
    return new Component(UUID.randomUUID().toString())
        .setType(Component.VERTICLE).setMain(TestResultCheckingExecutor.class.getName())
        .setConfig(new JsonObject().putObject("input", input).putObject("output", output));
  }

  @Override
  public void start() {
    BasicExecutor executor = new DefaultBasicExecutor(vertx, container, context);
    executor.start(new Handler<AsyncResult<BasicExecutor>>() {
      @Override
      public void handle(AsyncResult<BasicExecutor> result) {
        if (result.failed()) {
          container.logger().error(result.cause());
        }
        else {
          final BasicExecutor executor = result.result();
          executor.execute(executor.config().getObject("input"), new Handler<AsyncResult<JsonMessage>>() {
            @Override
            public void handle(AsyncResult<JsonMessage> result) {
              assertTrue(result.succeeded());
              JsonObject body = result.result().body();
              JsonObject output = executor.config().getObject("output");
              for (String fieldName : output.getFieldNames()) {
                assertEquals(output.getValue(fieldName), body.getValue(fieldName));
              }
              testComplete();
            }
          });
        }
      }
    });
  }

}
