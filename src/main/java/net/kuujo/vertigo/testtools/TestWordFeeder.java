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
package net.kuujo.vertigo.testtools;

import java.util.Random;
import java.util.UUID;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.VertigoVerticle;
import net.kuujo.vertigo.component.feeder.PollingFeeder;
import net.kuujo.vertigo.network.Verticle;

/**
 * A feeder that feeder "random" test words to a network.
 *
 * @author Jordan Halterman
 */
public class TestWordFeeder extends VertigoVerticle {

  /**
   * Creates a test word feeder definition.
   *
   * @param field
   *   The field in which to place words.
   * @return
   *   A component definition.
   */
  public static Verticle createDefinition(String field) {
    String[] words = new String[]{"apples", "bananas", "oranges", "peaches", "pears", "strawberries"};
    return createDefinition(field, words);
  }

  /**
   * Creates a test word feeder definition.
   *
   * @param field
   *   The field in which to place words.
   * @param words
   *   An array of words to feed.
   * @return
   *   A component definition.
   */
  public static Verticle createDefinition(String field, String[] words) {
    return new Verticle(UUID.randomUUID().toString())
      .setMain(TestWordFeeder.class.getName())
      .setConfig(new JsonObject().putString("field", field)
      .putArray("words", new JsonArray(words)));
  }

  private String field;
  private String[] words;
  private Random random = new Random();

  @Override
  public void start() {
    field = container.config().getString("field");
    words = (String[]) container.config().getArray("words").toArray();

    vertigo.createPollingFeeder().start(new Handler<AsyncResult<PollingFeeder>>() {
      @Override
      public void handle(AsyncResult<PollingFeeder> result) {
        if (result.failed()) {
          container.logger().error("Failed to start feeder.");
        }
        else {
          result.result().feedHandler(new Handler<PollingFeeder>() {
            @Override
            public void handle(PollingFeeder feeder) {
              String word = words[random.nextInt(words.length)];
              JsonObject data = new JsonObject().putString(field, word);
              feeder.feed(data);
            }
          });
        }
      }
    });
  }

}
