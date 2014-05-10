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
package net.kuujo.vertigo.io.batch.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.kuujo.vertigo.io.batch.OutputBatch;
import net.kuujo.vertigo.io.group.OutputGroup;
import net.kuujo.vertigo.io.group.impl.BaseOutputGroup;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Top-level output batch.<p>
 *
 * This output batch implementation is the API that is exposed to users.
 * However, internally it wraps other {@link OutputBatch} instances
 * which handle grouping logic for individual output connections.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class BaseOutputBatch implements OutputBatch {
  private final String id;
  private final Vertx vertx;
  private final Collection<OutputBatch> connections;

  public BaseOutputBatch(String id, Vertx vertx, Collection<OutputBatch> connections) {
    this.id = id;
    this.vertx = vertx;
    this.connections = connections;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public OutputBatch setSendQueueMaxSize(int maxSize) {
    for (OutputBatch group : connections) {
      group.setSendQueueMaxSize(maxSize);
    }
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    int maxSize = 0;
    for (OutputBatch group : connections) {
      maxSize += group.getSendQueueMaxSize();
    }
    return maxSize;
  }

  @Override
  public int size() {
    int highest = 0;
    for (OutputBatch group : connections) {
      highest = Math.max(highest, group.size());
    }
    return highest;
  }

  @Override
  public boolean sendQueueFull() {
    for (OutputBatch group : connections) {
      if (group.sendQueueFull()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public OutputBatch drainHandler(Handler<Void> handler) {
    for (OutputBatch group : connections) {
      group.drainHandler(handler);
    }
    return this;
  }

  @Override
  public OutputBatch group(Handler<OutputGroup> handler) {
    return group(UUID.randomUUID().toString(), handler);
  }

  @Override
  public OutputBatch group(final String name, final Handler<OutputGroup> handler) {
    final List<OutputGroup> groups = new ArrayList<>();
    final int connectionsSize = connections.size();
    if (connectionsSize == 0) {
      handler.handle(new BaseOutputGroup(name, vertx, groups));
    } else {
      for (OutputBatch connection : connections) {
        connection.group(name, new Handler<OutputGroup>() {
          @Override
          public void handle(OutputGroup group) {
            groups.add(group);
            if (groups.size() == connectionsSize) {
              handler.handle(new BaseOutputGroup(name, vertx, groups));
            }
          }
        });
      }
    }
    return this;
  }

  @Override
  public OutputBatch send(Object message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(String message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Boolean message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Character message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Short message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Integer message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Long message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Double message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Float message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Buffer message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(JsonObject message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(JsonArray message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Byte message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(byte[] message) {
    for (OutputBatch output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public void end() {
    for (OutputBatch output : connections) {
      output.end();
    }
  }

}
