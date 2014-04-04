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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.kuujo.vertigo.context.ConnectionContext;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.data.DataStore;
import net.kuujo.vertigo.input.grouping.MessageGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Output connection context.
 *
 * @author Jordan Halterman
 */
public class DefaultOutputConnectionContext extends DefaultConnectionContext<OutputConnectionContext> implements OutputConnectionContext {
  private ConnectionContext.Delivery delivery;
  private ConnectionContext.Order order;
  private Class<? extends DataStore> storage;
  private List<String> targets = new ArrayList<>();
  private MessageGrouping grouping = new RoundGrouping();
  @JsonIgnore
  private OutputPortContext port;

  DefaultOutputConnectionContext setPort(OutputPortContext port) {
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
  public Class<? extends DataStore> storage() {
    return storage;
  }

  @Override
  public OutputPortContext port() {
    return port;
  }

  /**
   * Returns a list of output addresses.
   *
   * @return A list of output addresses.
   */
  public List<String> targets() {
    return targets;
  }

  /**
   * Returns the output connection grouping.
   *
   * @return The output connection grouping.
   */
  public MessageGrouping grouping() {
    return grouping;
  }

  /**
   * Output connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultOutputConnectionContext> {

    private Builder() {
      super(new DefaultOutputConnectionContext());
    }

    private Builder(DefaultOutputConnectionContext context) {
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
    public static Builder newBuilder(DefaultOutputConnectionContext context) {
      return new Builder(context);
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
     * Sets the connection data store.
     *
     * @param storage The connection data store.
     * @return The context builder.
     */
    public Builder setDataStore(Class<? extends DataStore> storage) {
      context.storage = storage;
      return this;
    }

    /**
     * Sets the connection targets.
     *
     * @param targets The connection targets.
     * @return The context builder.
     */
    public Builder setTargets(String... targets) {
      context.targets = Arrays.asList(targets);
      return this;
    }

    /**
     * Sets the connection targets.
     *
     * @param targets The connection targets.
     * @return The context builder.
     */
    public Builder setTargets(List<String> targets) {
      context.targets = targets;
      return this;
    }

    /**
     * Adds a target to the component.
     *
     * @param target The target to add.
     * @return The context builder.
     */
    public Builder addTarget(String target) {
      context.targets.add(target);
      return this;
    }

    /**
     * Removes a target from the component.
     *
     * @param target The target to remove.
     * @return The context builder.
     */
    public Builder removeTarget(String target) {
      context.targets.remove(target);
      return this;
    }

    /**
     * Sets the connection grouping.
     *
     * @param grouping The connection grouping.
     * @return The context builder.
     */
    public Builder setGrouping(MessageGrouping grouping) {
      context.grouping = grouping;
      return this;
    }
  }

}
