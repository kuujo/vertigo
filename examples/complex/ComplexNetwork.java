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
import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.json.JsonObject;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.AllGrouping;
import net.kuujo.vertigo.input.filter.TagsFilter;
import net.kuujo.vertigo.feeder.BasicFeeder;
import net.kuujo.vertigo.worker.Worker;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * A complex network implementation.
 *
 * @author Jordan Halterman
 */
public class ComplexNetworkVerticle extends Verticle {

  /**
   * An example feeder verticle.
   */
  public static class ExampleFeeder extends Verticle {

    private Logger logger;

    @Override
    public void start() {
      Vertigo vertigo = new Vertigo(this);
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
  public static class ExampleWorker extends Verticle {

    private Logger logger;

    private Worker worker;

    @Override
    public void start() {
      Vertigo vertigo = new Vertigo(this);
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

    Network network = new Network("test");

    // Create feeder A at the event bus address "feeder-a"
    Component feederA = network.addVerticle("feeder-a", ExampleFeeder.class.getName());

    // Create feeder B at the event bus address "feeder-b"
    Component feederB = network.addVerticle("feeder-b", ExampleFeeder.class.getName());

    // Create worker A at the event bus address "worker-a"
    Component workerA = network.addVerticle("worker-a", ExampleWorker.class.getName(), 4);

    // Worker A listens for output from feeder A.
    workerA.addInput("feeder-a").groupBy(new RandomGrouping());

    // Create worker B and worker C, both of which listen for output from worker A.
    Component workerB = network.addVerticle("worker-b", ExampleWorker.class.getName(), 2);
    workerB.addInput("worker-a").groupBy(new RandomGrouping());

    Component workerC = network.addVerticle("worker-b", ExampleWorker.class.getName(), 4);
    workerC.addInput("worker-a").groupBy(new RandomGrouping());

    // Feeder A also feeds worker D.
    Component workerD = network.addVerticle("worker-d", ExampleWorker.class.getName(), 2);
    workerD.addInput("feeder-a", new RoundGrouping());

    // Worker D feeds worker E which feeds worker F.
    Component workerE = network.addVerticle("worker-e", ExampleWorker.class.getName(), 4);
    workerE.addInput("worker-d", new AllGrouping());

    Component workerF = network.addVerticle("worker-f", ExampleWorker.class.getName(), 2);
    workerF.addInput("worker-e", new RandomGrouping());

    // Feeder B feeds worker G which feeds worker H which feeds worker F.
    Component workerG = network.addVerticle("worker-g", ExampleWorker.class.getName(), 2);
    workerG.addInput("feeder-b").groupBy(new FieldsGrouping("count"));

    Component workerH = network.addVerticle("worker-h", ExampleWorker.class.getName(), 2);
    workerH.addInput("worker-g", new RandomGrouping()).filterBy(new TagsFilter("count"));

    workerF.addInput("worker-h");

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
