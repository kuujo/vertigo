/*
* Copyright 2013 the original author or authors.
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
package net.kuujo.vertigo.node.feeder;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.node.FailureException;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * A default feed queue implementation.
 *
 * @author Jordan Halterman
 */
public class BasicFeedQueue implements FeedQueue {

  private Map<String, Future<Void>> futures = new HashMap<String, Future<Void>>();

  private long maxQueueSize = 1000;

  @Override
  public FeedQueue maxQueueSize(long maxSize) {
    maxQueueSize = maxSize;
    return this;
  }

  @Override
  public long maxQueueSize() {
    return maxQueueSize;
  }

  @Override
  public long size() {
    return futures.size();
  }

  @Override
  public boolean full() {
    return size() >= maxQueueSize;
  }

  @Override
  public FeedQueue enqueue(final String id, Handler<AsyncResult<Void>> ackHandler) {
    Future<Void> future = new DefaultFutureResult<Void>().setHandler(ackHandler);
    futures.put(id, future);
    return this;
  }

  @Override
  public void ack(String id) {
    if (futures.containsKey(id)) {
      futures.remove(id).setResult(null);
    }
  }

  @Override
  public void fail(String id) {
    if (futures.containsKey(id)) {
      futures.remove(id).setFailure(new FailureException(""));
    }
  }

}
