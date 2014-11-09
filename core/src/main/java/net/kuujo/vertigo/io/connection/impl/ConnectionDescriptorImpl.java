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
package net.kuujo.vertigo.io.connection.impl;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.io.connection.ConnectionDescriptor;
import net.kuujo.vertigo.io.connection.SourceDescriptor;
import net.kuujo.vertigo.io.connection.TargetDescriptor;
import net.kuujo.vertigo.io.partition.*;

/**
 * Connection descriptor implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ConnectionDescriptorImpl implements ConnectionDescriptor {
  private final SourceDescriptor source;
  private final TargetDescriptor target;
  private final Partitioner partitioner;

  public ConnectionDescriptorImpl(JsonObject connection) {
    JsonObject source = connection.getJsonObject("source");
    if (source == null) {
      throw new IllegalArgumentException("Invalid connection descriptor: No connection source defined");
    }
    this.source = new SourceDescriptorImpl(source);
    JsonObject target = connection.getJsonObject("target");
    if (target == null) {
      throw new IllegalArgumentException("Invalid connection descriptor: No connection target defined");
    }
    this.target = new TargetDescriptorImpl(target);
    JsonObject jsonPartitioner = connection.getJsonObject("partitioner");
    if (jsonPartitioner != null) {
      String partitioner = jsonPartitioner.getString("type");
      if (partitioner == null) {
        throw new IllegalArgumentException("Invalid connection descriptor: No partitioner type defined");
      }
      switch (partitioner) {
        case "round":
          this.partitioner = new RoundRobinPartitioner();
          break;
        case "random":
          this.partitioner = new RandomPartitioner();
          break;
        case "hash":
          String header = jsonPartitioner.getString("header");
          if (header != null) {
            this.partitioner = new HashPartitioner(header);
          } else {
            this.partitioner = new RoundRobinPartitioner();
          }
          break;
        case "all":
          this.partitioner = new AllPartitioner();
          break;
        default:
          this.partitioner = new RoundRobinPartitioner();
          break;
      }
    } else {
      this.partitioner = new RoundRobinPartitioner();
    }
  }

  @Override
  public SourceDescriptor source() {
    return source;
  }

  @Override
  public TargetDescriptor target() {
    return target;
  }

  @Override
  public Partitioner partitioner() {
    return partitioner;
  }

}
