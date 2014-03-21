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
package net.kuujo.vertigo.context;

import java.util.HashSet;
import java.util.Set;

import net.kuujo.vertigo.util.Observable;
import net.kuujo.vertigo.util.Observer;
import net.kuujo.vertigo.util.serializer.Serializable;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base context.
 * 
 * @author Jordan Halterman
 */
public abstract class Context<T extends Context<T>> implements Observable<T>, Serializable {
  private static final Serializer serializer = SerializerFactory.getSerializer(Context.class);
  @JsonIgnore
  protected final Set<Observer<T>> observers = new HashSet<>();

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

  /**
   * Returns the context address.
   *
   * @return The context address.
   */
  public abstract String address();

  @Override
  public String toString() {
    return address();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Context && ((Context<?>) other).address().equals(address());
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
  public static class Builder<T extends Context<T>> {
    protected final T context;

    protected Builder(T context) {
      this.context = context;
    }

    /**
     * Builds the context.
     *
     * @return The context.
     */
    public T build() {
      return context;
    }

  }

}
