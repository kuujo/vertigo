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

import net.kuujo.vitis.dispatcher.AllDispatcher;
import net.kuujo.vitis.dispatcher.Dispatcher;
import net.kuujo.vitis.messaging.BasicChannel;
import net.kuujo.vitis.messaging.Channel;
import net.kuujo.vitis.messaging.DefaultJsonMessage;
import net.kuujo.vitis.messaging.EventBusConnection;
import net.kuujo.vitis.messaging.JsonMessage;

import org.junit.Test;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

/**
 * A channel test.
 *
 * @author Jordan Halterman
 */
public class ChannelTest extends TestVerticle {

  private boolean oneReceived;

  private boolean twoReceived;

  private boolean threeReceived;

  private Channel createChannel(Dispatcher dispatcher) {
    Channel channel = new BasicChannel(dispatcher);
    channel.addConnection(new EventBusConnection("test.1", vertx.eventBus()));
    channel.addConnection(new EventBusConnection("test.2", vertx.eventBus()));
    channel.addConnection(new EventBusConnection("test.3", vertx.eventBus()));
    return channel;
  }

  @Test
  public void testAll() {
    Channel channel = createChannel(new AllDispatcher());
    EventBus eventBus = vertx.eventBus();
    eventBus.registerHandler("test.1", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        assertNotNull(message.body());
        JsonMessage json = new DefaultJsonMessage(message.body());
        assertEquals("Hello world!", json.body().getString("body"));
        assertEquals("foo", json.tag());
        oneReceived = true;
        if (oneReceived && twoReceived && threeReceived) {
          testComplete();
        }
      }
    });
    eventBus.registerHandler("test.2", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        assertNotNull(message.body());
        JsonMessage json = new DefaultJsonMessage(message.body());
        assertEquals("Hello world!", json.body().getString("body"));
        assertEquals("foo", json.tag());
        twoReceived = true;
        if (oneReceived && twoReceived && threeReceived) {
          testComplete();
        }
      }
    });
    eventBus.registerHandler("test.3", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        assertNotNull(message.body());
        JsonMessage json = new DefaultJsonMessage(message.body());
        assertEquals("Hello world!", json.body().getString("body"));
        assertEquals("foo", json.tag());
        threeReceived = true;
        if (oneReceived && twoReceived && threeReceived) {
          testComplete();
        }
      }
    });
    channel.write(DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "foo"));
  }

}
