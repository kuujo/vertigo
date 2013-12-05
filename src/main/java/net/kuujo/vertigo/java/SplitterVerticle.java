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

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.function.Function;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.splitter.Splitter;

/**
 * A Java aggregator verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class SplitterVerticle extends ComponentVerticle<Splitter> {
  protected String aggregationField;

  @Override
  protected Splitter createComponent(InstanceContext<Splitter> context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createSplitter(context);
  }

  @Override
  protected void start(Splitter splitter) {
    splitter.splitFunction(new Function<JsonMessage, Iterable<JsonObject>>() {
      @Override
      public Iterable<JsonObject> call(JsonMessage message) {
        return split(message);
      }
    });
  }

  /**
   * Splits a message into multiple sub-messages.
   *
   * @param message
   *   The message to split.
   * @return
   *   An iterable object containing split message data.
   */
  protected abstract Iterable<JsonObject> split(JsonMessage message);

}
