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
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.OutputCollector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

/**
 * Base class for Java vertigo component verticle implementations.<p>
 *
 * To create a Vertigo component simply extend this base class.<p>
 *
 * <pre>
 * {@code
 * public class MyComponent extends ComponentVerticle {
 * 
 *   public void start() {
 *     output.port("out").send("Hello world!");
 *   }
 * 
 * }
 * }
 * </pre>
 * 
 * The component verticle works exactly the same way as all normal Vert.x
 * verticles. When the {@link ComponentVerticle#start()} method is called,
 * the <em>the component and the network to which it belongs have been
 * completely started</em>, so it's perfectly fine to begin sending messages.<p>
 *
 * The component also contains a reference to the {@link net.kuujo.vertigo.cluster.Cluster}
 * to which the network belongs. The <code>cluster</code> can be used to deploy
 * additional modules or verticles or access cluster-wide shared data.
 * If Vert.x is clustered then cluster deployments will be performed remotely
 * and shared data structures will be backed by Hazelcast.<p>
 *
 * The component also has a special <code>logger</code> which is a
 * {@link net.kuujo.vertigo.io.logging.PortLogger} that is specific to
 * the component instance. The port logger will log any messages to
 * the underlying Vert.x {@link org.vertx.java.core.logging.Logger}
 * as well as the corresponding output port.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
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
