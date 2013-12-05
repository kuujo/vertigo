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
package net.kuujo.vertigo.java;

import net.kuujo.vertigo.aggregator.Aggregator;
import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.function.Function;
import net.kuujo.vertigo.function.Function2;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * A Java aggregator verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AggregatorVerticle<T> extends ComponentVerticle<Aggregator<T>> {
  protected String aggregationField;

  @Override
  protected Aggregator<T> createComponent(InstanceContext context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createAggregator(context);
  }

  @Override
  protected void start(Aggregator<T> aggregator) {
    aggregator.setAggregationField(aggregationField);
    aggregator.initFunction(new Function<JsonMessage, T>() {
      @Override
      public T call(JsonMessage message) {
        return init(message);
      }
    });

    aggregator.aggregateFunction(new Function2<T, JsonMessage, T>() {
      @Override
      public T call(T current, JsonMessage message) {
        return aggregate(current, message);
      }
    });

    aggregator.completeFunction(new Function<T, Boolean>() {
      @Override
      public Boolean call(T value) {
        return isComplete(value);
      }
    });
  }

  /**
   * Initializes the aggregator with an initial value.
   *
   * @param data
   *   An initial message from which to prepare aggregation.
   * @return
   *   The initial aggregation value.
   */
  protected T init(JsonMessage data) {
    return null;
  }

  /**
   * Aggregates a message with the current value.
   *
   * @param current
   *   The current aggregation value.
   * @param message
   *   The message with which to update the value.
   * @return
   *   The updated aggregation value.
   */
  protected abstract T aggregate(T current, JsonMessage message);

  /**
   * Indicates whether the current value is complete.
   *
   * @param value
   *   The current value.
   * @return
   *   Indicates whether the current value is complete.
   */
  protected abstract boolean isComplete(T value);

}
