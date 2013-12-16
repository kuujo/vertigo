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
package net.kuujo.vertigo.util;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.VertigoFactory;
import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.impl.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.impl.DefaultVertigoFactory;

/**
 * Static Vertigo instance for language bindings.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class Static {
  private static Vertigo vertigo;

  private static void init(Vertx vertx, Container container) {
    if (vertigo == null) {
      VertigoFactory vertigoFactory = new DefaultVertigoFactory(vertx, container);
      InstanceContext context = Context.parseContext(container.config());
      if (context != null) {
        ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
        net.kuujo.vertigo.component.Component component = componentFactory.createComponent(context);
        vertigo = vertigoFactory.createVertigo(component);
      }
      else {
        vertigo = vertigoFactory.createVertigo();
      }
    }
  }

  /**
   * Gets the current Vertigo instance.
   *
   * @param vertx
   *   A Vert.x instance with which to construct the Vertigo instance if necessary.
   * @param container
   *   A Container instance with which to construct the Vertigo instance if necessary.
   * @return
   *   A Vertigo instance.
   */
  public static Vertigo vertigo(Vertx vertx, Container container) {
    init(vertx, container);
    return vertigo;
  }

  /**
   * Gets the current component type as a string.
   *
   * @return
   *   The current component type as a string, or null if the current context
   *   is not a Vertigo component.
   */
  public static String type(Vertx vertx, Container container) {
    init(vertx, container);
    InstanceContext context = context(vertx, container);
    if (context != null) {
      return Component.serializeType(context.getComponent().getType());
    }
    return null;
  }

  /**
   * Gets the current instance context.
   *
   * @return
   *   The current instance context or null if the current context is not a component.
   */
  public static InstanceContext context(Vertx vertx, Container container) {
    init(vertx, container);
    return vertigo.context();
  }

  /**
   * Gets the current component.
   *
   * @return
   *   The current verticle component or null if the current context is not a component.
   */
  public static net.kuujo.vertigo.component.Component component(Vertx vertx, Container container) {
    init(vertx, container);
    return vertigo.component();
  }

}
