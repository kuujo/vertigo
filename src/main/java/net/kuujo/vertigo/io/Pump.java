/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.io;

import org.vertx.java.core.Handler;

/**
 * A Vertigo message pump similar to the Vert.x stream pump.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class Pump {
  private final Input<?> input;
  private final Output<?> output;
  private int pumped;

  /**
   * Creates a new pump.
   *
   * @param input The input from which to read messages.
   * @param output The output to which to write messages.
   * @return A new pump.
   */
  public static Pump createPump(Input<?> input, Output<?> output) {
    return new Pump(input, output);
  }

  private Pump(Input<?> input, Output<?> output) {
    this.input = input;
    this.output = output;
  }

  /**
   * Returns the number of messages pumped.
   *
   * @return The number of messages pumped by the pump.
   */
  public int messagesPumped() {
    return pumped;
  }

  /**
   * Starts the pump.
   */
  @SuppressWarnings("rawtypes")
  public void start() {
    input.messageHandler(new Handler() {
      @Override
      public void handle(Object message) {
        output.send(message);
        pumped++;
      }
    });
  }

  /**
   * Stops the pump.
   */
  public void stop() {
    input.messageHandler(null);
  }

}
