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
 * Static {@link Vertigo} factory methods intended for use in language bindings.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class Factory {

  /**
   * Builds a Vertigo instance.
   *
   * @param vertx
   *   A Java Vertx instance.
   * @param container
   *   A Java Container instance.
   * @return
   *   A Vertigo instance. If the current Vert.x verticle is a Vertigo component
   *   then the Vertigo instance will contain the component and context.
   */
  public static Vertigo createVertigo(Vertx vertx, Container container) {
    VertigoFactory vertigoFactory = new DefaultVertigoFactory(vertx, container);
    InstanceContext context = Context.parseContext(container.config());
    if (context != null) {
      ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
      net.kuujo.vertigo.component.Component component = componentFactory.createComponent(context);
      return vertigoFactory.createVertigo(component);
    }
    else {
      return vertigoFactory.createVertigo();
    }
  }

}
