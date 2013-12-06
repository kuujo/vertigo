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
package net.kuujo.vertigo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.annotations.Input;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.java.RichFeederVerticle;
import net.kuujo.vertigo.java.RichWorkerVerticle;
import net.kuujo.vertigo.java.VertigoVerticle;
import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A word count network example.
 *
 * This example demonstrates the use of groupings - in particular the FieldsGrouping -
 * to control the dispersion of messages between multiple verticle instances.
 *
 * @author Jordan Halterman
 */
public class WordCountNetwork extends VertigoVerticle {

  /**
   * A random word feeder.
   */
  public static class WordFeeder extends RichFeederVerticle {
    private String[] words = new String[]{
      "foo", "bar", "baz", "foobar", "foobaz", "barfoo", "barbaz", "bazfoo", "bazbar"
    };
    private Random random = new Random();

    @Override
    protected void nextMessage() {
      String word = words[random.nextInt(words.length-1)];
      emit(new JsonObject().putString("word", word));
    }
  }

  /**
   * A word counting worker.
   */
  @Input(schema={@Input.Field(name="word", type=String.class)})
  public static class WordCountWorker extends RichWorkerVerticle {
    private Map<String, Integer> counts = new HashMap<>();

    @Override
    protected void handleMessage(JsonMessage message) {
      String word = message.body().getString("word");
      Integer count = counts.get(word);
      if (count == null) count = 0;
      count++;
      emit(new JsonObject().putString("word", word).putNumber("count", count), message);
      ack(message);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void start() {
    Network network = vertigo.createNetwork("word_count");
    network.addFeeder("word_feeder", WordFeeder.class.getName());
    network.addWorker("word_counter", WordCountWorker.class.getName(), 4).addInput("word_feeder").groupBy(new FieldsGrouping("word"));

    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<NetworkContext>>() {
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
              vertigo.shutdownLocalNetwork(context);
            }
          });
        }
      }
    });
  }

}
