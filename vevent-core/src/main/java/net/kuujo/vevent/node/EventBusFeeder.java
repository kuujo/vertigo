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
package net.kuujo.vevent.node;

import java.util.ArrayDeque;
import java.util.Deque;

import net.kuujo.vevent.context.WorkerContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * An eventbus feeder.
 *
 * @author Jordan Halterman
 */
public class EventBusFeeder extends BasicFeeder {

  private String address;

  private Deque<Message<JsonObject>> feedQueue = new ArrayDeque<Message<JsonObject>>();

  protected Handler<Feeder> feedHandler = new Handler<Feeder>() {
    @Override
    public void handle(Feeder feeder) {
      final Message<JsonObject> message = dequeue();
      if (message != null) {
        feeder.feed(message.body(), new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            message.reply(result.succeeded());
          }
        });
      }
      else {
        scheduleFeed();
      }
    }
  };

  public EventBusFeeder(String address, Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
    this.address = address;
  }

  @Override
  public void start() {
    eventBus.registerHandler(address, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        enqueue(message);
      }
    });
    super.start();
  }

  private void enqueue(Message<JsonObject> message) {
    feedQueue.addLast(message);
  }

  private Message<JsonObject> dequeue() {
    return feedQueue.pollFirst();
  }

  @Override
  public Feeder feedHandler(Handler<Feeder> handler) {
    return this;
  }

}
