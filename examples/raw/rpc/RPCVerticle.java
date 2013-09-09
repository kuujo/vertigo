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
import org.vertx.java.core.Logger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.SeedVerticle;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.local.LocalRoot;
import com.blankstyle.vine.Vine;
import com.blankstyle.vine.Feeder;

/**
 * A simple remote-procedure call via vine.
 *
 * This example follows the general structure of the Storm Starter example
 * at https://github.com/nathanmarz/storm-starter/blob/master/src/jvm/storm/starter/ManualDRPC.java
 *
 * @author Jordan Halterman
 */
class RPCVerticle extends Verticle {

  /**
   * Add exclaimation marks to text.
   */
  public static class ExclaimSeed extends SeedVerticle {
    @Override
    public void process(JsonObject data) {
      emit(new JsonObject().putString(data.getString("body") + "!!!"));
    }
  }

  private java.util.logging.Logger logger;

  // The deploy handler is called once the vine is deployed.
  private Handler<AsyncResult<Vine>> deployHandler = new Handler<AsyncResult<Vine>>() {
    @Override
    public void handle(AsyncResult<Vine> result) {
      if (result.failed()) {
        logger.error("Failed to deploy vine.", result.cause());
      }
      else {
        // If the vine was successfully deployed, create a feeder to the vine
        // and feed three messages to the exclaim seed.
        Feeder feeder = vine.feeder();
        feeder.feed(new JsonObject().putString("body", "aaa"), resultHandler);
        feeder.feed(new JsonObject().putString("body", "bbb"), resultHandler);
        feeder.feed(new JsonObject().putString("body", "ccc"), resultHandler);
      }
    }
  };

  // The result handler is called once a result has been received from the vine.
  private Handler<AsyncResult<JsonObject>> resultHandler = new Handler<AsyncResult<JsonObject>>() {
    @Override
    public void handle(AsyncResult<JsonObject> result) {
      if (result.failed()) {
        logger.error("Failed to execute vine.", result.cause());
      }
      else {
        logger.info(result.result().getString("body"));
      }
    }
  };

  @Override
  public void start() {
    logger = container.logger();

    // Create a vine definition with a feeder to the ExclaimSeed.
    VineDefinition vine = new VineDefinition("rpc");
    vine.feed("exclaim", ExclaimSeed.class.getName());

    // Create a local root instance and deploy the vine.
    Root root = new LocalRoot(vertx, container);
    root.deploy(vine, deployHandler);
  }

}
