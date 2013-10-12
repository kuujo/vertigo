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

import net.kuujo.vertigo.component.worker.Worker;
import net.kuujo.vertigo.definition.ComponentDefinition;
import net.kuujo.vertigo.java.VertigoVerticle;
import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * A test worker that times out messages.
 *
 * @author Jordan Halterman
 */
public class TimeoutWorker extends VertigoVerticle {

  /**
   * Creates an timeout worker definition.
   *
   * @return
   *   A component definition.
   */
  public static ComponentDefinition createDefinition() {
    return createDefinition(1);
  }

  /**
   * Creates an timeout worker definition.
   *
   * @param workers
   *   The number of workers.
   * @return
   *   A component definition.
   */
  public static ComponentDefinition createDefinition(int workers) {
    return new ComponentDefinition(UUID.randomUUID().toString())
        .setType(ComponentDefinition.VERTICLE).setMain(TimeoutWorker.class.getName())
        .setWorkers(workers);
  }

  @Override
  public void start() {
    final Worker worker = vertigo.createWorker();
    worker.messageHandler(new Handler<JsonMessage>() {
      @Override
      public void handle(JsonMessage message) {
        // Do nothing useful.
      }
    }).start();
  }

}
