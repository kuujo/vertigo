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
package rpc;

import org.vertx.java.platform.Verticle;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.SeedVerticle;
import com.blankstyle.vine.Groupings;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.local.LocalRoot;
import com.blankstyle.vine.Feeder;

/**
 * A word count vine demonstrating groupings.
 *
 * This example follows the general structure of the Storm Starter example
 * at https://github.com/nathanmarz/storm-starter/blob/master/src/jvm/storm/starter/WordCountTopology.java
 *
 * @author Jordan Halterman
 */
public class WordCountVine extends Verticle {

  /**
   * Splits a sentence into words.
   */
  public static class SplitSentenceSeed extends SeedVerticle {
    @Override
    public void process(JsonObject data) {
      String[] words = data.getString("sentence", "").split("\\s+");
      for (String word : words) {
        emit(new JsonObject().putString("word", word));
      }
    }
  }

  /**
   * Maintains a count of words.
   */
  public static class WordCountSeed extends SeedVerticle {
    Map<String, Integer> counts = new HashMap<String, Integer>();

    @Override
    public void process(JsonObject data) {
      String word = data.getString("word");
      Integer count = counts.get(word);
      if (count == null) {
        count = 0;
      }
      count++;
      counts.put(word, count);
      emit(new JsonObject().putString("word", word).putInteger("count", count));
    }
  }

  private java.util.logging.Logger logger;

  @Override
  public void start() {
    logger = container.logger();

    // Create a vine definition that feeds data to the sentence splitter, which
    // feeds data to the word counter. Note that since the word counter uses
    // multiple workers, it *must* use a grouping that ensures that specific
    // workers will always receive the same word.

    // "wordcount" is the eventbus address to the vine.
    VineDefinition vine = new VineDefinition("wordcount");
    vine.feed("split", SplitSentenceSeed.class.getName(), 8).groupBy(Groupings.random())
      .to("count", WordCountSeed.class.getName(), 12).groupBy(Groupings.fields("word"));

    // Create a local root instance and deploy the vine.
    final Root root = new LocalRoot(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<Feeder>>() {
      @Override
      public void handle(AsyncResult<Feeder> result) {
        if (result.failed()) {
          logger.error("Failed to deploy vine.", result.cause());
        }
        else {
          // If the vine was successfully deployed, feed some sentences to it.
          Feeder feeder = result.result();
          feeder.feed(new JsonObject().putString("sentence", "The frog jumped over the log."));
          feeder.feed(new JsonObject().putString("sentence", "The cat jumped over the hat."));
          feeder.feed(new JsonObject().putString("sentence", "The fox jumped over the rocks."));

          // Set a timer that shuts down the vine after two seconds.
          vertx.setTimer(2000, new Handler<Long>() {
            public void handle(Long timerID) {
              root.shutdown("wordcount");
            }
          });
        }
      }
    });
  }

}
