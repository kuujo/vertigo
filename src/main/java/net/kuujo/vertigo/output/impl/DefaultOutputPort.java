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
import java.util.Iterator;
import java.util.List;

import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.context.OutputStreamContext;
import net.kuujo.vertigo.output.OutputGroup;
import net.kuujo.vertigo.output.OutputPort;
import net.kuujo.vertigo.output.OutputStream;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Default output port implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputPort implements OutputPort, Observer<OutputPortContext> {
  private static final int DEFAULT_SEND_QUEUE_MAX_SIZE = 10000;
  private final Vertx vertx;
  private OutputPortContext context;
  private final List<OutputStream> streams = new ArrayList<>();
  private int maxQueueSize = DEFAULT_SEND_QUEUE_MAX_SIZE;
  private Handler<Void> drainHandler;
  private boolean open;

  public DefaultOutputPort(Vertx vertx, OutputPortContext context) {
    this.vertx = vertx;
    this.context = context;
  }

  DefaultOutputPort setContext(OutputPortContext context) {
    this.context = context;
    return this;
  }

  @Override
  public String name() {
    return context.name();
  }

  @Override
  public OutputPortContext context() {
    return context;
  }

  @Override
  public void update(OutputPortContext update) {
    Iterator<OutputStream> iter = streams.iterator();
    while (iter.hasNext()) {
      OutputStream stream = iter.next();
      boolean exists = false;
      for (OutputStreamContext output : update.streams()) {
        if (output.equals(stream.context())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        stream.close();
        iter.remove();
      }
    }

    for (OutputStreamContext output : update.streams()) {
      boolean exists = false;
      for (OutputStream stream : streams) {
        if (stream.context().equals(output)) {
          exists = true;
          break;
        }
      }

      if (!exists) {
        OutputStream stream = new DefaultOutputStream(vertx, output);
        streams.add(stream.open());
      }
    }
  }

  @Override
  public OutputPort setSendQueueMaxSize(int maxSize) {
    this.maxQueueSize = maxSize;
    for (OutputStream stream : streams) {
      stream.setSendQueueMaxSize(maxQueueSize);
    }
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return maxQueueSize;
  }

  @Override
  public boolean sendQueueFull() {
    for (OutputStream stream : streams) {
      if (stream.sendQueueFull()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public OutputPort drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    for (OutputStream stream : streams) {
      stream.drainHandler(handler);
    }
    return this;
  }

  @Override
  public OutputPort open() {
    return open(null);
  }

  @Override
  public OutputPort open(final Handler<AsyncResult<Void>> doneHandler) {
    if (!open) {
      streams.clear();
      open = true;
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(context.streams().size()).setHandler(doneHandler);
      for (OutputStreamContext stream : context.streams()) {
        streams.add(new DefaultOutputStream(vertx, stream)
          .setSendQueueMaxSize(Math.round(maxQueueSize / context.streams().size()))
          .drainHandler(drainHandler).open(counter));
      }
    }
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    if (open) {
      List<OutputStream> streams = new ArrayList<>(this.streams);
      this.streams.clear();
      open = false;
      final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(streams.size()).setHandler(doneHandler);
      for (OutputStream stream : streams) {
        stream.close(counter);
      }
    }
  }

  @Override
  public OutputPort group(final String name, final Handler<OutputGroup> handler) {
    final List<OutputGroup> groups = new ArrayList<>();
    final int streamSize = streams.size();
    for (OutputStream stream : streams) {
      stream.group(name, new Handler<OutputGroup>() {
        @Override
        public void handle(OutputGroup group) {
          groups.add(group);
          if (groups.size() == streamSize) {
            handler.handle(new BaseOutputGroup(name, groups));
          }
        }
      });
    }
    return this;
  }

  @Override
  public OutputPort send(Object message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(String message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Boolean message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Character message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Short message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Integer message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Long message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Double message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Float message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Buffer message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(JsonObject message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(JsonArray message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(Byte message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort send(byte[] message) {
    for (OutputStream stream : streams) {
      stream.send(message);
    }
    return this;
  }

}
