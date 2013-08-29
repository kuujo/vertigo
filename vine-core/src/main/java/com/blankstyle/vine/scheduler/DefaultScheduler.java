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
package com.blankstyle.vine.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

import com.blankstyle.vine.Stem;
import com.blankstyle.vine.context.WorkerContext;

/**
 * A default scheduler implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultScheduler implements Scheduler {

  @Override
  public void assign(WorkerContext context, Collection<Stem> stems, Handler<AsyncResult<String>> resultHandler) {
    final Future<String> future = new DefaultFutureResult<String>();

    Iterator<Stem> iter = stems.iterator();
    List<Stem> stemList = new ArrayList<Stem>();
    while (iter.hasNext()) {
      stemList.add(iter.next());
    }

    // Assign the worker to a random stem.
    final Stem stem = stemList.get((int) (Math.random() * stemList.size()));
    stem.assign(context, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          future.setResult(stem.getAddress());
        }
        else {
          future.setFailure(result.cause());
        }
      }
    });
  }

}
