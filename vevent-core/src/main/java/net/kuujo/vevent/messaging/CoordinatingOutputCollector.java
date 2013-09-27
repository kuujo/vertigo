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
package net.kuujo.vevent.messaging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * A reliable output collector implementation.
 *
 * @author Jordan Halterman
 */
public class CoordinatingOutputCollector implements OutputCollector {

  private Map<String, Channel<?>> channels = new HashMap<>();

  @Override
  public OutputCollector addChannel(String name, Channel<?> channel) {
    channels.put(name, channel);
    return this;
  }

  @Override
  public OutputCollector removeChannel(String name) {
    if (channels.containsKey(name)) {
      channels.remove(name);
    }
    return this;
  }

  @Override
  public Set<String> getChannelNames() {
    return channels.keySet();
  }

  @Override
  public Channel<?> getChannel(String name) {
    return channels.get(name);
  }

  @Override
  public int size() {
    return channels.size();
  }

  @Override
  public OutputCollector emit(JsonMessage message) {
    emit(message, 0, null);
    return this;
  }

  @Override
  public OutputCollector emit(JsonMessage message,
      Handler<AsyncResult<Boolean>> ackHandler) {
    emit(message, 0, ackHandler);
    return this;
  }

  @Override
  public OutputCollector emit(JsonMessage message, long timeout,
      Handler<AsyncResult<Boolean>> ackHandler) {
    new RecursiveEmission(channels.keySet(), toSet(message), timeout, createFuture(ackHandler)).execute();
    return this;
  }

  @Override
  public OutputCollector emit(JsonMessage[] messages) {
    emit(messages, 0, null);
    return this;
  }

  @Override
  public OutputCollector emit(JsonMessage[] messages,
      Handler<AsyncResult<Boolean>> ackHandler) {
    emit(messages, 0, ackHandler);
    return this;
  }

  @Override
  public OutputCollector emit(JsonMessage[] messages, long timeout,
      Handler<AsyncResult<Boolean>> ackHandler) {
    new RecursiveEmission(channels.keySet(), toSet(messages), timeout, createFuture(ackHandler)).execute();
    return this;
  }

  private Set<JsonMessage> toSet(JsonMessage[] messages) {
    Set<JsonMessage> messageSet = new HashSet<JsonMessage>();
    for (JsonMessage message : messages) {
      messageSet.add(message);
    }
    return messageSet;
  }

  private Set<JsonMessage> toSet(JsonMessage message) {
    Set<JsonMessage> messageSet = new HashSet<JsonMessage>();
    messageSet.add(message);
    return messageSet;
  }

  /**
   * Creates a future result handler.
   */
  private Future<Boolean> createFuture(Handler<AsyncResult<Boolean>> handler) {
    return new DefaultFutureResult<Boolean>().setHandler(handler);
  }

  /**
   * A recursive message emission. The recursive emission emits and tracks
   * multiple messages for acking.
   */
  private class RecursiveEmission {
    private static final boolean CHANNEL_RETRY = false;
    private static final int CHANNEL_ATTEMPTS = 0;
    private Set<String> channelNames;
    private Set<JsonMessage> messages;
    private long timeout;
    private Future<Boolean> future;
    private Map<JsonMessage, Set<String>> completedChannels = new HashMap<JsonMessage, Set<String>>();
    private Set<JsonMessage> completedMessages = new HashSet<JsonMessage>();
    private boolean finished;

    public RecursiveEmission(Set<String> channelNames, Set<JsonMessage> messages, long timeout, Future<Boolean> future) {
      this.channelNames = channelNames;
      this.messages = messages;
      this.timeout = timeout;
      this.future = future;
    }

    /**
     * Executes the emitter.
     */
    public void execute() {
      if (!finished) {
        for (JsonMessage message : messages) {
          doEmit(message);
        }
      }
    }

    /**
     * Emits a single message to all output channels.
     */
    private void doEmit(JsonMessage message) {
      if (!finished) {
        for (String channelName : channelNames) {
          doEmitTo(channelName, message);
        }
      }
    }

    /**
     * Emits a single message to a single output channel.
     */
    private void doEmitTo(final String channelName, final JsonMessage message) {
      // By emitting all messages with the same timeout, we know that if
      // an eventbus timeout occurs then the emission failed.
      channels.get(channelName).write(message, timeout, CHANNEL_RETRY, CHANNEL_ATTEMPTS, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (!finished) {
            // If the send itself failed then fail the future.
            if (result.failed()) {
              future.setFailure(result.cause());
            }
            // Otherwise, check the result.
            else {
              // If the message was acked then add this channel to the list
              // of successfully acked channels.
              boolean succeeded = result.result();
              if (succeeded) {
                Set<String> channelList;
                if (completedChannels.containsKey(message)) {
                  channelList = completedChannels.get(message);
                }
                else {
                  channelList = new HashSet<String>();
                  completedChannels.put(message, channelList);
                }

                // If the set of completed channels matches the set of all channels
                // to which we are emitting messages, this message is complete.
                channelList.add(channelName);
                if (channelList.equals(channelNames)) {
                  completedMessages.add(message);
                  // If the set of completed messages matches all emitter messages
                  // then the entire emission is complete.
                  if (completedMessages.equals(messages)) {
                    finished = true;
                    future.setResult(true);
                  }
                }
              }
              // If a message was failed, immediately fail the tree. Once a tree
              // has been failed no more messages will be checked.
              else {
                finished = true;
                future.setResult(false);
              }
            }
          }
        }
      });
    }
  }

}
