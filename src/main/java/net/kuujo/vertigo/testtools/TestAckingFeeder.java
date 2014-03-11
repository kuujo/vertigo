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

import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.java.BasicFeeder;
import net.kuujo.vertigo.message.MessageId;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A test feeder that tests that a message was successfully acked.
 *
 * @author Jordan Halterman
 */
public class TestAckingFeeder extends BasicFeeder {

  @Override
  public void start(Feeder feeder) {
    feeder.emit(container.config(), new Handler<AsyncResult<MessageId>>() {
      @Override
      public void handle(AsyncResult<MessageId> result) {
        assertTrue(result.succeeded());
        assertNotNull(result.result());
        testComplete();
      }
    });
  }

}
