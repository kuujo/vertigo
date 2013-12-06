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

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.annotations.Input;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.runtime.FailureException;
import net.kuujo.vertigo.runtime.TimeoutException;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.AllGrouping;
import net.kuujo.vertigo.java.RichFeederVerticle;
import net.kuujo.vertigo.java.RichWorkerVerticle;
import net.kuujo.vertigo.java.VertigoVerticle;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.worker.Worker;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;

/**
 * A complex network implementation.
 *
 * @author Jordan Halterman
 */
public class ComplexNetwork extends VertigoVerticle {

  /**
   * A simple number feeder.
   */
  public static class SimpleFeeder extends RichFeederVerticle {
    @Override
    protected void start(Feeder feeder) {
      logger = container.logger();
      feeder.setAutoRetry(true);
      super.start(feeder);
    }

    @Override
    protected void nextMessage() {
      emit(new JsonObject().putNumber("count", 0));
    }

    @Override
    protected void handleAck(MessageId messageId) {
      logger.info("Successfully processed " + messageId.correlationId());
    }

    @Override
    protected void handleFailure(MessageId messageId, FailureException cause) {
      logger.warn(messageId.correlationId() + " failed: " + cause.getMessage());
    }

    @Override
    protected void handleTimeout(MessageId messageId, TimeoutException cause) {
      logger.warn(messageId.correlationId() + " timed out");
    }
  }

  /**
   * An example worker verticle.
   *
   * This verticle can be used for all workers in the network, dispite the fact
   * that different network components use different groupings and filters. This
   * is demonstrative of the separation between component definitions and implementations.
   */
  @Input(schema={@Input.Field(name="count", type=Number.class)})
  public static class SimpleWorker extends RichWorkerVerticle {
    @Override
    protected void handleMessage(JsonMessage message) {
      int count = message.body().getInteger("count");
      emit(new JsonObject().putNumber("count", count+1), message);
      ack(message);
    }
  }

  private Logger logger;

  @Override
  @SuppressWarnings("unchecked")
  public void start() {
    logger = container.logger();

    // Create a new network from the protected Vertigo instance.
    Network network = vertigo.createNetwork("test");

    // Create feeder A at the event bus address "feeder-a"
    network.addFeeder("feeder-a", SimpleFeeder.class.getName());

    // Create feeder B at the event bus address "feeder-b"
    network.addFeeder("feeder-b", SimpleFeeder.class.getName());

    // Create worker A at the event bus address "worker-a" with four instances.
    // Worker A listens for output from feeder A.
    network.addWorker("worker-a", SimpleWorker.class.getName(), 4).addInput("feeder-a").groupBy(new RandomGrouping());

    // Create worker B and worker C, both of which listen for output from worker A.
    network.addWorker("worker-b", SimpleWorker.class.getName(), 2).addInput("worker-a").groupBy(new RandomGrouping());
    network.addWorker("worker-c", SimpleWorker.class.getName(), 4).addInput("worker-a").groupBy(new RandomGrouping());

    // Feeder A also feeds worker D.
    network.addWorker("worker-d", SimpleWorker.class.getName(), 2).addInput("feeder-a", new RoundGrouping());

    // Worker D feeds worker E which feeds worker F.
    network.addWorker("worker-e", SimpleWorker.class.getName(), 4).addInput("worker-d").groupBy(new AllGrouping());
    Component<Worker> workerf = network.addWorker("worker-f", SimpleWorker.class.getName(), 4);
    workerf.addInput("worker-e").groupBy(new RandomGrouping());

    // Feeder B feeds worker G which feeds worker H which feeds worker F.
    network.addWorker("worker-g", SimpleWorker.class.getName(), 2).addInput("feeder-b").groupBy(new FieldsGrouping("count"));

    network.addWorker("worker-h", SimpleWorker.class.getName(), 2).addInput("worker-g").groupBy(new RandomGrouping());

    workerf.addInput("worker-h");

    // Deploy the network using a local cluster.
    vertigo.deployLocalNetwork(network, new Handler<AsyncResult<NetworkContext>>() {
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
            vertigo.shutdownLocalNetwork(context);
          }
        });
      }
    });
  }

}
