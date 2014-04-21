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
package net.kuujo.vertigo.util;

/**
 * An observable interface. This interface differs from the normal
 * observable in that it allows immutable objects to be "updated"
 * by replacing them with a new updated immutable.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Observable<T extends Observable<T>> {

  /**
   * Registers an observer.
   *
   * @param observer The observer to register.
   * @return The observable.
   */
  T registerObserver(Observer<T> observer);

  /**
   * Unregisters an observer.
   *
   * @param observer The observer to unregister.
   * @return The observable.
   */
  T unregisterObserver(Observer<T> observer);

  /**
   * Notifies all observers of an update.
   *
   * @param object The updated object.
   */
  void notify(T object);

}
