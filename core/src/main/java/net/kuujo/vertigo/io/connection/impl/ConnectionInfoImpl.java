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
import net.kuujo.vertigo.io.connection.ConnectionInfo;
import net.kuujo.vertigo.io.connection.SourceInfo;
import net.kuujo.vertigo.io.connection.TargetInfo;
import net.kuujo.vertigo.io.partition.*;

/**
 * A connection represents a link between two components within a network.<p>
 *
 * When a connection is created, each partition of the source component
 * will be setup to send messages to each partition of the target component.
 * How messages are routed to multiple target partitions can be configured
 * using selectors.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ConnectionInfoImpl implements ConnectionInfo {

  private SourceInfo source;
  private TargetInfo target;
  private Partitioner partitioner;

  public ConnectionInfoImpl() {
  }

  public ConnectionInfoImpl(ConnectionInfo connection) {
    this.source = connection.getSource();
    this.target = connection.getTarget();
    this.partitioner = connection.getPartitioner();
  }

  public ConnectionInfoImpl(JsonObject connection) {
    JsonObject source = connection.getJsonObject("source");
    if (source == null) {
      throw new IllegalArgumentException("Invalid connection descriptor: No connection source defined");
    }
    this.source = new SourceInfoImpl(source);
    JsonObject target = connection.getJsonObject("target");
    if (target == null) {
      throw new IllegalArgumentException("Invalid connection descriptor: No connection target defined");
    }
    this.target = new TargetInfoImpl(target);
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
  public ConnectionInfoImpl setSource(SourceInfo source) {
    this.source = source;
    return this;
  }

  @Override
  public SourceInfo getSource() {
    return source;
  }

  @Override
  public ConnectionInfoImpl setTarget(TargetInfo target) {
    this.target = target;
    return this;
  }

  @Override
  public TargetInfo getTarget() {
    return target;
  }

  @Override
  public Partitioner getPartitioner() {
    return partitioner;
  }

  @Override
  public ConnectionInfoImpl setPartitioner(Partitioner partitioner) {
    this.partitioner = partitioner;
    return this;
  }

  @Override
  public ConnectionInfoImpl roundPartition() {
    this.partitioner = new RoundRobinPartitioner();
    return this;
  }

  @Override
  public ConnectionInfoImpl randomPartition() {
    this.partitioner = new RandomPartitioner();
    return this;
  }

  @Override
  public ConnectionInfoImpl hashPartition(String header) {
    this.partitioner = new HashPartitioner(header);
    return this;
  }

  @Override
  public ConnectionInfoImpl allPartition() {
    this.partitioner = new AllPartitioner();
    return this;
  }

  @Override
  public ConnectionInfoImpl partition(Partitioner partitioner) {
    this.partitioner = partitioner;
    return this;
  }

}
