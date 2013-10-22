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
package net.kuujo.vertigo.test.integration.messaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.kuujo.vertigo.dispatcher.AllDispatcher;
import net.kuujo.vertigo.dispatcher.Dispatcher;
import net.kuujo.vertigo.dispatcher.FieldsDispatcher;
import net.kuujo.vertigo.dispatcher.RoundRobinDispatcher;
import net.kuujo.vertigo.messaging.Connection;
import net.kuujo.vertigo.messaging.ConnectionPool;
import net.kuujo.vertigo.messaging.ConnectionSet;
import net.kuujo.vertigo.messaging.DefaultJsonMessage;
import net.kuujo.vertigo.messaging.EventBusConnection;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.junit.Test;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A dispatcher test.
 *
 * @author Jordan Halterman
 */
public class DispatcherTest extends TestVerticle {

  @Test
  public void testAllDispatcher() {
    Dispatcher dispatcher = new AllDispatcher();
    ConnectionPool connections = new ConnectionSet();
    EventBus eventBus = vertx.eventBus();

    final Set<String> complete = new HashSet<String>();

    Connection connectionOne = new DefaultConnection("test.1", eventBus);
    eventBus.registerHandler("test.1", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonMessage jsonMessage = new DefaultJsonMessage(message.body());
        assertEquals("Hello world!", jsonMessage.body().getString("body"));
        complete.add("test.1");
        if (complete.size() == 3) {
          assertTrue(complete.contains("test.1"));
          assertTrue(complete.contains("test.1"));
          assertTrue(complete.contains("test.1"));
          testComplete();
        }
      }
    });
    connections.add(connectionOne);

    Connection connectionTwo = new DefaultConnection("test.2", eventBus);
    eventBus.registerHandler("test.2", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonMessage jsonMessage = new DefaultJsonMessage(message.body());
        assertEquals("Hello world!", jsonMessage.body().getString("body"));
        complete.add("test.2");
        if (complete.size() == 3) {
          assertTrue(complete.contains("test.1"));
          assertTrue(complete.contains("test.1"));
          assertTrue(complete.contains("test.1"));
          testComplete();
        }
      }
    });
    connections.add(connectionTwo);

    Connection connectionThree = new DefaultConnection("test.3", eventBus);
    eventBus.registerHandler("test.3", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonMessage jsonMessage = new DefaultJsonMessage(message.body());
        assertEquals("Hello world!", jsonMessage.body().getString("body"));
        complete.add("test.3");
        if (complete.size() == 3) {
          assertTrue(complete.contains("test.1"));
          assertTrue(complete.contains("test.1"));
          assertTrue(complete.contains("test.1"));
          testComplete();
        }
      }
    });
    connections.add(connectionThree);

    dispatcher.init(connections);

    dispatcher.dispatch(DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "auditor"));
  }

  @Test
  public void testRoundDispatcher() {
    Dispatcher dispatcher = new RoundRobinDispatcher();
    ConnectionPool connections = new ConnectionSet();
    EventBus eventBus = vertx.eventBus();

    final List<String> order = new ArrayList<String>();
    final List<String> complete = new ArrayList<String>();

    Connection connectionOne = new DefaultConnection("test.1", eventBus);
    eventBus.registerHandler("test.1", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (!order.contains("test.1")) {
          order.add("test.1");
        }
        else if (order.size() == 3) {
          int index = order.indexOf("test.1");
          assertEquals(complete.size(), index);
          complete.add("test.1");
          if (complete.size() == 3) {
            testComplete();
          }
        }
      }
    });
    connections.add(connectionOne);

    Connection connectionTwo = new DefaultConnection("test.2", eventBus);
    eventBus.registerHandler("test.2", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (!order.contains("test.2")) {
          order.add("test.2");
        }
        else if (order.size() == 3) {
          int index = order.indexOf("test.2");
          assertEquals(complete.size(), index);
          complete.add("test.2");
          if (complete.size() == 3) {
            testComplete();
          }
        }
      }
    });
    connections.add(connectionTwo);

    Connection connectionThree = new DefaultConnection("test.3", eventBus);
    eventBus.registerHandler("test.3", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (!order.contains("test.3")) {
          order.add("test.3");
        }
        else if (order.size() == 3) {
          int index = order.indexOf("test.3");
          assertEquals(complete.size(), index);
          complete.add("test.3");
          if (complete.size() == 3) {
            testComplete();
          }
        }
      }
    });
    connections.add(connectionThree);

    dispatcher.init(connections);

    JsonObject message = new JsonObject().putString("body", "Hello world!");
    dispatcher.dispatch(DefaultJsonMessage.create(message, "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(message, "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(message, "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(message, "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(message, "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(message, "auditor"));
  }

  @Test
  public void testFieldsDispatcher() {
    Dispatcher dispatcher = new FieldsDispatcher("test");
    ConnectionPool connections = new ConnectionSet();
    EventBus eventBus = vertx.eventBus();

    final List<String> order = new ArrayList<String>();
    final List<String> complete = new ArrayList<String>();

    Connection connectionOne = new DefaultConnection("test.1", eventBus);
    eventBus.registerHandler("test.1", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (!order.contains("test.1")) {
          order.add("test.1");
        }
        else if (order.size() == 3) {
          int index = order.indexOf("test.1");
          assertEquals(complete.size(), index);
          complete.add("test.1");
          if (complete.size() == 3) {
            testComplete();
          }
        }
      }
    });
    connections.add(connectionOne);

    Connection connectionTwo = new DefaultConnection("test.2", eventBus);
    eventBus.registerHandler("test.2", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (!order.contains("test.2")) {
          order.add("test.2");
        }
        else if (order.size() == 3) {
          int index = order.indexOf("test.2");
          assertEquals(complete.size(), index);
          complete.add("test.2");
          if (complete.size() == 3) {
            testComplete();
          }
        }
      }
    });
    connections.add(connectionTwo);

    Connection connectionThree = new DefaultConnection("test.3", eventBus);
    eventBus.registerHandler("test.3", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        if (!order.contains("test.3")) {
          order.add("test.3");
        }
        else if (order.size() == 3) {
          int index = order.indexOf("test.3");
          assertEquals(complete.size(), index);
          complete.add("test.3");
          if (complete.size() == 3) {
            testComplete();
          }
        }
      }
    });
    connections.add(connectionThree);

    dispatcher.init(connections);

    dispatcher.dispatch(DefaultJsonMessage.create(new JsonObject().putString("test", "a"), "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(new JsonObject().putString("test", "ab"), "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(new JsonObject().putString("test", "abc"), "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(new JsonObject().putString("test", "b"), "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(new JsonObject().putString("test", "bc"), "auditor"));
    dispatcher.dispatch(DefaultJsonMessage.create(new JsonObject().putString("test", "bcd"), "auditor"));
  }

}
