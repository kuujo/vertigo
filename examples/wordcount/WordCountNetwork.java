/*
 * Copyright 2013-2014 the original author or authors.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.ActiveNetwork;

import org.vertx.java.platform.Verticle;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;

/**
 * A word count network example.
 *
 * This example demonstrates the use of selectors - in particular the HashSelector -
 * to control the dispersion of messages between multiple verticle instances.
 *
 * @author Jordan Halterman
 */
public class WordCountNetwork extends Verticle {

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

    /**
     * Sends a random word to the "word" output port whenever
     * the output queue is not full.
     */
    private void doSend() {
      while (!output.port("word").sendQueueFull()) {
        output.port("word").send(words[random.nextInt(words.length-1)]);
      }
      output.port("word").drainHandler(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          doSend();
        }
      });
    }
  }

  /**
   * Receives words on the "word" input port and maintains a
   * historical count of words received. Each time a word
   * is received its updated count is sent on the "count"
   * output port.
   */
  public static class WordCounter extends ComponentVerticle {
    private final Map<String, Integer> counts = new HashMap<>();

    @Override
    public void start() {
      input.port("word").messageHandler(new Handler<String>() {
        @Override
        public void handle(String word) {
          Integer count = counts.get(word);
          if (count == null) count = 0;
          count++;
          output.port("count").send(new JsonObject().putString("word", word).putNumber("count", count));
        }
      });
    }

  }

  @Override
  @SuppressWarnings("unchecked")
  public void start(final Future<Void> startResult) {
    final Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("default", new Handler<AsyncResult<ClusterManager>>() {
      @Override
      public void handle(AsyncResult<ClusterManager> result) {
        if (result.failed()) {
          startResult.setFailure(result.cause());
        } else {
          ClusterManager cluster = result.result();

          NetworkConfig network = vertigo.createNetwork("word-count");
          network.addVerticle("word-feeder", WordFeeder.class.getName());
          network.addVerticle("word-counter", WordCounter.class.getName(), 4);
          network.createConnection("word-feeder", "word", "word-counter", "word").hashSelect();

          cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
            @Override
            public void handle(AsyncResult<ActiveNetwork> result) {
              if (result.failed()) {
                startResult.setFailure(result.cause());
              } else {
                startResult.setResult((Void) null);
              }
            }
          });
        }
      }
    });
  }

}
