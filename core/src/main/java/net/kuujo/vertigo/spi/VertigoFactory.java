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
package net.kuujo.vertigo.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import net.kuujo.vertigo.Vertigo;

/**
 * Vertigo factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface VertigoFactory {

  /**
   * Creates a new Vertigo instance.
   *
   * @return The new Vertigo instance.
   */
  Vertigo vertigo();

  /**
   * Creates a new Vertigo instance.
   *
   * @param options The Vert.x options.
   * @return The Vertigo instance.
   */
  Vertigo vertigo(VertxOptions options);

  /**
   * Creates a new Vertigo instance.
   *
   * @param vertx The underlying Vert.x instance.
   * @return The Vertigo instance.
   */
  Vertigo vertigo(Vertx vertx);

  /**
   * Asynchronously creates a new Vertigo instance.
   *
   * @param options The Vert.x options.
   * @param resultHandler An asynchronous handler to be called once complete.
   */
  void vertigoAsync(VertxOptions options, Handler<AsyncResult<Vertigo>> resultHandler);

}
