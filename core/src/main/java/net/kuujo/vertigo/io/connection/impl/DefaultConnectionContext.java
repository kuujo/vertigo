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
package net.kuujo.vertigo.io.connection.impl;

import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.io.connection.ConnectionContext;

/**
 * Connection contexts represent a direct connection between
 * one component and another component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class DefaultConnectionContext<T extends ConnectionContext<T>> extends BaseContext<T> implements ConnectionContext<T> {
  protected SourceContext source;
  protected TargetContext target;

  @Override
  public SourceContext source() {
    return source;
  }

  @Override
  public TargetContext target() {
    return target;
  }

  @Override
  public String uri() {
    return null;
  }

  @Override
  public String toString() {
    return String.format("Connection[%s-%d:%s->%s-%d:%s]", source.component(), source.instance(), source.port(), target.component(), target.instance(), target.port());
  }

  /**
   * Default connection endpoint.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   *
   * @param <T> The endpoint type.
   */
  public static class DefaultEndpointContext<T extends EndpointContext<T>> extends BaseContext<T> implements EndpointContext<T> {
    protected String address;
    protected String component;
    protected String port;
    protected int instance;

    @Override
    public String address() {
      return address;
    }

    @Override
    public String component() {
      return component;
    }

    @Override
    public String port() {
      return port;
    }

    @Override
    public int instance() {
      return instance;
    }

    @Override
    public String uri() {
      return null;
    }

    @Override
    public String toString() {
      return String.format("Connection[%s-%d:%s]", component, instance, port);
    }

    /**
     * Endpoint context builder.
     *
     * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
     *
     * @param <T> The endpoint type.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static class Builder<T extends DefaultEndpointContext, U extends Builder<T, U>> extends BaseContext.Builder<Builder<T, ?>, T> {

      protected Builder(T context) {
        super(context);
      }

      /**
       * Sets the endpoint component.
       *
       * @param component The endpoint component.
       * @return The context builder.
       */
      public U setComponent(String component) {
        context.component = component;
        return (U) this;
      }

      /**
       * Sets the endpoint port.
       *
       * @param port The endpoint port.
       * @return The context builder.
       */
      public U setPort(String port) {
        context.port = port;
        return (U) this;
      }

      /**
       * Sets the endpoint instance number.
       *
       * @param instance The endpoint instance number.
       * @return The context builder.
       */
      public U setInstance(int instance) {
        context.instance = instance;
        return (U) this;
      }

    }
    
  }

  /**
   * Default connection source.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  public static class DefaultSourceContext extends DefaultEndpointContext<SourceContext> implements SourceContext {

    @Override
    public String toString() {
      return String.format("Source[%s-%d:%s]", component, instance, port);
    }

    /**
     * Source context builder.
     *
     * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
     */
    public static class Builder extends DefaultEndpointContext.Builder<DefaultSourceContext, Builder> {

      private Builder() {
        super(new DefaultSourceContext());
      }

      protected Builder(DefaultSourceContext context) {
        super(context);
      }

      /**
       * Creates a new context builder.
       *
       * @return A new context builder.
       */
      public static Builder newBuilder() {
        return new Builder();
      }

      /**
       * Creates a new context builder.
       *
       * @param context A starting source context.
       * @return A new context builder.
       */
      public static Builder newBuilder(DefaultSourceContext context) {
        return new Builder(context);
      }

    }

  }

  /**
   * Default connection target.
   *
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  public static class DefaultTargetContext extends DefaultEndpointContext<TargetContext> implements TargetContext {

    @Override
    public String toString() {
      return String.format("Target[%s-%d:%s]", component, instance, port);
    }

    /**
     * Target context builder.
     *
     * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
     */
    public static class Builder extends DefaultEndpointContext.Builder<DefaultTargetContext, Builder> {

      private Builder() {
        super(new DefaultTargetContext());
      }

      protected Builder(DefaultTargetContext context) {
        super(context);
      }

      /**
       * Creates a new context builder.
       *
       * @return A new context builder.
       */
      public static Builder newBuilder() {
        return new Builder();
      }

      /**
       * Creates a new context builder.
       *
       * @param context A starting target context.
       * @return A new context builder.
       */
      public static Builder newBuilder(DefaultTargetContext context) {
        return new Builder(context);
      }

    }

  }

}
