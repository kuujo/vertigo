/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.output.partitioner;

import io.vertx.core.MultiMap;
import net.kuujo.vertigo.output.connection.OutputConnectionInfo;

import java.util.List;

/**
 * Selector that sends messages on all connections.<p>
 *
 * The *all* selector dispatches messages to all partitions of a component.
 * Thus, if a component has four partitions, all four component workers will
 * receive every message emitted to that component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class AllPartitioner implements Partitioner {

  public AllPartitioner() {
  }

  @Override
  public List<OutputConnectionInfo> partition(MultiMap headers, List<OutputConnectionInfo> connections) {
    return connections;
  }

}
