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
 * This example demonstrates the use of selectors - in particular the HashSelector -
 * to control the dispersion of messages between multiple verticle instances.
 *
 * @author Jordan Halterman
 */
public class WordCountNetwork extends VertigoVerticle {

  /**
   * Random word feeder.
   */
  public static class WordFeeder extends ComponentVerticle {
    private String[] words = new String[]{
      "foo", "bar", "baz", "foobar", "foobaz", "barfoo", "barbaz", "bazfoo", "bazbar"
    };
    private Random random = new Random();

    @Override
    public void start() {
      doSend();
    }

    private void doSend() {
      while (!output.port("out").sendQueueFull()) {
        output.port("out").send(words[random.nextInt(words.length-1)]);
      }
      output.port("out").drainHandler(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          doSend();
        }
      });
    }
  }

  /**
   * Word counter.
   */
  public static class WordCounter extends ComponentVerticle {
    private final Map<String, Integer> counts = new HashMap<>();

    @Override
    public void start() {
      input.port("in").messageHandler(new Handler<String>() {
        @Override
        public void handle(String word) {
          Integer count = counts.get(word);
          if (count == null) count = 0;
          count++;
          output.port("out").send(new JsonObject().putString("word", word).putNumber("count", count));
        }
      });
    }

  }

  @Override
  @SuppressWarnings("unchecked")
  public void start() {
    Network network = vertigo.createNetwork("word-count");
    network.addVerticle("word-feeder", WordFeeder.class.getName());
    network.addVerticle("word-counter", WordCounter.class.getName(), 4);
    network.createConnection("word-feeder", "out", "word-counter", "in").hashSelect();

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
