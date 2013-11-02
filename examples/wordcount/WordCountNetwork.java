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
package wordcount;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.VertigoVerticle;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.feeder.BasicFeeder;
import net.kuujo.vertigo.worker.Worker;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A word count network example.
 *
 * This example demonstrates the use of groupings - in particular the FieldsGrouping -
 * to control the dispersion of messages between multiple verticle instances.
 *
 * @author Jordan Halterman
 */
public class WordCountNetwork extends Verticle {

  public static class WordCountWorker extends VertigoVerticle {

    private Worker worker;

    private Map<String, Integer> counts = new HashMap<String, Integer>();

    @Override
    public void start() {
      worker = vertigo.createWorker();
      worker.messageHandler(new Handler<JsonMessage>() {
        @Override
        public void handle(JsonMessage message) {
          String word = message.body().getString("word");
          Integer count = counts.get(word);
          if (count == null) {
            count = 0;
          }
          count++;
          worker.emit(new JsonObject().putString("word", word).putNumber("count", count));
          worker.ack(message);
        }
      }).start();
    }
  }

  public static class WordFeeder extends VertigoVerticle {

    @Override
    public void start() {
      vertigo.createBasicFeeder().start(new Handler<AsyncResult<BasicFeeder>>() {
        @Override
        public void handle(AsyncResult<BasicFeeder> result) {
          if (result.failed()) {
            container.logger().error(result.cause());
          }
          else {
            BasicFeeder feeder = result.result();
            feeder.feed(new JsonObject().putString("word", "foo"));
            feeder.feed(new JsonObject().putString("word", "bar"));
            feeder.feed(new JsonObject().putString("word", "foobar"));
            feeder.feed(new JsonObject().putString("word", "barbaz"));
            feeder.feed(new JsonObject().putString("word", "bar"));
            feeder.feed(new JsonObject().putString("word", "foobar"));
            feeder.feed(new JsonObject().putString("word", "barbaz"));
            feeder.feed(new JsonObject().putString("word", "bar"));
            feeder.feed(new JsonObject().putString("word", "foobar"));
            feeder.feed(new JsonObject().putString("word", "foobar"));
            feeder.feed(new JsonObject().putString("word", "foo"));
            feeder.feed(new JsonObject().putString("word", "bar"));
            feeder.feed(new JsonObject().putString("word", "bar"));
            feeder.feed(new JsonObject().putString("word", "bar"));
            feeder.feed(new JsonObject().putString("word", "bar"));
          }
        }
      });
    }

  }

  @Override
  public void start() {
    Network network = new Network("word_count");
    Component feeder = network.addVerticle("word_feeder", WordFeeder.class.getName());
    Component counter = network.addVerticle("word_counter", WordCountWorker.class.getName(), 4);
    counter.addInput("word_feeder").groupBy(new FieldsGrouping("word"));

    final Cluster cluster = new LocalCluster(vertx, container);
    cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          container.logger().error(result.cause());
        }
        else {
          final NetworkContext context = result.result();
          vertx.setTimer(5000, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
              cluster.shutdown(context);
            }
          });
        }
      }
    });
  }

}
