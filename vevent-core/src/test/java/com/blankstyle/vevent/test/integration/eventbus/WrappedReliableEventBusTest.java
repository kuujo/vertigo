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
package com.blankstyle.vevent.test.integration.eventbus;

import net.kuujo.vevent.eventbus.ReliableEventBus;
import net.kuujo.vevent.eventbus.WrappedReliableEventBus;

import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A wrapped reliable eventbus test.
 *
 * @author Jordan Halterman
 */
public class WrappedReliableEventBusTest extends TestVerticle implements Handler<Message<JsonObject>> {

  private boolean received;

  @Before
  public void setUp() {
    received = false;
  }

  @Test
  public void testTimeout() {
    ReliableEventBus eventBus = new WrappedReliableEventBus(vertx.eventBus(), vertx);
    eventBus.registerHandler("test.a", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        
      }
    });

    // Send a message that will not recieve a response.
    eventBus.send("test.a", new JsonObject().putString("body", "Hello world!"), (long) 1, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        assertTrue(result.failed());
        testComplete();
      }
    });
  }

  @Test
  public void testResend() {
    ReliableEventBus eventBus = new WrappedReliableEventBus(vertx.eventBus(), vertx);
    eventBus.registerHandler("test.b", this);

    // Send a message that will not recieve a response.
    eventBus.send("test.b", new JsonObject().putString("body", "Hello world!"), (long) 1, true, new AsyncResultHandler<Message<JsonObject>>() {
      @Override
      public void handle(AsyncResult<Message<JsonObject>> result) {
        assertTrue(result.failed());
        testComplete();
      }
    });
  }

  public void handle(Message<JsonObject> message) {
    // If this is the first time receiving the message then don't
    // reply. This is used for testing resends. On the second try
    // we can resend the message.
    if (!received) {
      received = true;
    }
    else {
      message.reply(new JsonObject());
    }
  }

}
