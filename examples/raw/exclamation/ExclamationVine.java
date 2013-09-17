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
package exclamation;

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
 * A basic vine example.
 *
 * This example follows the general structure of the Storm Starter example
 * at https://github.com/nathanmarz/storm-starter/blob/master/src/jvm/storm/starter/ExclamationTopology.java
 *
 * @author Jordan Halterman
 */
public class ExclamationVine extends Verticle {

  /**
   * Add exclamation marks to text.
   */
  public static class ExclamationSeed extends SeedVerticle {
    @Override
    public void process(JsonObject data) {
      emit(new JsonObject().putString("body", data.getString("body") + "!!!"));
    }
  }

  private Logger logger;

  @Override
  public void start() {
    logger = container.logger();

    // Create a vine definition that feeds data to the ExclamationSeed, which
    // feeds data to itself again.
    VineDefinition vine = new VineDefinition("exclamation");
    vine.feed("exclaim1", ExclamationSeed.class.getName(), 3).groupBy(Groupings.random())
      .to("exclaim2", ExclamationSeed.class.getName(), 2).groupBy(Groupings.random());

    // Create a local root instance and deploy the vine.
    final Root root = new LocalRoot(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<Vine>>() {
      @Override
      public void handle(AsyncResult<Feeder> result) {
        if (result.failed()) {
          logger.error("Failed to deploy vine.", result.cause());
        }
        else {
          // If the vine was successfully deployed, shut down the vine.
          root.shutdown("exclamation", new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              // Ensure that the vine was properly shut down.
              if (result.failed()) {
                logger.error("Failed to shutdown vine.", result.cause());
              }
            }
          });
        }
      }
    });
  }

}
