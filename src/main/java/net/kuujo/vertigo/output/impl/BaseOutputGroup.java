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
package net.kuujo.vertigo.output.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.kuujo.vertigo.output.OutputGroup;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Top-level output group.<p>
 *
 * This output group implementation is the API that is exposed to users.
 * However, internally it wraps {@link ConnectionOutputGroup} instances
 * which handle grouping logic for individual output connections.
 *
 * @author Jordan Halterman
 */
public class BaseOutputGroup implements OutputGroup {
  private final String name;
  private final Collection<OutputGroup> connections;

  public BaseOutputGroup(String name, Collection<OutputGroup> connections) {
    this.name = name;
    this.connections = connections;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public OutputGroup setSendQueueMaxSize(int maxSize) {
    for (OutputGroup group : connections) {
      group.setSendQueueMaxSize(maxSize);
    }
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    int maxSize = 0;
    for (OutputGroup group : connections) {
      maxSize += group.getSendQueueMaxSize();
    }
    return maxSize;
  }

  @Override
  public boolean sendQueueFull() {
    for (OutputGroup group : connections) {
      if (group.sendQueueFull()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public OutputGroup drainHandler(Handler<Void> handler) {
    for (OutputGroup group : connections) {
      group.drainHandler(handler);
    }
    return this;
  }

  @Override
  public OutputGroup group(final String name, final Handler<OutputGroup> handler) {
    final List<OutputGroup> groups = new ArrayList<>();
    final int connectionsSize = connections.size();
    for (OutputGroup connection : connections) {
      connection.group(name, new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          groups.add(group);
          if (groups.size() == connectionsSize) {
            handler.handle(new BaseOutputGroup(name, groups));
          }
        }
      });
    }
    return this;
  }

  @Override
  public OutputGroup send(Object message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Object message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(String message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(String message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Boolean message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Boolean message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Character message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Character message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Short message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Short message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Integer message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Integer message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Long message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Long message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Double message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Double message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Float message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Float message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Buffer message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Buffer message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(JsonObject message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(JsonObject message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(JsonArray message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(JsonArray message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(Byte message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(Byte message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup send(byte[] message) {
    for (OutputGroup output : connections) {
      output.send(message);
    }
    return this;
  }

  @Override
  public OutputGroup send(byte[] message, String key) {
    for (OutputGroup output : connections) {
      output.send(message, key);
    }
    return this;
  }

  @Override
  public OutputGroup end() {
    for (OutputGroup output : connections) {
      output.end();
    }
    return this;
  }

}
