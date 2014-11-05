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
import net.kuujo.vertigo.io.connection.ConnectionDefinition;
import net.kuujo.vertigo.io.connection.SourceDefinition;
import net.kuujo.vertigo.io.connection.TargetDefinition;
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
public class ConnectionDefinitionImpl implements ConnectionDefinition {

  /**
   * <code>source</code> is an object defining the connection source. See the
   * {@link net.kuujo.vertigo.io.connection.SourceDefinition} documentation for details on the source structure.
   */
  public static final String CONNECTION_SOURCE = "source";

  /**
   * <code>target</code> is an object defining the connection target. See the
   * {@link net.kuujo.vertigo.io.connection.TargetDefinition} documentation for details on the target structure.
   */
  public static final String CONNECTION_TARGET = "target";

  /**
   * <code>partitioner</code> is an object defining the connection partitioner. The partitioner
   * definition should contain a <code>type</code> which indicates the partitioner type,
   * e.g. <code>round-robin</code>, <code>random</code>, <code>hash</code>, <code>fair</code>,
   * <code>all</code>, or <code>custom</code>. If a <code>custom</code> selector is indicated
   * then an additional <code>selector</code> field must be provided which indicates the
   * custom selector class.
   */
  public static final String CONNECTION_PARTITIONER = "partitioner";

  private SourceDefinition source;
  private TargetDefinition target;
  private Partitioner partitioner;

  public ConnectionDefinitionImpl() {
  }

  public ConnectionDefinitionImpl(ConnectionDefinitionImpl options) {
    this.source = options.getSource();
    this.target = options.getTarget();
    this.partitioner = options.getPartitioner();
  }

  public ConnectionDefinitionImpl(JsonObject definition) {
    this.source = new SourceDefinitionImpl(definition.getJsonObject(CONNECTION_SOURCE));
    this.target = new TargetDefinitionImpl(definition.getJsonObject(CONNECTION_TARGET));
    String partitioner = definition.getString(CONNECTION_PARTITIONER);
    if (partitioner != null) {
      switch (partitioner) {
        case "round":
          this.partitioner = new RoundRobinPartitioner();
          break;
        case "random":
          this.partitioner = new RandomPartitioner();
          break;
        case "hash":
          String header = definition.getString("partitionHeader");
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
  public ConnectionDefinitionImpl setSource(SourceDefinition source) {
    this.source = source;
    return this;
  }

  @Override
  public SourceDefinition getSource() {
    return source;
  }

  @Override
  public ConnectionDefinitionImpl setTarget(TargetDefinition target) {
    this.target = target;
    return this;
  }

  @Override
  public TargetDefinition getTarget() {
    return target;
  }

  @Override
  public Partitioner getPartitioner() {
    return partitioner;
  }

  @Override
  public ConnectionDefinitionImpl setPartitioner(Partitioner partitioner) {
    this.partitioner = partitioner;
    return this;
  }

  @Override
  public ConnectionDefinitionImpl roundPartition() {
    this.partitioner = new RoundRobinPartitioner();
    return this;
  }

  @Override
  public ConnectionDefinitionImpl randomPartition() {
    this.partitioner = new RandomPartitioner();
    return this;
  }

  @Override
  public ConnectionDefinitionImpl hashPartition(String header) {
    this.partitioner = new HashPartitioner(header);
    return this;
  }

  @Override
  public ConnectionDefinitionImpl allPartition() {
    this.partitioner = new AllPartitioner();
    return this;
  }

  @Override
  public ConnectionDefinitionImpl partition(Partitioner partitioner) {
    this.partitioner = partitioner;
    return this;
  }

}
