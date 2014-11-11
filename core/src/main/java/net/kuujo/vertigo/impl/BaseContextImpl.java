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

package net.kuujo.vertigo.impl;

import net.kuujo.vertigo.TypeContext;

/**
 * Base type context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class BaseContextImpl<T extends TypeContext<T>> implements TypeContext<T> {
  protected String id;

  @Override
  public String id() {
    return id;
  }

  @Override
  public String toString(boolean formatted) {
    return getClass().getCanonicalName();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T copy() {
    return (T) this;
  }

}