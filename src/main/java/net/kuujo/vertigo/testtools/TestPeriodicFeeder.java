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

import java.util.UUID;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.java.BasicFeeder;

/**
 * A feeder that periodically feeds a network with randomly generated field values.
 *
 * @author Jordan Halterman
 */
public class TestPeriodicFeeder extends BasicFeeder {
  private static final long DEFAULT_INTERVAL = 100;

  @Override
  public void start(final Feeder feeder) {
    final JsonArray fields = container.config().getArray("fields");
    final long interval = container.config().getLong("interval", DEFAULT_INTERVAL);
    vertx.setPeriodic(interval, new Handler<Long>() {
      @Override
      public void handle(Long timerId) {
        JsonObject data = new JsonObject();
        for (Object field : fields) {
          data.putString((String) field, UUID.randomUUID().toString());
        }
        feeder.emit(data);
      }
    });
  }

}
