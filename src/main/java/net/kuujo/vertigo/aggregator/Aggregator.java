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
package net.kuujo.vertigo.aggregator;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.function.Function;
import net.kuujo.vertigo.function.Function2;
import net.kuujo.vertigo.function.Provider;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * A data aggregator.
 *
 * @author Jordan Halterman
 */
public interface Aggregator<T> extends Component<Aggregator<T>> {

  /**
   * Sets the aggregation output field name.
   *
   * @param fieldName
   *   The output field name.
   * @return
   *   The called aggregator instance.
   */
  Aggregator<T> setAggregationField(String fieldName);

  /**
   * Gets the aggregation output field name.
   *
   * @return
   *   The output field name.
   */
  String getAggregationField();

  /**
   * Sets an empty initializer function on the aggregator.
   *
   * @param initializer
   *   An empty initializer function.
   * @return
   *   The called aggregator instance.
   */
  Aggregator<T> initFunction(Provider<T> initializer);

  /**
   * Sets the initializer function on the aggregator.
   *
   * @param initializer
   *   The initializer function.
   * @return
   *   The called aggregator instance.
   */
  Aggregator<T> initFunction(Function<JsonMessage, T> initializer);

  /**
   * Sets the aggregate function on the aggregator.
   *
   * @param aggregator
   *   The aggregate function.
   * @return
   *   The called aggregator instance.
   */
  Aggregator<T> aggregateFunction(Function2<T, JsonMessage, T> aggregator);

  /**
   * Sets the complete function on the aggreagtor.
   *
   * @param complete
   *   A function indicating whether the aggregation is complete.
   * @return
   *   The called aggregator instance.
   */
  Aggregator<T> completeFunction(Function<T, Boolean> complete);

}
