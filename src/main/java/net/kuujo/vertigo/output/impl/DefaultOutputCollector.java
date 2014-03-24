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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.hooks.OutputHook;
import net.kuujo.vertigo.network.auditor.Acker;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.output.OutputPort;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default output collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputCollector implements OutputCollector, Observer<OutputContext> {
  private final Vertx vertx;
  private final OutputContext context;
  private final Acker acker;
  private final List<OutputHook> hooks = new ArrayList<>();
  private final Map<String, OutputPort> streams = new HashMap<>();

  public DefaultOutputCollector(Vertx vertx, OutputContext context, Acker acker) {
    this.vertx = vertx;
    this.context = context;
    this.acker = acker;
  }

  @Override
  public OutputContext context() {
    return context;
  }

  @Override
  public OutputCollector addHook(OutputHook hook) {
    hooks.add(hook);
    for (OutputPort stream : streams.values()) {
      stream.addHook(hook);
    }
    return this;
  }

  @Override
  public Collection<OutputPort> streams() {
    return streams.values();
  }

  @Override
  public OutputPort stream(String name) {
    OutputPort stream = streams.get(name);
    if (stream == null) {
      OutputPortContext context = OutputPortContext.Builder.newBuilder()
          .setAddress(UUID.randomUUID().toString())
          .setName(name)
          .build();
      stream = new DefaultOutputPort(vertx, context, acker);
      streams.put(name, stream);
    }
    return stream;
  }

  @Override
  public void update(OutputContext update) {
    Iterator<OutputPort> iter = streams.values().iterator();
    while (iter.hasNext()) {
      OutputPort stream = iter.next();
      boolean exists = false;
      for (OutputPortContext output : update.ports()) {
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

    for (OutputPortContext output : update.ports()) {
      boolean exists = false;
      for (OutputPort stream : streams.values()) {
        if (stream.context().equals(output)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        streams.put(output.name(), new DefaultOutputPort(vertx, output, acker).open());
      }
    }
  }

  @Override
  public OutputCollector open() {
    return open(null);
  }

  @Override
  public OutputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
    if (streams.isEmpty()) {
      final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.ports().size());
      startCounter.setHandler(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });

      for (OutputPortContext stream : context.ports()) {
        streams.put(stream.name(), new DefaultOutputPort(vertx, stream, acker).open(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              startCounter.fail(result.cause());
            } else {
              startCounter.succeed();
            }
          }
        }));
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
    final CountingCompletionHandler<Void> stopCounter = new CountingCompletionHandler<Void>(streams.size());
    stopCounter.setHandler(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          streams.clear();
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });

    for (OutputPort output : streams.values()) {
      output.close(new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            stopCounter.fail(result.cause());
          } else {
            stopCounter.succeed();
          }
        }
      });
    }
  }

}
