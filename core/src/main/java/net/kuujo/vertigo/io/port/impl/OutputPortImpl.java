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
package net.kuujo.vertigo.io.port.impl;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import net.kuujo.vertigo.io.ControllableOutput;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.io.stream.OutputStream;
import net.kuujo.vertigo.io.stream.OutputStreamInfo;
import net.kuujo.vertigo.io.stream.impl.OutputStreamImpl;
import net.kuujo.vertigo.util.Args;
import net.kuujo.vertigo.util.CountingCompletionHandler;
import net.kuujo.vertigo.util.TaskRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Output port implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputPortImpl<T> implements OutputPort<T>, ControllableOutput<OutputPort<T>, T> {
  private static final Logger log = LoggerFactory.getLogger(OutputPortImpl.class);
  private static final int DEFAULT_SEND_QUEUE_MAX_SIZE = 10000;
  private final Vertx vertx;
  private OutputPortInfo info;
  private final List<OutputStream<T>> streams = new ArrayList<>();
  private final TaskRunner tasks = new TaskRunner();
  private int maxQueueSize = DEFAULT_SEND_QUEUE_MAX_SIZE;
  private Handler<Void> drainHandler;
  private boolean open;

  public OutputPortImpl(Vertx vertx, OutputPortInfo info) {
    this.vertx = vertx;
    this.info = info;
  }

  @Override
  public String name() {
    return info.name();
  }

  @Override
  public OutputPort<T> setSendQueueMaxSize(int maxSize) {
    Args.checkPositive(maxSize, "max size must be a positive number");
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
  public int size() {
    int highest = 0;
    for (OutputStream stream : streams) {
      highest = Math.max(highest, stream.size());
    }
    return highest;
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
  public OutputPort<T> drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    for (OutputStream stream : streams) {
      stream.drainHandler(handler);
    }
    return this;
  }

  @Override
  public OutputPort<T> open() {
    return open(null);
  }

  @Override
  public OutputPort<T> open(final Handler<AsyncResult<Void>> doneHandler) {
    // Prevent the object from being opened and closed simultaneously
    // by queueing open/close operations as tasks.
    tasks.runTask((task) -> {
      if (!open) {
        streams.clear();
        open = true;
        final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(info.streams().size());
        counter.setHandler(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (doneHandler != null) {
              doneHandler.handle(result);
            }
            task.complete();
          }
        });

        // Only add streams to the stream list once the stream has been
        // opened. This helps ensure that we don't attempt to send messages
        // on a closed stream.
        for (OutputStreamInfo output : info.streams()) {
          final OutputStream<T> stream = new OutputStreamImpl<>(vertx, output);
          stream.setSendQueueMaxSize(maxQueueSize);
          stream.drainHandler(drainHandler);
          stream.open((result) -> {
            if (result.failed()) {
              log.error(String.format("%s - Failed to open output stream: %s", OutputPortImpl.this, stream));
              counter.fail(result.cause());
            } else {
              log.info(String.format("%s - Opened output stream: %s", OutputPortImpl.this, stream));
              streams.add(stream);
              counter.succeed();
            }
          });
        }
      } else {
        doneHandler.handle(Future.completedFuture());
        task.complete();
      }
    });
    return this;
  }

  @Override
  public void close() {
    close(null);
  }

  @Override
  public void close(final Handler<AsyncResult<Void>> doneHandler) {
    // Prevent the object from being opened and closed simultaneously
    // by queueing open/close operations as tasks.
    tasks.runTask((task) -> {
      if (open) {
        List<OutputStream> streams = new ArrayList<>(OutputPortImpl.this.streams);
        OutputPortImpl.this.streams.clear();
        open = false;
        final CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(streams.size());
        counter.setHandler(new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (doneHandler != null) {
              doneHandler.handle(result);
            }
            task.complete();
          }
        });
        for (final OutputStream stream : streams) {
          stream.close(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                log.warn(String.format("%s - Failed to close output stream: %s", OutputPortImpl.this, stream));
                counter.fail(result.cause());
              } else {
                log.info(String.format("%s - Closed output stream: %s", OutputPortImpl.this, stream));
                counter.succeed();
              }
            }
          });
        }
      } else {
        doneHandler.handle(Future.completedFuture());
        task.complete();
      }
    });
  }

  @Override
  public OutputPort<T> send(T message) {
    for (OutputStream<T> stream : streams) {
      stream.send(message);
    }
    return this;
  }

  @Override
  public OutputPort<T> send(T message, MultiMap headers) {
    for (OutputStream<T> stream : streams) {
      stream.send(message, headers);
    }
    return this;
  }

  @Override
  public String toString() {
    return info.toString();
  }

}
