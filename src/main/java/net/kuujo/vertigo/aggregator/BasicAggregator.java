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

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.component.BaseComponent;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.function.Function;
import net.kuujo.vertigo.function.Function2;
import net.kuujo.vertigo.function.Provider;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * A basic aggregator implementation.
 *
 * @author Jordan Halterman
 */
public class BasicAggregator<T> extends BaseComponent<Aggregator<T>> implements Aggregator<T> {
  private static final String DEFAULT_FIELD_NAME = "value";
  private String field;
  private Provider<T> emptyInitFunction;
  private Function<JsonMessage, T> initFunction;
  private Function2<T, JsonMessage, T> aggregateFunction;
  private Function<T, Boolean> completeFunction;
  private T currentValue;
  private final Handler<JsonMessage> messageHandler = new Handler<JsonMessage>() {
    @Override
    public void handle(JsonMessage message) {
      if (currentValue == null) {
        if (initFunction != null) {
          currentValue = initFunction.call(message);
        }
        else if (emptyInitFunction != null) {
          currentValue = emptyInitFunction.call();
        }
      }
      else {
        currentValue = aggregateFunction.call(currentValue, message);
      }

      if (completeFunction.call(currentValue)) {
        // If no output field is provided then emit the value to a default field.
        // If the value is a JsonObject then emit it as the absolute message value.
        if (field == null) {
          if (currentValue instanceof JsonObject) {
            output.emit((JsonObject) currentValue, message);
          }
          else {
            output.emit(new JsonObject().putValue(DEFAULT_FIELD_NAME, currentValue), message);
          }
        }
        else {
          output.emit(new JsonObject().putValue(field, currentValue), message);
        }
        currentValue = null;
      }
      input.ack(message);
    }
  };

  public BasicAggregator(Vertx vertx, Container container, InstanceContext<Aggregator<T>> context) {
    super(vertx, container, context);
  }

  @Override
  public Aggregator<T> setAggregationField(String fieldName) {
    this.field = fieldName;
    return this;
  }

  @Override
  public String getAggregationField() {
    return field;
  }

  @Override
  public Aggregator<T> initFunction(Provider<T> initializer) {
    this.emptyInitFunction = initializer;
    this.initFunction = null;
    checkReady();
    return this;
  }

  @Override
  public Aggregator<T> initFunction(Function<JsonMessage, T> initializer) {
    this.initFunction = initializer;
    this.emptyInitFunction = null;
    checkReady();
    return this;
  }

  @Override
  public Aggregator<T> aggregateFunction(Function2<T, JsonMessage, T> aggregator) {
    this.aggregateFunction = aggregator;
    checkReady();
    return this;
  }

  @Override
  public Aggregator<T> completeFunction(Function<T, Boolean> complete) {
    this.completeFunction = complete;
    checkReady();
    return this;
  }

  /**
   * Checks whether the aggregator is ready and if so registers an input message handler.
   */
  private void checkReady() {
    if ((emptyInitFunction != null || initFunction != null) && aggregateFunction != null && completeFunction != null) {
      input.messageHandler(messageHandler);
    }
    else {
      input.messageHandler(null);
    }
  }

}
