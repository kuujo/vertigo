/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.testtools;

import net.kuujo.vertigo.java.ComponentVerticle;

import org.vertx.java.core.Handler;

/**
 * Periodic feeder test component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class TestPeriodicFeeder extends ComponentVerticle {

  @Override
  public void start() {
    vertx.setPeriodic(1000, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        output.port("out").send(container.config());
      }
    });
  }

}
