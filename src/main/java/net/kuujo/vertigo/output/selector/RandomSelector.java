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
package net.kuujo.vertigo.output.selector;

import java.util.List;
import java.util.Random;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.output.Connection;

/**
 * A *random* selector.
 *
 * The *random* selector dispatches messages to component workers randomly.
 *
 * @author Jordan Halterman
 */
public class RandomSelector implements Selector {
  private Random rand = new Random();

  public RandomSelector() {
  }

  @Override
  public List<Connection> select(JsonMessage message, List<Connection> connections) {
    int index = rand.nextInt(connections.size());
    return connections.subList(index, index+1);
  }

}
