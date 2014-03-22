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
import net.kuujo.vertigo.cluster.ClusterClient;
import net.kuujo.vertigo.cluster.ClusterEvent;
import net.kuujo.vertigo.component.ComponentCoordinator;
import net.kuujo.vertigo.context.InstanceContext;

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
  private final ClusterClient cluster;
  private InstanceContext currentContext;
  private Handler<Void> resumeHandler;
  private Handler<Void> pauseHandler;

  private final Handler<ClusterEvent> instanceHandler = new Handler<ClusterEvent>() {
    @Override
    public void handle(ClusterEvent event) {
      if (currentContext != null) {
        currentContext.notify(InstanceContext.fromJson(new JsonObject(event.<String>value())));
      }
    }
  };

  private final Handler<ClusterEvent> statusHandler = new Handler<ClusterEvent>() {
    @Override
    public void handle(ClusterEvent event) {
      if (event.type().equals(ClusterEvent.Type.CREATE)) {
        if (resumeHandler != null) {
          resumeHandler.handle((Void) null);
        }
      }
      if (event.type().equals(ClusterEvent.Type.DELETE)) {
        if (pauseHandler != null) {
          pauseHandler.handle((Void) null);
        }
      }
    }
  };

  public DefaultComponentCoordinator(String address, ClusterClient cluster) {
    this.address = address;
    this.cluster = cluster;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public ComponentCoordinator start(final Handler<AsyncResult<InstanceContext>> doneHandler) {
    cluster.get(address, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.failed()) {
          new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
        }
        else {
          currentContext = InstanceContext.fromJson(new JsonObject(result.result()));
          cluster.watch(address, instanceHandler, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
              }
              else {
                cluster.watch(currentContext.component().network().status(), statusHandler, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<InstanceContext>(result.cause()).setHandler(doneHandler);
                    }
                    else {
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
      cluster.set(currentContext.status(), true, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          }
          else {
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
      cluster.delete(currentContext.status(), new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          }
          else {
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
  public ComponentCoordinator resumeHandler(Handler<Void> handler) {
    resumeHandler = handler;
    return this;
  }

  @Override
  public ComponentCoordinator pauseHandler(Handler<Void> handler) {
    pauseHandler = handler;
    return this;
  }

  @Override
  public void stop(final Handler<AsyncResult<Void>> doneHandler) {
    if (currentContext != null) {
      cluster.unwatch(currentContext.component().network().status(), statusHandler, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            cluster.unwatch(address, instanceHandler);
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          }
          else {
            cluster.unwatch(address, instanceHandler, doneHandler);
          }
          currentContext = null;
        }
      });
    }
    else {
      cluster.unwatch(address, instanceHandler, doneHandler);
    }
  }

}
