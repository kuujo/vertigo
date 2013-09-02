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
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * A default channel publisher.
 *
 * @author Jordan Halterman
 */
public class DefaultChannelPublisher implements ChannelPublisher<Channel<?>> {

  private List<Channel<?>> channels = new ArrayList<Channel<?>>();

  @Override
  public ChannelPublisher<Channel<?>> addChannel(Channel<?> channel) {
    if (!channels.contains(channel)) {
      channels.add(channel);
    }
    return this;
  }

  @Override
  public ChannelPublisher<Channel<?>> removeChannel(Channel<?> channel) {
    if (channels.contains(channel)) {
      channels.remove(channel);
    }
    return this;
  }

  @Override
  public void publish(JsonMessage message) {
    Iterator<Channel<?>> iter = channels.iterator();
    while (iter.hasNext()) {
      iter.next().publish(message);
    }
  }

  @Override
  public void publish(JsonMessage message, Handler<AsyncResult<Void>> doneHandler) {
    publish(message);
    new DefaultFutureResult<Void>().setHandler(doneHandler).setResult(null);
  }

}
