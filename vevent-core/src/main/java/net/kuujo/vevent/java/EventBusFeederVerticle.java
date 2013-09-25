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
package net.kuujo.vevent.java;

import net.kuujo.vevent.context.ComponentContext;
import net.kuujo.vevent.feeder.EventBusFeeder;
import net.kuujo.vevent.feeder.Feeder;

/**
 * An eventbus-based feeder verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class EventBusFeederVerticle extends FeederVerticle {

  @Override
  Feeder createFeeder(ComponentContext context) {
    return new EventBusFeeder(context.getAddress(), vertx.eventBus(), context);
  }

}
