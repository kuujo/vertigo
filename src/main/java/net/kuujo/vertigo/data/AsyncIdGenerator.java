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
package net.kuujo.vertigo.data;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Asynchronous globally unique ID generator.
 *
 * @author Jordan Halterman
 */
public interface AsyncIdGenerator {

  /**
   * Returns the ID generator name.
   *
   * @return The unique ID generator name.
   */
  String name();

  /**
   * Gets the next unique ID from the generator.
   *
   * @param resultHandler An asynchronous handler to be called with the next unique ID.
   */
  void nextId(Handler<AsyncResult<Long>> resultHandler);

}
