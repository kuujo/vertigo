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
package net.kuujo.vertigo.runtime;

import net.kuujo.vertigo.VertigoException;


/**
 * A processing failure exception.<p>
 *
 * This exception will be the cause of the failure of a message to process
 * if the root message or any of its descendants was explicitly failed by
 * a worker verticle. This is usually used to indicate a malformed message
 * or otherwise invalid message data. The failure can be checked in feeder
 * or executor verticles by using <code>instanceof</code> on the
 * <code>AsyncResult</code> cause.<p>
 *
 * <pre>
 * feeder.emit(new JsonObject().putString("foo", "bar"), new Handler<AsyncResult<MessageId>>() {
 *   public void handle(AsyncResult<MessageId> result) {
 *     if (result.failed() && result.cause() instanceof FailureException) {
 *       // The message was explicitly failed.
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * For timeout errors, see {@link TimeoutException}
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("serial")
public class FailureException extends VertigoException {

  public FailureException(String message) {
    super(message);
  }

  public FailureException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailureException(Throwable cause) {
    super(cause);
  }

}

