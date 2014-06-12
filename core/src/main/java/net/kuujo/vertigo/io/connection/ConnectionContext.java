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

import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.io.connection.impl.DefaultConnectionContext;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Connection contexts represent a direct connection between two instances
 * of separate components.<p>
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface ConnectionContext<T extends ConnectionContext<T>> extends Context<T> {

  /**
   * Returns the connection source.
   *
   * @return The connection source.
   */
  SourceContext source();

  /**
   * Returns the connection target.
   *
   * @return The connection target.
   */
  TargetContext target();

  /**
   * Connection endpoint context.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   *
   * @param <T> The endpoint type.
   */
  @JsonTypeInfo(
    use=JsonTypeInfo.Id.NAME,
    include=JsonTypeInfo.As.PROPERTY,
    property="type"
  )
  @JsonSubTypes({
    @JsonSubTypes.Type(value=DefaultConnectionContext.DefaultSourceContext.class, name="source"),
    @JsonSubTypes.Type(value=DefaultConnectionContext.DefaultTargetContext.class, name="target")
  })
  public static interface EndpointContext<T extends EndpointContext<T>> extends Context<T> {

    /**
     * Returns the endpoint component name.
     *
     * @return The endpoint component name.
     */
    String component();

    /**
     * Returns the endpoint port name.
     *
     * @return The endpoint port name.
     */
    String port();

    /**
     * Returns the endpoint instance number.
     *
     * @return The endpoint instance number.
     */
    int instance();

  }

  /**
   * Connection source.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  @JsonTypeInfo(
    use=JsonTypeInfo.Id.CLASS,
    include=JsonTypeInfo.As.PROPERTY,
    property="class",
    defaultImpl=DefaultConnectionContext.DefaultSourceContext.class
  )
  public static interface SourceContext extends EndpointContext<SourceContext> {
  }

  /**
   * Connection target.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  @JsonTypeInfo(
    use=JsonTypeInfo.Id.CLASS,
    include=JsonTypeInfo.As.PROPERTY,
    property="class",
    defaultImpl=DefaultConnectionContext.DefaultTargetContext.class
  )
  public static interface TargetContext extends EndpointContext<TargetContext> {
  }

}
