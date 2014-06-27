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
package net.kuujo.vertigo.cluster.manager.impl;

import java.util.UUID;

import org.vertx.java.core.Handler;

/**
 * Fake cluster listener for non-clustered Vert.x instances.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
class NoClusterListener implements ClusterListener {
  private final String id = UUID.randomUUID().toString();

  @Override
  public String nodeId() {
    return id;
  }

  @Override
  public void registerJoinHandler(Handler<String> handler) {
    // Do nothing useful.
  }

  @Override
  public void unregisterJoinHandler(Handler<String> handler) {
    // Do nothing useful.
  }

  @Override
  public void registerLeaveHandler(Handler<String> handler) {
    // Do nothing useful.
  }

  @Override
  public void unregisterLeaveHandler(Handler<String> handler) {
    // Do nothing useful.
  }

}
