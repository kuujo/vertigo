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

import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.annotations.Config;
import net.kuujo.vertigo.annotations.Schema;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.schema.Field;
import net.kuujo.vertigo.message.schema.MessageSchema;
import static net.kuujo.vertigo.util.Context.parseContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Base class for Java vertigo verticle implementations.
 *
 * @author Jordan Halterman
 */
abstract class VertigoVerticle<T extends Component<T>> extends Verticle {

  /**
   * Creates a component instance for the verticle.
   *
   * @param context
   *   The component instance context.
   * @return
   *   The new component instance.
   */
  protected abstract T createComponent(InstanceContext context);

  @Override
  public void start(final Future<Void> future) {
    InstanceContext context = parseContext(container.config());
    final T component = createComponent(context);
    try {
      checkConfig(container.config());
      declareSchema(component);
    }
    catch (Exception e) {
      future.setFailure(e);
      return;
    }

    component.start(new Handler<AsyncResult<T>>() {
      @Override
      public void handle(AsyncResult<T> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          VertigoVerticle.super.start(future);
          start(component);
        }
      }
    });
  }

  /**
   * Called when the component has been started.
   *
   * @param component
   *   The component that was started.
   */
  protected void start(T component) {
  }

  /**
   * Checks the worker configuration.
   */
  private void checkConfig(JsonObject config) {
    Config configInfo = getClass().getAnnotation(Config.class);
    if (configInfo != null) {
      for (Config.Field field : configInfo.value()) {
        Object value = config.getValue(field.name());
        if (value != null) {
          if (!field.type().isAssignableFrom(value.getClass())) {
            throw new VertigoException("Invalid component configuration.");
          }
        }
        else {
          if (field.required()) {
            throw new VertigoException("Invalid component configuration.");
          }
        }
      }
    }
  }

  /**
   * Declares the worker schema according to annotations.
   */
  private void declareSchema(T component) {
    Schema schemaInfo = getClass().getAnnotation(Schema.class);
    if (schemaInfo != null) {
      MessageSchema schema = new MessageSchema();
      for (Schema.Field field : schemaInfo.value()) {
        schema.addField(new Field(field.name(), field.type()).setRequired(field.required()));
      }
      component.declareSchema(schema);
    }
  }

}
