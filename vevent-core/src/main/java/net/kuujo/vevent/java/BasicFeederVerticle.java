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

import net.kuujo.vevent.context.WorkerContext;
import net.kuujo.vevent.node.BasicFeeder;
import net.kuujo.vevent.node.Feeder;

/**
 * A basic feeder verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class BasicFeederVerticle extends FeederVerticle {

  @Override
  Feeder createFeeder(WorkerContext context) {
    return new BasicFeeder(vertx, container, context);
  }

}
