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

import io.vertx.core.Vertx;
import net.kuujo.vertigo.io.InputReference;
import net.kuujo.vertigo.io.port.InputPortReference;
import net.kuujo.vertigo.io.port.impl.InputPortReferenceImpl;

/**
 * Input reference implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputReferenceImpl implements InputReference {
  private final Vertx vertx;
  private final String address;

  public InputReferenceImpl(Vertx vertx, String address) {
    this.vertx = vertx;
    this.address = address;
  }

  @Override
  public <T> InputPortReference<T> port(String name) {
    return new InputPortReferenceImpl<>(vertx, address, name);
  }

}
