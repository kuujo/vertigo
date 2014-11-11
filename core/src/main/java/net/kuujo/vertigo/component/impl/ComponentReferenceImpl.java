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
package net.kuujo.vertigo.component.impl;

import io.vertx.core.Vertx;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.ComponentReference;
import net.kuujo.vertigo.component.PartitionReference;
import net.kuujo.vertigo.io.InputReference;
import net.kuujo.vertigo.io.OutputReference;
import net.kuujo.vertigo.io.impl.InputReferenceImpl;
import net.kuujo.vertigo.io.impl.OutputReferenceImpl;

/**
 * Component reference implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentReferenceImpl implements ComponentReference {
  private final Vertx vertx;
  private final ComponentContext context;

  public ComponentReferenceImpl(Vertx vertx, ComponentContext context) {
    this.vertx = vertx;
    this.context = context;
  }

  @Override
  public PartitionReference partition(int partition) {
    return new PartitionReferenceImpl(vertx, context.partition(partition));
  }

  @Override
  public InputReference input() {
    return new InputReferenceImpl(vertx, context.address());
  }

  @Override
  public OutputReference output() {
    return new OutputReferenceImpl(vertx, context.address());
  }

}
