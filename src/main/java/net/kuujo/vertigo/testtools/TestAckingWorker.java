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

import net.kuujo.vertigo.java.WorkerVerticle;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.worker.Worker;

/**
 * A test worker that acks and emits all messages as children.
 *
 * @author Jordan Halterman
 */
public class TestAckingWorker extends WorkerVerticle {

  /**
   * Creates an acking worker definition.
   *
   * @return
   *   A component definition.
   */
  public static Component<Worker> createDefinition() {
    return createDefinition(1);
  }

  /**
   * Creates an acking worker definition.
   *
   * @param instances
   *   The number of instances.
   * @return
   *   A component definition.
   */
  public static Component<Worker> createDefinition(int instances) {
    return new Component<Worker>(Worker.class, UUID.randomUUID().toString(), TestAckingWorker.class.getName()).setInstances(instances);
  }

  @Override
  protected void handleMessage(JsonMessage message, Worker worker) {
    worker.emit(message);
    worker.ack(message);
  }

}
