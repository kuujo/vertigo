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

import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.filter.Filter;
import net.kuujo.vertigo.function.Function;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * A Java filter verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class FilterVerticle extends ComponentVerticle<Filter> {
  protected Filter filter;

  @Override
  protected Filter createComponent(InstanceContext<Filter> context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createFilter(context);
  }

  @Override
  protected void start(Filter filter) {
    this.filter = filter;
    filter.filterFunction(new Function<JsonMessage, Boolean>() {
      @Override
      public Boolean call(JsonMessage message) {
        return isKeep(message);
      }
    });
  }

  /**
   * Indicates whether the given message should be kept or filtered out.
   *
   * @param message
   *   The message to evaluate.
   * @return
   *   Indicates whether the message should be kept.
   */
  protected abstract boolean isKeep(JsonMessage message);

}
