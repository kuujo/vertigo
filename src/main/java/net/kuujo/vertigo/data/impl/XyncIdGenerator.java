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
package net.kuujo.vertigo.data.impl;

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.data.AsyncIdGenerator;
import net.kuujo.xync.data.impl.XyncAsyncIdGenerator;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * An event bus ID generator implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@ClusterType
public class XyncIdGenerator implements AsyncIdGenerator {
  private final net.kuujo.xync.data.AsyncIdGenerator generator;

  @Factory
  public static XyncIdGenerator factory(String name, Vertx vertx) {
    return new XyncIdGenerator(new XyncAsyncIdGenerator(name, vertx.eventBus()));
  }

  private XyncIdGenerator(net.kuujo.xync.data.AsyncIdGenerator generator) {
    this.generator = generator;
  }

  @Override
  public String name() {
    return generator.name();
  }

  @Override
  public void nextId(final Handler<AsyncResult<Long>> resultHandler) {
    generator.nextId(resultHandler);
  }

}
