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
package net.kuujo.vertigo.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.VertigoOptions;
import net.kuujo.vertigo.spi.VertigoFactory;

/**
 * Vertigo factory implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class VertigoFactoryImpl implements VertigoFactory {

  @Override
  public Vertigo vertigo() {
    return vertigo(new VertigoOptions());
  }

  @Override
  public Vertigo vertigo(VertigoOptions options) {
    return vertigo(Vertx.vertx(options), options);
  }

  @Override
  public Vertigo vertigo(Vertx vertx) {
    return vertigo(vertx, new VertigoOptions());
  }

  @Override
  public Vertigo vertigo(Vertx vertx, VertigoOptions options) {
    return new VertigoImpl(vertx, options);
  }

  @Override
  public void vertigoAsync(VertigoOptions options, Handler<AsyncResult<Vertigo>> resultHandler) {
    Vertx.vertxAsync(options, result -> {
      if (result.failed()) {
        Future.<Vertigo>completedFuture(result.cause()).setHandler(resultHandler);
      } else {
        Future.<Vertigo>completedFuture(new VertigoImpl(result.result(), options)).setHandler(resultHandler);
      }
    });
  }

}
