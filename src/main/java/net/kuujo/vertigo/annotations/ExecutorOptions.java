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
package net.kuujo.vertigo.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Options for Java executor verticles.<p>
 *
 * Use this annotation on any executor verticle implementation. Vertigo will
 * automatically handle setup of the internal executor prior to starting it.<p>
 *
 * <pre>
 * @ExecutorOptions(autoRetry=true)
 * public class MyExecutor extends ExecutorVerticle {
 *   public void start(Executor executor) {
 *     executor.emit(new JsonObject().putString("foo", "bar"), new Handler<AsyncResult<JsonMessage>>() {
 *       public void handle(AsyncResult<JsonMessage> result) {
 *         ...
 *       }
 *     });
 *   }
 * }
 * </pre>
 *
 * @author Jordan Halterman
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutorOptions {

  /**
   * Indicates the execution result timeout in milliseconds. Defaults to 30000 milliseconds
   */
  long resultTimeout() default 30000;

  /**
   * Indicates the execute queue max size. Defaults to 1000
   */
  long executeQueueMaxSize() default 1000;

  /**
   * Indicates whether to automatically retry sending timed out messages.
   * Defaults to false
   */
  boolean autoRetry() default false;

  /**
   * Indicates the number of automatic retry attempts. Defaults to unlimited
   */
  int autoRetryAttempts() default -1;

  /**
   * Indicates the interval at which the execute handler will be called. Defaults
   * to 10 milliseconds
   */
  long executeInterval() default 10;

}
