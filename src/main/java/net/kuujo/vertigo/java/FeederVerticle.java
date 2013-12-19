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

import org.vertx.java.core.Handler;

import net.kuujo.vertigo.annotations.FeederOptions;
import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.impl.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.Feeder;

/**
 * A feeder verticle implementation.<p>
 *
 * This is a basic verticle that makes a Vertigo feeder available via the
 * {@link #start(Feeder)} method. Users can either operate on the {@link Feeder}
 * by overriding that method or my overriding the {@link #nextMessage(Feeder)}
 * method.<p>
 *
 * <pre>
 * public class MyFeederVerticle extends FeederVerticle {
 *   protected void start(Feeder feeder) {
 *     feeder.emit(new JsonObject().putString("foo", "bar"), new Handler<AsyncResult<MessageId>>() {
 *       public void handle(AsyncResult<MessageId> result) {
 *         ...
 *       }
 *     });
 *   }
 * }
 * </pre>
 *
 * @author Jordan Halterman
 */
public abstract class FeederVerticle extends ComponentVerticle<Feeder> {
  protected Feeder feeder;

  @Override
  protected Feeder createComponent(InstanceContext<Feeder> context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createFeeder(context);
  }

  @Override
  protected void start(Feeder feeder) {
    this.feeder = setupFeeder(feeder);
    feeder.feedHandler(new Handler<Feeder>() {
      @Override
      public void handle(Feeder feeder) {
        nextMessage(feeder);
      }
    });
  }

  /**
   * Sets up the feeder according to feeder options.
   */
  private Feeder setupFeeder(Feeder feeder) {
    FeederOptions options = getClass().getAnnotation(FeederOptions.class);
    if (options != null) {
      feeder.setFeedQueueMaxSize(options.feedQueueMaxSize());
      feeder.setAutoRetry(options.autoRetry());
      feeder.setAutoRetryAttempts(options.autoRetryAttempts());
      feeder.setFeedInterval(options.feedInterval());
    }
    return feeder;
  }

  /**
   * Called when the feeder is requesting the next message.
   *
   * Override this method to perform polling-based feeding. The feeder will automatically
   * call this method any time the feed queue is prepared to accept new messages.
   */
  protected void nextMessage(Feeder feeder) {
  }

}
