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

package net.kuujo.vertigo.io.impl;

import io.vertx.core.MultiMap;
import net.kuujo.vertigo.io.VertigoMessage;

/**
 * Vertigo message implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class VertigoMessageImpl<T> implements VertigoMessage<T> {
  private String id;
  private T body;
  private MultiMap headers;

  public VertigoMessageImpl() {
  }

  public VertigoMessageImpl(String id, T body, MultiMap headers) {
    this.id = id;
    this.body = body;
    this.headers = headers;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public T body() {
    return body;
  }

  @Override
  public MultiMap headers() {
    return headers;
  }

  @Override
  public void ack() {
  }

  @Override
  public void fail() {
  }

}
