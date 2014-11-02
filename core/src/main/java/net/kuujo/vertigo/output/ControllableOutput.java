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

package net.kuujo.vertigo.output;

import io.vertx.core.Handler;
import net.kuujo.vertigo.output.Output;

/**
 * Controllable output.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ControllableOutput<T extends Output<T, U>, U> extends Output<T, U> {

  /**
   * Returns the current connection send queue size.
   *
   * @return The current connection send queue size.
   */
  int size();

  /**
   * Sets the send queue max size.
   *
   * @param maxSize The send queue max size.
   * @return The send stream.
   */
  T setSendQueueMaxSize(int maxSize);

  /**
   * Returns the send queue max size.
   *
   * @return The send queue max size.
   */
  int getSendQueueMaxSize();

  /**
   * Returns a boolean indicating whether the send queue is full.
   *
   * @return Indicates whether the send queue is full.
   */
  boolean sendQueueFull();

  /**
   * Sets a drain handler on the output.<p>
   *
   * When the output's send queue becomes full, the output will be temporarily
   * paused while its output queue is empty. Once the queue size decreases
   * back to 50% of the maximum queue size the drain handler will be called
   * so that normal operation can resume.
   *
   * @param handler A handler to be called when the stream is prepared to accept
   *        new messages.
   * @return The send stream.
   */
  T drainHandler(Handler<Void> handler);

}
