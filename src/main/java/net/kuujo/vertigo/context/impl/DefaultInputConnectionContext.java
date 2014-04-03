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
package net.kuujo.vertigo.context.impl;

import net.kuujo.vertigo.context.ConnectionContext;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.context.InputPortContext;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Input connection context.
 *
 * @author Jordan Halterman
 */
public class DefaultInputConnectionContext extends DefaultConnectionContext<InputConnectionContext> implements InputConnectionContext {
  private String address;
  private ConnectionContext.Delivery delivery = ConnectionContext.Delivery.AT_MOST_ONCE;
  private ConnectionContext.Order order = ConnectionContext.Order.NO_ORDER;
  private String source;
  @JsonIgnore
  private InputPortContext port;

  DefaultInputConnectionContext setPort(InputPortContext port) {
    this.port = port;
    return this;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public ConnectionContext.Delivery delivery() {
    return delivery;
  }

  @JsonSetter("delivery")
  private void setDeliveryMethod(String delivery) {
    this.delivery = Delivery.parse(delivery);
  }

  @JsonGetter("delivery")
  private String getDeliveryMethod() {
    return this.delivery.toString();
  }

  @Override
  public ConnectionContext.Order order() {
    return order;
  }

  @JsonSetter("order")
  private void setOrderMethod(boolean ordered) {
    this.order = Order.parse(ordered);
  }

  @JsonGetter("order")
  private boolean getOrderMethod() {
    return this.order.isOrdered();
  }

  @Override
  public InputPortContext port() {
    return port;
  }

  @Override
  public String source() {
    return source;
  }

  /**
   * Input connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<DefaultInputConnectionContext> {

    private Builder() {
      super(new DefaultInputConnectionContext());
    }

    private Builder(DefaultInputConnectionContext context) {
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
     * @param context A starting connection context.
     * @return A new context builder.
     */
    public static Builder newBuilder(DefaultInputConnectionContext context) {
      return new Builder(context);
    }

    /**
     * Sets the stream address.
     *
     * @param address The stream address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the message delivery method.
     *
     * @param delivery The message delivery method.
     * @return The context builder.
     */
    public Builder setDelivery(ConnectionContext.Delivery delivery) {
      context.delivery = delivery;
      return this;
    }

    /**
     * Sets the message order.
     *
     * @param order The message order.
     * @return The context builder.
     */
    public Builder setOrder(ConnectionContext.Order order) {
      context.order = order;
      return this;
    }

    /**
     * Sets the connection source.
     *
     * @param source The connection source.
     * @return The context builder.
     */
    public Builder setSource(String source) {
      context.source = source;
      return this;
    }
  }

}
