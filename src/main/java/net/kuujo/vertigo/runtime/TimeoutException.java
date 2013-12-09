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
 * A processing timeout exception.<p>
 *
 * This exception will be the cause of the failure to process a message if
 * the root message or one of its descendants timed out. This can be checked
 * in feeder or executor verticles by using <code>instanceof</code> on the
 * <code>AsyncResult</code> cause.<p>
 *
 * <pre>
 * feeder.emit(new JsonObject().putString("foo", "bar"), new Handler<AsyncResult<MessageId>>() {
 *   public void handle(AsyncResult<MessageId> result) {
 *     if (result.failed() && result.cause() instanceof TimeoutException) {
 *       // The message timed out.
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * For explicit failures, see {@link FailureException}
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("serial")
public class TimeoutException extends VertigoException {

  public TimeoutException(String message) {
    super(message);
  }

  public TimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public TimeoutException(Throwable cause) {
    super(cause);
  }

}
