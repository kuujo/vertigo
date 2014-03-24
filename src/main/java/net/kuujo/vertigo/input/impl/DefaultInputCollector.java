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
package net.kuujo.vertigo.input.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.InputStreamContext;
import net.kuujo.vertigo.hooks.InputHook;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.input.InputStream;
import net.kuujo.vertigo.network.auditor.Acker;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.Observer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Default input collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputCollector implements InputCollector, Observer<InputContext> {
  private final Vertx vertx;
  private final InputContext context;
  private final Acker acker;
  private final List<InputHook> hooks = new ArrayList<>();
  private final Map<String, InputStream> streams = new HashMap<>();

  public DefaultInputCollector(Vertx vertx, InputContext context, Acker acker) {
    this.vertx = vertx;
    this.context = context;
    this.acker = acker;
  }

  @Override
  public InputContext context() {
    return context;
  }

  @Override
  public InputCollector addHook(InputHook hook) {
    hooks.add(hook);
    for (InputStream stream : streams.values()) {
      stream.addHook(hook);
    }
    return this;
  }

  @Override
  public Collection<InputStream> streams() {
    return streams.values();
  }

  @Override
  public InputStream stream(String name) {
    InputStream stream = streams.get(name);
    if (stream == null) {
      InputStreamContext context = InputStreamContext.Builder.newBuilder()
          .setAddress(UUID.randomUUID().toString())
          .setName(name)
          .build();
      stream = new DefaultInputStream(vertx, context, acker);
      streams.put(name, stream);
    }
    return stream;
  }

  @Override
  public void update(InputContext update) {
    Iterator<InputStream> iter = streams.values().iterator();
    while (iter.hasNext()) {
      InputStream stream = iter.next();
      boolean exists = false;
      for (InputStreamContext input : update.streams()) {
        if (input.equals(stream.context())) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        stream.close();
        iter.remove();
      }
    }

    for (InputStreamContext input : update.streams()) {
      boolean exists = false;
      for (InputStream stream : streams.values()) {
        if (stream.context().equals(input)) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        streams.put(input.name(), new DefaultInputStream(vertx, input, acker).open());
      }
    }
  }

  @Override
  public InputCollector open() {
    return open(null);
  }

  @Override
  public InputCollector open(final Handler<AsyncResult<Void>> doneHandler) {
    if (streams.isEmpty()) {
      final CountingCompletionHandler<Void> startCounter = new CountingCompletionHandler<Void>(context.streams().size());
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

      for (InputStreamContext stream : context.streams()) {
        streams.put(stream.name(), new DefaultInputStream(vertx, stream, acker).open(new Handler<AsyncResult<Void>>() {
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

    for (InputStream stream : streams.values()) {
      stream.close(new Handler<AsyncResult<Void>>() {
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
