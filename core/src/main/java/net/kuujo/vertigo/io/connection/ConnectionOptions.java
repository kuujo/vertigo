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
package net.kuujo.vertigo.io.connection;

import io.vertx.codegen.annotations.Options;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.io.partitioner.*;

import java.io.Serializable;

/**
 * A connection represents a link between two components within a network.<p>
 *
 * When a connection is created, each instance of the source component
 * will be setup to send messages to each instance of the target component.
 * How messages are routed to multiple target instances can be configured
 * using selectors.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@Options
public class ConnectionOptions implements Serializable {

  /**
   * <code>source</code> is an object defining the connection source. See the
   * {@link SourceOptions} documentation for details on the source structure.
   */
  public static final String CONNECTION_SOURCE = "source";

  /**
   * <code>target</code> is an object defining the connection target. See the
   * {@link TargetOptions} documentation for details on the target structure.
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

  private SourceOptions source;
  private TargetOptions target;
  private Partitioner partitioner;

  public ConnectionOptions() {
  }

  public ConnectionOptions(ConnectionOptions options) {
    this.source = options.getSource();
    this.target = options.getTarget();
    this.partitioner = options.getPartitioner();
  }

  public ConnectionOptions(JsonObject options) {
    this.source = new SourceOptions(options.getJsonObject(CONNECTION_SOURCE));
    this.target = new TargetOptions(options.getJsonObject(CONNECTION_TARGET));
    String partitioner = options.getString(CONNECTION_PARTITIONER);
    if (partitioner != null) {
      switch (partitioner) {
        case "round":
          this.partitioner = new RoundRobinPartitioner();
          break;
        case "random":
          this.partitioner = new RandomPartitioner();
          break;
        case "hash":
          String header = options.getString("partitionHeader");
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

  /**
   * Sets the connection source.
   *
   * @param source The connection source.
   * @return The connection options.
   */
  public ConnectionOptions setSource(SourceOptions source) {
    this.source = source;
    return this;
  }

  /**
   * Returns the connection source.
   *
   * @return The connection source.
   */
  public SourceOptions getSource() {
    return source;
  }

  /**
   * Sets the connection target.
   *
   * @param target The connection target.
   * @return The connection options.
   */
  public ConnectionOptions setTarget(TargetOptions target) {
    this.target = target;
    return this;
  }

  /**
   * Returns the connection target.
   *
   * @return The connection target.
   */
  public TargetOptions getTarget() {
    return target;
  }

  /**
   * Returns the connection partitioner.
   *
   * @return The connection selector.
   */
  public Partitioner getPartitioner() {
    return partitioner;
  }

  /**
   * Sets the connection partitioner.
   *
   * @param partitioner The partitioner with which to partition individual connections.
   * @return The connection configuration.
   */
  public ConnectionOptions setPartitioner(Partitioner partitioner) {
    this.partitioner = partitioner;
    return this;
  }

  /**
   * Sets a round-robin partitioner on the connection.
   *
   * @return The connection configuration.
   */
  public ConnectionOptions roundPartition() {
    this.partitioner = new RoundRobinPartitioner();
    return this;
  }

  /**
   * Sets a random partitioner on the connection.
   *
   * @return The connection configuration.
   */
  public ConnectionOptions randomPartition() {
    this.partitioner = new RandomPartitioner();
    return this;
  }

  /**
   * Sets a mod-hash based partitioner on the connection.
   *
   * @param header The hash header.
   * @return The connection configuration.
   */
  public ConnectionOptions hashPartition(String header) {
    this.partitioner = new HashPartitioner(header);
    return this;
  }

  /**
   * Sets an all partitioner on the connection.
   *
   * @return The connection configuration.
   */
  public ConnectionOptions allPartition() {
    this.partitioner = new AllPartitioner();
    return this;
  }

  /**
   * Sets a custom partitioner on the connection.
   *
   * @param partitioner The custom selector to set.
   * @return The connection configuration.
   */
  public ConnectionOptions partition(Partitioner partitioner) {
    this.partitioner = partitioner;
    return this;
  }

}
