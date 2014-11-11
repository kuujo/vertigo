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
package net.kuujo.vertigo.io.connection;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.TypeContext;
import net.kuujo.vertigo.io.connection.impl.SourceContextImpl;

/**
 * Connection source context.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface SourceContext extends EndpointContext<SourceContext> {

  /**
   * Returns a new source context builder.
   *
   * @return A new source context builder.
   */
  static Builder builder() {
    return new SourceContextImpl.Builder();
  }

  /**
   * Returns a new source context builder.
   *
   * @param source The source context to wrap.
   * @return The source context builder wrapper.
   */
  static Builder builder(SourceContext source) {
    return new SourceContextImpl.Builder((SourceContextImpl) source);
  }

  /**
   * Source context builder.
   */
  public static interface Builder extends TypeContext.Builder<Builder, SourceContext> {

    /**
     * Sets the source component.
     *
     * @param component The source component name.
     * @return The source context builder.
     */
    Builder setComponent(String component);

    /**
     * Sets the source port.
     *
     * @param port The source port name.
     * @return The source context builder.
     */
    Builder setPort(String port);

  }

}
