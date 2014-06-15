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
package net.kuujo.vertigo.component.impl;

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.data.MapEvent;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;
import net.kuujo.vertigo.cluster.data.impl.WrappedWatchableAsyncMap;
import net.kuujo.vertigo.component.ComponentCoordinator;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.util.Contexts;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Default coordinator implementation.<p>
 *
 * This coordinator implementation uses the current Vertigo cluster
 * facility to coordinate with the network manager using event-based
 * cluster-wide shared data keys.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultComponentCoordinator implements ComponentCoordinator {
  private final Logger log;
  private final String address;
  private final Vertx vertx;
  private Cluster cluster;
  private WatchableAsyncMap<String, String> data;
  private InstanceContext currentContext;
  private Handler<Void> resumeHandler;
  private Handler<Void> pauseHandler;
  private boolean paused = true;

  private final Handler<MapEvent<String, String>> instanceHandler = new Handler<MapEvent<String, String>>() {
    @Override
    public void handle(MapEvent<String, String> event) {
      if (currentContext != null && !event.type().equals(MapEvent.Type.CHANGE)) {
        log.info(String.format("%s - Configuration change detected, updating context", DefaultComponentCoordinator.this));
        currentContext.notify(Contexts.<InstanceContext>deserialize(new JsonObject(event.value())));
        resume();
      }
    }
  };

  private final Handler<MapEvent<String, String>> statusHandler = new Handler<MapEvent<String, String>>() {
    @Override
    public void handle(MapEvent<String, String> event) {
      if (event.type().equals(MapEvent.Type.CREATE) || event.type().equals(MapEvent.Type.UPDATE)) {
        log.debug(String.format("%s - Resumed", DefaultComponentCoordinator.this));
        paused = false;
        checkResume();
      } else if (event.type().equals(MapEvent.Type.DELETE)) {
        log.debug(String.format("%s - Paused", DefaultComponentCoordinator.this));
        paused = true;
        checkPause();
      }
    }
  };

  public DefaultComponentCoordinator(InstanceContext context, Vertx vertx, Cluster cluster) {
    this.address = context.address();
    this.log = LoggerFactory.getLogger(String.format("%s-%s", DefaultComponentCoordinator.class.getName(), address));
    this.vertx = vertx;
    this.currentContext = context;
    this.cluster = cluster;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public ComponentCoordinator start(final Handler<AsyncResult<InstanceContext>> doneHandler) {
    data = new WrappedWatchableAsyncMap<String, String>(cluster.<String, String>getMap(currentContext.component().network().address()), vertx);

    // Start watching the component's context. It's important that this
    // happens in a certain order in order to prevent race conditions. First
    // we watch the component's configuration, then we attempt to load the
    // existing component configuration. Otherwise, the component's configuration
    // could potentially be set between get() and watch().
    log.debug(String.format("%s - start() watching key at %s", this, address));
    data.watch(address, instanceHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
        } else {
          data.get(address, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
              if (result.failed()) {
                new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
              } else {
                if (result.result() != null) {
                  currentContext.notify(Contexts.<InstanceContext>deserialize(new JsonObject(result.result())));
                }
                log.debug(String.format("%s - start() watching status key at %s", DefaultComponentCoordinator.this, currentContext.component().network().status()));
                data.watch(currentContext.component().network().status(), statusHandler, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
                    } else {
                      new DefaultFutureResult<InstanceContext>(currentContext).setHandler(doneHandler);
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
    return this;
  }

  @Override
  public ComponentCoordinator resume() {
    return resume(null);
  }

  @Override
  public ComponentCoordinator resume(final Handler<AsyncResult<Void>> doneHandler) {
    if (currentContext != null && data != null) {
      // Set the status key to "ready" to indicate to the network that the
      // component is ready to start - all its connections have been opened.
      log.debug(String.format("%s - resume() setting status key at %s", DefaultComponentCoordinator.this, currentContext.status()));
      data.put(currentContext.status(), "ready", new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });
    }
    else {
      new DefaultFutureResult<Void>(new VertigoException("No component context available.")).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public ComponentCoordinator pause() {
    return pause(null);
  }

  @Override
  public ComponentCoordinator pause(final Handler<AsyncResult<Void>> doneHandler) {
    if (currentContext != null && data != null) {
      log.debug(String.format("%s - pause() clearing status key at %s", DefaultComponentCoordinator.this, currentContext.status()));
      data.remove(currentContext.status(), new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
          }
        }
      });
    } else {
      new DefaultFutureResult<Void>(new VertigoException("No component context available.")).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public ComponentCoordinator resumeHandler(Handler<Void> handler) {
    resumeHandler = handler;
    checkResume();
    return this;
  }

  /**
   * Checks and triggers the resume handler.
   */
  private void checkResume() {
    if (!paused && resumeHandler != null) {
      resumeHandler.handle((Void) null);
    }
  }

  @Override
  public ComponentCoordinator pauseHandler(Handler<Void> handler) {
    pauseHandler = handler;
    checkPause();
    return this;
  }

  /**
   * Checks and triggers the pause handler.
   */
  private void checkPause() {
    if (paused && pauseHandler != null) {
      pauseHandler.handle((Void) null);
    }
  }

  @Override
  public void stop(final Handler<AsyncResult<Void>> doneHandler) {
    if (currentContext != null && data != null) {
      log.debug(String.format("%s - stop() unwatching status key at %s", DefaultComponentCoordinator.this, currentContext.component().network().status()));
      data.unwatch(currentContext.component().network().status(), statusHandler, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            data.unwatch(address, instanceHandler);
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            data.unwatch(address, instanceHandler, doneHandler);
          }
          currentContext = null;
        }
      });
    } else {
      data.unwatch(address, instanceHandler, doneHandler);
    }
  }

  @Override
  public String toString() {
    return String.format("Coordinator[%s]", address);
  }

}
