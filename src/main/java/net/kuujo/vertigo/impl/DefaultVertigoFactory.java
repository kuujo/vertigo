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
package net.kuujo.vertigo.impl;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.VertigoFactory;
import net.kuujo.vertigo.component.Component;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * A default Vertigo factory implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultVertigoFactory implements VertigoFactory {
  private Vertx vertx;
  private Container container;

  public DefaultVertigoFactory() {
  }

  public DefaultVertigoFactory(Vertx vertx, Container container) {
    setVertx(vertx);
    setContainer(container);
  }

  @Override
  public VertigoFactory setVertx(Vertx vertx) {
    this.vertx = vertx;
    return this;
  }

  @Override
  public VertigoFactory setContainer(Container container) {
    this.container = container;
    return this;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Vertigo createVertigo() {
    return new DefaultVertigo(vertx, container);
  }

  @Override
  public <T extends Component<T>> Vertigo<T> createVertigo(T component) {
    return new DefaultVertigo<T>(vertx, container, component);
  }

}
