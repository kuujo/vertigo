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

import net.kuujo.vevent.definition.NetworkDefinition;
import net.kuujo.vevent.context.NetworkContext;
import net.kuujo.vevent.messaging.JsonMessage;
import net.kuujo.vevent.WorkerVerticle;
import net.kuujo.vevent.Groupings;
import net.kuujo.vevent.Root;
import net.kuujo.vevent.LocalCluster;
import net.kuujo.vevent.Feeder;

/**
 * A basic network example.
 *
 * This example follows the general structure of the Storm Starter example
 * at https://github.com/nathanmarz/storm-starter/blob/master/src/jvm/storm/starter/ExclamationTopology.java
 *
 * @author Jordan Halterman
 */
public class ExclamationNetwork extends Verticle {

  /**
   * Add exclamation marks to text.
   */
  public static class Exclamation extends WorkerVerticle {
    @Override
    public void process(JsonMessage message) {
      emit(new JsonObject().putString("body", data.getString("body") + "!!!"), message);
    }
  }

  private Logger logger;

  @Override
  public void start() {
    logger = container.logger();

    // Create a network definition that feeds data to the Exclamation, which
    // feeds data to itself again.
    NetworkDefinition network = new NetworkDefinition("exclamation");
    network.from("exclaim1", Exclamation.class.getName(), 3).groupBy(Groupings.random())
      .to("exclaim2", Exclamation.class.getName(), 2).groupBy(Groupings.random());

    // Create a local cluster instance and deploy the network.
    final Cluster cluster = new LocalCluster(vertx, container);
    cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          logger.error("Failed to deploy network.", result.cause());
        }
        else {
          // If the network was successfully deployed, shut down the network.
          cluster.shutdown("exclamation", new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              // Ensure that the network was properly shut down.
              if (result.failed()) {
                logger.error("Failed to shutdown network.", result.cause());
              }
            }
          });
        }
      }
    });
  }

}
