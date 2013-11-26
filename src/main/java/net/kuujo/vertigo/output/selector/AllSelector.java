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

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.output.Connection;

/**
 * An *all* selector.
 *
 * The *all* selector dispatches messages to all instances of a component.
 * Thus, if a component has four instances, all four component workers will
 * receive every message emitted to that component.
 *
 * @author Jordan Halterman
 */
public class AllSelector implements Selector {

  public AllSelector() {
  }

  @Override
  public List<Connection> select(JsonMessage message, List<Connection> connections) {
    return connections;
  }

}
