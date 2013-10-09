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
package complex;

import org.vertx.java.platform.Verticle;
import org.vertx.java.core.logging.Logger;

import net.kuujo.vertigo.java.VertigoVerticle;
import net.kuujo.vertigo.Cluster;
import net.kuujo.vertigo.LocalCluster;
import net.kuujo.vertigo.definition.NetworkDefinition;
import net.kuujo.vertigo.definition.ComponentDefinition;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.grouping.RandomGrouping;
import net.kuujo.vertigo.grouping.RoundGrouping;
import net.kuujo.vertigo.grouping.FieldsGrouping;
import net.kuujo.vertigo.grouping.AllGrouping;
import net.kuujo.vertigo.filter.TagsFilter;
import net.kuujo.vertigo.component.feeder.BasicFeeder;
import net.kuujo.vertigo.component.worker.Worker;
import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * A complex network implementation.
 *
 * @author Jordan Halterman
 */
class ComplexNetworkVerticle extends Verticle {

  /**
   * An example feeder verticle.
   */
  public static class ExampleFeeder extends VertigoVerticle {

    private Logger logger;

    @Override
    public void start() {
      logger = container.logger();
      vertigo.createBasicFeeder().start(new Handler<AsyncResult<BasicFeeder>>() {
        @Override
        public void handle(AsyncResult<BasicFeeder> result) {
          if (result.failed()) {
            logger.warn("Failed to start feeder.", result.cause());
          }
          else {
            BasicFeeder feeder = result.result();
            feeder.feed(new JsonObject().putNumber("count", 0), new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                if (result.failed()) {
                  logger.warn("Failed to process data.", result.cause());
                }
                else {
                  logger.info("Successfully processed data!");
                }
              }
            });
          }
        }
      });
    }
  }

  /**
   * An example worker verticle.
   *
   * This verticle can be used for all workers in the network, dispite the fact
   * that different network components use different groupings and filters. This
   * is demonstrative of the separation between component definitions and implementations.
   */
  public static class ExampleWorker extends VertigoVerticle {

    private Logger logger;

    private Worker worker;

    @Override
    public void start() {
      worker = vertigo.createWorker();
      worker.messageHandler(new Handler<JsonMessage>() {
        @Override
        public void handle(JsonMessage message) {
          int count = message.body().getInteger("count");
          worker.emit(new JsonObject().putNumber("count", count+1), message);
          worker.ack(message);
        }
      });
    }
  }

  private java.util.logging.Logger logger;

  @Override
  public void start() {
    logger = container.logger();

    NetworkDefinition network = new NetworkDefinition();
    // Create feeder A
    ComponentDefinition feederA = network.fromVerticle("feeder-a", ExampleFeeder.class.getName());

    // Create feeder B
    ComponentDefinition feederB = network.fromVerticle("feeder-b", ExampleFeeder.class.getName());

    // Feeder A feeds worker A, worker A needs to be saved to a variable so we
    // can feed its output to other workers.
    ComponentDefinition workerA = feederA.toVerticle("worker-a", ExampleWorker.class.getName(), 4).groupBy(new RandomGrouping());

    // Worker A feeds both worker B and worker C.
    workerA.toVerticle("worker-b", ExampleWorker.class.getName(), 4).groupBy(new RandomGrouping());
    workerA.toVerticle("worker-c", ExampleWorker.class.getName(), 4).groupBy(new RandomGrouping());

    // Feeder A also feeds worker D.
    ComponentDefinition workerD = feederA.toVerticle("worker-d", ExampleWorker.class.getName(), 2).groupBy(new RoundGrouping());

    // Worker D feeds worker E which feeds worker F. We need to safe worker F
    // (the return value) to a variable since it's fed by more than one other component.
    ComponentDefinition workerF = workerD.toVerticle("worker-e", ExampleWorker.class.getName(), 4).groupBy(new AllGrouping())
        .toVerticle("worker-f", ExampleWorker.class.getName(), 2).groupBy(new RandomGrouping());

    // Feeder B feeds worker G which feeds worker H which feeds worker F.
    feederB.toVerticle("worker-g", ExampleWorker.class.getName(), 2).groupBy(new FieldsGrouping("count"))
        .toVerticle("worker-h", ExampleWorker.class.getName(), 2).groupBy(new RandomGrouping()).filterBy(new TagsFilter("count"))
        .toVerticle(workerF);

    // Deploy the network usign a local cluster.
    final Cluster cluster = new LocalCluster(vertx, container);
    cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          logger.warn("Failed to deploy network.", result.cause());
        }
        else {
          logger.info("Deployed complex network.");
        }

        final NetworkContext context = result.result();

        // Shut down the network in ten seconds.
        vertx.setTimer(10000, new Handler<Long>() {
          @Override
          public void handle(Long timerID) {
            cluster.shutdown(context);
          }
        });
      }
    });
  }

}
