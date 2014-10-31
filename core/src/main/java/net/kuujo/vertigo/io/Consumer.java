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

package net.kuujo.vertigo.io;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

/**
 * Vertigo port consumer.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface Consumer<T> extends ReadStream<VertigoMessage<T>> {

  @Override
  Consumer<T> exceptionHandler(Handler<Throwable> handler);

  @Override
  Consumer<T> handler(Handler<VertigoMessage<T>> handler);

  @Override
  Consumer<T> pause();

  @Override
  Consumer<T> resume();

  @Override
  Consumer<T> endHandler(Handler<Void> handler);

  /**
   * Returns the name of the port to which the consumer belongs.
   *
   * @return The name of the port to which the consumer belongs.
   */
  String port();

  /**
   * Returns a read stream for messages received on the port.
   *
   * @return A port message read stream.
   */
  ReadStream<T> bodyStream();

}
