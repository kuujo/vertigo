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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.context.Context;
import net.kuujo.vertigo.util.Observer;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base context.
 * 
 * @author Jordan Halterman
 */
abstract class BaseContext<T extends Context<T>> implements Context<T> {
  private static final Serializer serializer = SerializerFactory.getSerializer(BaseContext.class);
  @JsonIgnore
  protected final Set<Observer<T>> observers = new HashSet<>();
  protected String address;
  protected final Map<String, Object> options = new HashMap<>();

  @Override
  @SuppressWarnings("unchecked")
  public T registerObserver(Observer<T> observer) {
    observers.add(observer);
    return (T) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T unregisterObserver(Observer<T> observer) {
    observers.remove(observer);
    return (T) this;
  }

  @Override
  public void notify(T object) {
    for (Observer<T> observer : observers) {
      object.registerObserver(observer);
      observer.update(object);
    }
  }

  @Override
  public JsonObject options() {
    return new JsonObject(options);
  }

  @Override
  @SuppressWarnings({"hiding", "unchecked"})
  public <T> T option(String option) {
    return (T) options.get(option);
  }

  @Override
  @SuppressWarnings({"hiding", "unchecked"})
  public <T> T option(String option, T defaultValue) {
    T value = (T) options.get(option);
    return value != null ? value : defaultValue;
  }

  @Override
  public String toString() {
    return address();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof BaseContext && ((BaseContext<?>) other).address().equals(address());
  }

  @Override
  public int hashCode() {
    return address().hashCode();
  }

  /**
   * Creates a copy of the context.
   *
   * @return A new copy of the context.
   */
  @SuppressWarnings("unchecked")
  public T copy() {
    return (T) serializer.deserializeString(serializer.serializeToString(this), getClass());
  }

  /**
   * Base context builder.
   *
   * @author Jordan Halterman
   */
  @SuppressWarnings("rawtypes")
  public static abstract class Builder<T extends Builder<T, U>, U extends BaseContext> {
    protected final U context;

    protected Builder(U context) {
      this.context = context;
    }

    /**
     * Sets the context address.
     *
     * @param address The context address.
     * @return The context builder.
     */
    @SuppressWarnings("unchecked")
    public T setAddress(String address) {
      context.address = address;
      return (T) this;
    }

    /**
     * Sets context options.
     *
     * @param options A json object of context options.
     * @return The context builder.
     */
    @SuppressWarnings("unchecked")
    public T setOptions(JsonObject options) {
      for (String fieldName : options.getFieldNames()) {
        context.options.put(fieldName, options.getValue(fieldName));
      }
      return (T) this;
    }

    /**
     * Sets a context options.
     *
     * @param option The option name.
     * @param value The option value.
     * @return The context builder.
     */
    @SuppressWarnings("unchecked")
    public T setOption(String option, Object value) {
      context.options.put(option, value);
      return (T) this;
    }

    /**
     * Builds the context.
     *
     * @return The context.
     */
    public U build() {
      return context;
    }

  }

}
