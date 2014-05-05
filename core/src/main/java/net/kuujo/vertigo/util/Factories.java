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
package net.kuujo.vertigo.util;

import static net.kuujo.vertigo.util.Config.parseCluster;
import static net.kuujo.vertigo.util.Config.parseContext;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.component.impl.DefaultComponentFactory;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

/**
 * Factory utility.<p>
 *
 * This helper class should be used with the {@link net.kuujo.vertigo.util.Factory}
 * annotation to construct types from annotated factory methods. Specifically,
 * when constructing types that are specific to different cluster modes, the factory
 * utility will automatically fall back to the appropriate default local- or cluster-
 * type according to the current Vertigo cluster mode.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Factories {

  /**
   * Creates a component instance for the current Vert.x instance.
   */
  public static Component createComponent(Vertx vertx, Container container) {
    return new DefaultComponentFactory().setVertx(vertx).setContainer(container).createComponent(parseContext(container.config()), parseCluster(container.config(), vertx, container));
  }

}
