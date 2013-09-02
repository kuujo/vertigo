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
package com.blankstyle.vine.messaging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Publishes messages to reliable channels, recursively awaiting responses
 * before sending the next message.
 *
 * @author Jordan Halterman
 */
public class RecursiveChannelPublisher implements ChannelPublisher<ReliableChannel<?>> {

  private List<ReliableChannel<?>> channels = new ArrayList<ReliableChannel<?>>();

  @Override
  public ChannelPublisher<ReliableChannel<?>> addChannel(ReliableChannel<?> channel) {
    if (!channels.contains(channel)) {
      channels.add(channel);
    }
    return this;
  }

  @Override
  public ChannelPublisher<ReliableChannel<?>> removeChannel(ReliableChannel<?> channel) {
    if (channels.contains(channel)) {
      channels.remove(channel);
    }
    return this;
  }

  @Override
  public void publish(JsonMessage message) {
    Iterator<ReliableChannel<?>> iter = channels.iterator();
    if (iter.hasNext()) {
      recursivePublish(message, iter.next(), iter);
    }
  }

  private void recursivePublish(final JsonMessage message, ReliableChannel<?> currentChannel, final Iterator<ReliableChannel<?>> iterator) {
    currentChannel.publish(message, new AsyncResultHandler<Void>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (iterator.hasNext()) {
          recursivePublish(message, iterator.next(), iterator);
        }
      }
    });
  }

  @Override
  public void publish(JsonMessage message, Handler<AsyncResult<Void>> doneHandler) {
    Iterator<ReliableChannel<?>> iter = channels.iterator();
    if (iter.hasNext()) {
      recursivePublish(message, iter.next(), iter, doneHandler);
    }
  }

  private void recursivePublish(final JsonMessage message, ReliableChannel<?> currentChannel, final Iterator<ReliableChannel<?>> iterator, final Handler<AsyncResult<Void>> doneHandler) {
    currentChannel.publish(message, new AsyncResultHandler<Void>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          if (iterator.hasNext()) {
            recursivePublish(message, iterator.next(), iterator, doneHandler);
          }
          else {
            new DefaultFutureResult<Void>().setHandler(doneHandler).setResult(null);
          }
        }
        else {
          new DefaultFutureResult<Void>().setHandler(doneHandler).setFailure(result.cause());
        }
      }
    });
  }

}
