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
import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.component.ComponentCoordinator;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.impl.DefaultInstanceContext;
import net.kuujo.vertigo.data.MapEvent;
import net.kuujo.vertigo.data.WatchableAsyncMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Default coordinator implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultComponentCoordinator implements ComponentCoordinator {
  private final String address;
  private final WatchableAsyncMap<String, String> data;
  private InstanceContext currentContext;
  private Handler<Void> resumeHandler;
  private Handler<Void> pauseHandler;
  private boolean paused = true;

  private final Handler<MapEvent<String, String>> instanceHandler = new Handler<MapEvent<String, String>>() {
    @Override
    public void handle(MapEvent<String, String> event) {
      if (currentContext != null) {
        currentContext.notify(DefaultInstanceContext.fromJson(new JsonObject(event.value())));
      }
    }
  };

  private final Handler<MapEvent<String, String>> statusHandler = new Handler<MapEvent<String, String>>() {
    @Override
    public void handle(MapEvent<String, String> event) {
      if (event.type().equals(MapEvent.Type.CREATE)) {
        paused = false;
        checkResume();
      }
      if (event.type().equals(MapEvent.Type.DELETE)) {
        paused = true;
        checkPause();
      }
    }
  };

  public DefaultComponentCoordinator(String network, String address, VertigoCluster cluster) {
    this.address = address;
    this.data = cluster.getMap(network);
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public ComponentCoordinator start(final Handler<AsyncResult<InstanceContext>> doneHandler) {
    data.get(address, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
        } else {
          currentContext = DefaultInstanceContext.fromJson(new JsonObject(result.result()));
          data.watch(address, instanceHandler, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
              } else {
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
    if (currentContext != null) {
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
    if (currentContext != null) {
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
    if (currentContext != null) {
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

}
