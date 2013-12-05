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

import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.Feeder;

/**
 * A feeder verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class FeederVerticle extends ComponentVerticle<Feeder> {
  protected Feeder feeder;

  @Override
  protected Feeder createComponent(InstanceContext context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createFeeder(context);
  }

  @Override
  protected void start(Feeder feeder) {
    this.feeder = feeder;
    feeder.feedHandler(new Handler<Feeder>() {
      @Override
      public void handle(Feeder feeder) {
        nextMessage(feeder);
      }
    });
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
