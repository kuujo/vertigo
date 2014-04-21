/*
 * Copyright 2013-2014 the original author or authors.
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

import static net.kuujo.vertigo.util.Factories.createComponent;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.OutputCollector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

/**
 * Base class for Java vertigo component verticle implementations.
 * 
 * @author Jordan Halterman
 */
public abstract class ComponentVerticle extends Verticle {
  private Component component;
  protected Vertigo vertigo;
  protected InstanceContext context;
  protected Cluster cluster;
  protected Logger logger;
  protected InputCollector input;
  protected OutputCollector output;

  @Override
  public void start(final Future<Void> startResult) {
    component = createComponent(vertx, container);
    context = component.context();
    cluster = component.cluster();
    logger = component.logger();
    input = component.input();
    output = component.output();
    vertigo = new Vertigo(this);

    component.start(new Handler<AsyncResult<Component>>() {
      @Override
      public void handle(AsyncResult<Component> result) {
        if (result.failed()) {
          startResult.setFailure(result.cause());
        } else {
          ComponentVerticle.super.start(startResult);
        }
      }
    });
  }

}
