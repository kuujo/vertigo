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
package net.kuujo.vitis.test.integration.messaging;

import net.kuujo.vitis.messaging.Connection;
import net.kuujo.vitis.messaging.DefaultJsonMessage;
import net.kuujo.vitis.messaging.EventBusConnection;
import net.kuujo.vitis.messaging.JsonMessage;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A connection test.
 *
 * @author Jordan Halterman
 */
public class ConnectionTest extends TestVerticle {

  private static final String TEST_ADDRESS = "test";

  @Test
  public void testWrite() {
    vertx.eventBus().registerHandler(TEST_ADDRESS, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        assertNotNull(message.body());
        JsonMessage json = new DefaultJsonMessage(message.body());
        assertEquals("Hello world!", json.body().getString("body"));
        assertEquals("foo", json.tag());
        testComplete();
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        Connection connection = new EventBusConnection(TEST_ADDRESS, vertx.eventBus());
        connection.write(DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "foo"));
      }
    });
  }

}
