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

import net.kuujo.vertigo.DefaultVertigoFactory;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.VertigoFactory;
import net.kuujo.vertigo.annotations.Config;
import net.kuujo.vertigo.annotations.Schema;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.schema.Field;
import net.kuujo.vertigo.message.schema.MessageSchema;
import static net.kuujo.vertigo.util.Context.parseContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Base class for Java vertigo component verticle implementations.
 *
 * @author Jordan Halterman
 */
abstract class ComponentVerticle<T extends Component<T>> extends Verticle {
  protected Vertigo<T> vertigo;

  /**
   * Creates a component instance for the verticle.
   *
   * @param context
   *   The component instance context.
   * @return
   *   The new component instance.
   */
  protected abstract T createComponent(InstanceContext<T> context);

  /**
   * Because of the method by which Vertigo coordinates starting of
   * component instances, we do not use the future-based start method
   * here. Instead, we simply allow the Vertigo coordinator to do its
   * job and then provide a separate start method for when the component
   * is actually started (which differs from when the verticle is started).
   */
  @Override
  @SuppressWarnings("unchecked")
  public void start() {
    InstanceContext<T> context = (InstanceContext<T>) parseContext(container.config());
    final T component = createComponent(context);
    VertigoFactory factory = new DefaultVertigoFactory(vertx, container);
    vertigo = factory.createVertigo(component);
    try {
      checkConfig(container.config());
      declareSchema(component);
    }
    catch (Exception e) {
      return;
    }

    component.start(new Handler<AsyncResult<T>>() {
      @Override
      public void handle(AsyncResult<T> result) {
        if (result.succeeded()) {
          start(component);
        }
      }
    });
  }

  /**
   * Called when the component has been started.
   *
   * This method differs from the normal {@link start() start} method in that
   * this method will be called *after* all other start methods. This is because
   * this method is actually called once the component has connected to all of
   * its interested trading partners.
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
