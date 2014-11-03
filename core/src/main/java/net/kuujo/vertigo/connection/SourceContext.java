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
package net.kuujo.vertigo.connection;

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.connection.impl.SourceContextImpl;

/**
 * Connection source info.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface SourceContext extends EndpointContext<SourceContext> {

  /**
   * Returns a new source info builder.
   *
   * @return A new source info builder.
   */
  static Builder builder() {
    return new SourceContextImpl.Builder();
  }

  /**
   * Returns a new source info builder.
   *
   * @param source The source info to wrap.
   * @return The source info builder wrapper.
   */
  static Builder builder(SourceContext source) {
    return new SourceContextImpl.Builder((SourceContextImpl) source);
  }

  /**
   * Source info builder.
   */
  public static interface Builder extends Context.Builder<SourceContext> {

    /**
     * Sets the source component.
     *
     * @param component The source component name.
     * @return The source info builder.
     */
    Builder setComponent(String component);

    /**
     * Sets the source port.
     *
     * @param port The source port name.
     * @return The source info builder.
     */
    Builder setPort(String port);

    /**
     * Sets the source partition.
     *
     * @param partition The source partition number.
     * @return The source info builder.
     */
    Builder setPartition(int partition);
  }

}
