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
package net.kuujo.vertigo.cluster.data.impl;

import net.kuujo.vertigo.cluster.impl.ClusterLocator;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Base asynchronous data structure.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
class AsyncDataStructure {
  protected final String address;
  protected final String name;
  protected final Vertx vertx;
  private final ClusterLocator clusterLocator;
  private String localAddress;
  private long lastReset;
  private long check;

  public AsyncDataStructure(String address, String name, Vertx vertx) {
    this.address = address;
    this.name = name;
    this.vertx = vertx;
    this.clusterLocator = new ClusterLocator(vertx);
  }

  public String name() {
    return name;
  }

  /**
   * Checks whether the local cluster address needs to be updated.
   */
  protected void checkAddress() {
    if (localAddress == null) {
      check++;
      if (check > 1000 && System.currentTimeMillis() - lastReset > 15000) {
        resetLocalAddress(null);
      }
    }
  }

  /**
   * Updates the known local group address.
   */
  protected void resetLocalAddress(final Handler<AsyncResult<Boolean>> doneHandler) {
    localAddress = null;
    lastReset = System.currentTimeMillis();
    clusterLocator.locateCluster(address, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded() && result.result() != null) {
          localAddress = result.result();
          new DefaultFutureResult<Boolean>(true).setHandler(doneHandler);
        } else {
          new DefaultFutureResult<Boolean>(false).setHandler(doneHandler);
        }
      }
    });
  }

  /**
   * Returns the local address if available, otherwise a group address.
   */
  protected final String localOrGroupAddress() {
    return localAddress != null ? localAddress : address;
  }

}
