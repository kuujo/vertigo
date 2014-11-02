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

import java.util.Collections;
import java.util.List;

/**
 * Selector that sends messages based on a simple mod-hash algorithm.<p>
 *
 * The hash selector is a consistent-hashing based grouping. Given
 * a value on which to hash, this grouping guarantees that workers will
 * always receive messages with the same values.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class HashPartitioner implements Partitioner {
  private String header;

  public HashPartitioner() {
  }

  public HashPartitioner(String header) {
    this.header = header;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<OutputConnectionInfo> partition(MultiMap headers, List<OutputConnectionInfo> connections) {
    Object value = headers.get(header);
    if (header == null) {
      return Collections.EMPTY_LIST;
    }
    int index = Math.abs(value.hashCode() % connections.size());
    return connections.subList(index, index+1);
  }

}
