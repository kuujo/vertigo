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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.java.ExecutorVerticle;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.rpc.Executor;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A test executor that checks that an execution was successful.
 *
 * @author Jordan Halterman
 */
public class TestAckingExecutor extends ExecutorVerticle {

  @Override
  public void start(Executor executor) {
    executor.execute(container.config().getObject("input", new JsonObject()), new Handler<AsyncResult<JsonMessage>>() {
      @Override
      public void handle(AsyncResult<JsonMessage> result) {
        assertTrue(result.succeeded());
        testComplete();
      }
    });
  }

}
