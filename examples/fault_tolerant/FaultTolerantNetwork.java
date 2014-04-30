/*
 * Copyright 2014 the original author or authors.
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
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.data.AsyncIdGenerator;
import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Fault-tolerant feeder example.<p>
 *
 * This example demonstrates fault-tolerant streams using Vertigo cluster-wide
 * shared data. When a message is received on an event bus handler, the message
 * is tagged with a unique ID from a cluster-wide ID generator and stored in
 * a cluster-wide map. The feeder then listens on an input <code>ack</code>
 * port for message acks.
 *
 * @author Jordan Halterman
 */
public class FaultTolerantNetwork extends Verticle {

  /**
   * Persists and sends messages received on the event bus.
   */
  public static class FaultTolerantFeeder extends ComponentVerticle {
    private AsyncCounter ids;
    private AsyncMap<Long, String> messages;

    /**
     * When a message is received, assign a unique ID to the message
     * and store the message in the cluster.
     */
    private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
      @Override
      public void handle(final Message<JsonObject> message) {
        ids.incrementAndGet(new Handler<AsyncResult<Long>>() {
          @Override
          public void handle(AsyncResult<Long> result) {
            if (result.succeeded()) {
              final long id = result.result();
              final JsonObject body = message.body();
              body.putNumber("id", id);
              messages.put(id, body.encode(), new Handler<AsyncResult<String>>() {
                @Override
                public void handle(AsyncResult<String> result) {
                  if (result.succeeded()) {
                    output.port("out").send(body);
                  }
                }
              });
            }
          }
        });
      }
    };

    @Override
    public void start(final Future<Void> startResult) {
      vertx.eventBus().registerHandler("test", messageHandler, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            startResult.setFailure(result.cause());
          } else {
            FaultTolerantFeeder.super.start(startResult);
          }
        }
      });
    }

    @Override
    public void start() {
      ids = cluster.getIdGenerator("ids");
      messages = cluster.getMap("messages");

      // Listen on the 'ack' port for ack messages. When an ack message
      // is received, remove the message from the messages map.
      input.port("ack").messageHandler(new Handler<Long>() {
        @Override
        public void handle(Long messageId) {
          messages.remove(messageId);
        }
      });
    }

  }

  /**
   * Receives and acks messages.
   */
  public static class MessageReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<JsonObject>() {
        @Override
        public void handle(JsonObject message) {
          // Ack the message by sending the message ID on the ack port.
          output.port("ack").send(message.getLong("id"));
        }
      });
    }
  }

  @Override
  public void start() {
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("fault-tolerant");
    network.addVerticle("sender", FaultTolerantFeeder.class.getName());
    network.addVerticle("receiver", MessageReceiver.class.getName());
    network.createConnection("sender", "out", "receiver", "in");
    network.createConnection("receiver", "ack", "sender", "ack");

    vertigo.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
      @Override
      public void handle(AsyncResult<ActiveNetwork> result) {
        if (result.failed()) {
          container.logger().error(result.cause());
        } else {
          container.logger().info("Started successfully.");
        }
      }
    });
  }

}
