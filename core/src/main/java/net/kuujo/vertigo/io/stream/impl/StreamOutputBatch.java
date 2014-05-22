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
package net.kuujo.vertigo.io.stream.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.kuujo.vertigo.io.batch.OutputBatch;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.connection.impl.ConnectionOutputBatch;
import net.kuujo.vertigo.io.group.OutputGroup;
import net.kuujo.vertigo.io.group.impl.BaseOutputGroup;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Stream level output batch.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class StreamOutputBatch implements OutputBatch {
  private final String id;
  private final DefaultOutputStream stream;
  private final List<ConnectionOutputBatch> batches;

  public StreamOutputBatch(String id, DefaultOutputStream stream, List<ConnectionOutputBatch> batches) {
    this.id = id;
    this.stream = stream;
    this.batches = batches;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public Vertx vertx() {
    return stream.vertx();
  }

  @Override
  public int size() {
    return stream.size();
  }

  @Override
  public OutputBatch setSendQueueMaxSize(int maxSize) {
    stream.setSendQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return stream.getSendQueueMaxSize();
  }

  @Override
  public boolean sendQueueFull() {
    return stream.sendQueueFull();
  }

  @Override
  public OutputBatch drainHandler(Handler<Void> handler) {
    stream.drainHandler(handler);
    return this;
  }

  @Override
  public OutputBatch send(Object message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(String message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Short message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Integer message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Long message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Float message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Double message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Boolean message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Byte message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(byte[] message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Character message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(JsonArray message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(JsonObject message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch send(Buffer message) {
    for (OutputConnection connection : stream.selector.select(message, batches)) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputBatch group(Handler<OutputGroup> handler) {
    return group(UUID.randomUUID().toString(), handler);
  }

  @Override
  public OutputBatch group(String name, Handler<OutputGroup> handler) {
    return group(name, null, handler);
  }

  @Override
  public OutputBatch group(final String name, final Object args, final Handler<OutputGroup> handler) {
    final List<OutputGroup> groups = new ArrayList<>();
    List<ConnectionOutputBatch> batches = stream.selector.select(name, this.batches);
    final int batchesSize = batches.size();
    for (ConnectionOutputBatch batch : batches) {
      batch.group(name, args, new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          groups.add(group);
          if (groups.size() == batchesSize) {
            handler.handle(new BaseOutputGroup(name, stream.vertx(), groups));
          }
        }
      });
    }
    return this;
  }

  @Override
  public void end() {
    for (ConnectionOutputBatch batch : batches) {
      batch.end();
    }
  }

  @Override
  public <T> void end(T args) {
    for (ConnectionOutputBatch batch : batches) {
      batch.end(args);
    }
  }

}
