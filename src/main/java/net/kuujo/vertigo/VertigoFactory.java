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
package net.kuujo.vertigo;

import net.kuujo.vertigo.component.Component;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * A {@link Vertigo} instance factory.
 *
 * @author Jordan Halterman
 */
public interface VertigoFactory {

  /**
   * Sets the factory Vertx instance.
   *
   * @param vertx
   *   A Vert.x instance.
   * @return
   *   The called factory instance.
   */
  VertigoFactory setVertx(Vertx vertx);

  /**
   * Sets the factory Container instance.
   *
   * @param container
   *   A container instance.
   * @return
   *   The called factory instance.
   */
  VertigoFactory setContainer(Container container);

  /**
   * Creates an empty Vertigo instance.
   *
   * @return
   *   The empty Vertigo instance.
   */
  @SuppressWarnings("rawtypes")
  Vertigo createVertigo();

  /**
   * Creates a Vertigo instance.
   *
   * @param component
   *   The current component instance.
   * @return
   *   A Vertigo instance which contains the current component instance.
   */
  <T extends Component<T>> Vertigo<T> createVertigo(T component);

}
