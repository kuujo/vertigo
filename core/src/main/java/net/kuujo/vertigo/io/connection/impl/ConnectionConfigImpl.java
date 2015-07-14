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
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.io.connection.SourceConfig;
import net.kuujo.vertigo.io.connection.TargetConfig;
import net.kuujo.vertigo.io.port.InputPortConfig;
import net.kuujo.vertigo.io.port.OutputPortConfig;

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
public class ConnectionConfigImpl implements ConnectionConfig {
  private SourceConfig source;
  private TargetConfig target;
  private boolean ordered;
  private boolean atLeastOnce;

  public ConnectionConfigImpl() {
    this.source = new SourceConfigImpl();
    this.target = new TargetConfigImpl();
  }

  public ConnectionConfigImpl(ConnectionConfig connection) {
    this.source = connection.getSource();
    this.target = connection.getTarget();
    this.ordered = connection.isOrdered();
    this.atLeastOnce = connection.isAtLeastOnce();
  }

  public ConnectionConfigImpl(OutputPortConfig output, InputPortConfig input) {
    this.source = new SourceConfigImpl(output);
    this.target = new TargetConfigImpl(input);
  }

  public ConnectionConfigImpl(JsonObject connection) {
    update(connection);
  }

  @Override
  public ConnectionConfigImpl setSource(SourceConfig source) {
    this.source = source;
    return this;
  }

  @Override
  public SourceConfig getSource() {
    return source;
  }

  @Override
  public ConnectionConfigImpl setTarget(TargetConfig target) {
    this.target = target;
    return this;
  }

  @Override
  public TargetConfig getTarget() {
    return target;
  }

  @Override
  public ConnectionConfig setOrdered(boolean ordered) {
    this.ordered = ordered;
    return this;
  }

  @Override
  public boolean isOrdered() {
    return ordered;
  }

  @Override
  public ConnectionConfig setAtLeastOnce(boolean atLeastOnce) {
    this.atLeastOnce = atLeastOnce;
    return this;
  }

  @Override
  public boolean isAtLeastOnce() {
    return atLeastOnce;
  }

  @Override
  public void update(JsonObject connection) {
    if (connection.containsKey(CONNECTION_SOURCE)) {
      if (this.source == null) {
        this.source = new SourceConfigImpl(connection.getJsonObject(CONNECTION_SOURCE));
      } else {
        this.source.update(connection.getJsonObject(CONNECTION_SOURCE));
      }
    }
    if (connection.containsKey(CONNECTION_TARGET)) {
      if (this.target == null) {
        this.target = new TargetConfigImpl(connection.getJsonObject(CONNECTION_TARGET));
      } else {
        this.target.update(connection.getJsonObject(CONNECTION_TARGET));
      }
    }
    if (connection.containsKey(CONNECTION_ORDERED)) {
      this.ordered = connection.getBoolean(CONNECTION_ORDERED);
    }
    if (connection.containsKey(CONNECTION_AT_LEAST_ONCE)) {
      this.atLeastOnce = connection.getBoolean(CONNECTION_AT_LEAST_ONCE);
    }
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put(CONNECTION_SOURCE, source != null ? source.toJson() : null);
    json.put(CONNECTION_TARGET, target != null ? target.toJson() : null);
    json.put(CONNECTION_ORDERED, ordered);
    json.put(CONNECTION_AT_LEAST_ONCE, atLeastOnce);
    return json;
  }

}
