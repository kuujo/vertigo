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
package net.kuujo.vertigo.filter;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.component.BaseComponent;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.function.Function;
import net.kuujo.vertigo.message.JsonMessage;

/**
 * A basic filter implementation.
 *
 * @author Jordan Halterman
 */
public class BasicFilter extends BaseComponent<Filter> implements Filter {
  private Function<JsonMessage, Boolean> filter;
  private final Handler<JsonMessage> messageHandler = new Handler<JsonMessage>() {
    @Override
    public void handle(JsonMessage message) {
      if (filter.call(message)) {
        output.emit(message);
      }
      input.ack(message);
    }
  };

  public BasicFilter(Vertx vertx, Container container, InstanceContext<Filter> context) {
    super(vertx, container, context);
  }

  @Override
  public Filter filterFunction(Function<JsonMessage, Boolean> filter) {
    this.filter = filter;
    input.messageHandler(messageHandler);
    return this;
  }

}
