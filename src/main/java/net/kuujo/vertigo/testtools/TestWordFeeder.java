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

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.java.BasicFeeder;

/**
 * A feeder that feeder "random" test words to a network.
 *
 * @author Jordan Halterman
 */
public class TestWordFeeder extends BasicFeeder {
  private String field;
  private String[] words;
  private Random random = new Random();

  @Override
  public void start(final Feeder feeder) {
    field = container.config().getString("field");
    words = (String[]) container.config().getArray("words").toArray();
    feeder.feedHandler(new Handler<Feeder>() {
      @Override
      public void handle(Feeder feeder) {
        String word = words[random.nextInt(words.length)];
        JsonObject data = new JsonObject().putString(field, word);
        feeder.emit(data);
      }
    });
  }

}
