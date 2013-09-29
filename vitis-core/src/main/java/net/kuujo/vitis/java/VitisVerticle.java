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
package net.kuujo.vitis.java;

import net.kuujo.vitis.Vitis;
import net.kuujo.vitis.DefaultVitis;
import net.kuujo.vitis.context.WorkerContext;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * A Vitis verticle implementation.
 *
 * The VitisVerticle is an extension of the core Vert.x verticle that allows users
 * access to a Vitis instance for creating feeders, workers, and executors.
 *
 * @author Jordan Halterman
 */
public class VitisVerticle extends Verticle {

  protected Vitis vitis;

  @Override
  public void setContainer(Container container) {
    super.setContainer(container);
    setupVitis();
  }

  @Override
  public void setVertx(Vertx vertx) {
    super.setVertx(vertx);
    setupVitis();
  }

  private void setupVitis() {
    if (container != null && vertx != null) {
      vitis = new DefaultVitis(vertx, container);
      vitis.setContext(new WorkerContext(container.config()));
    }
  }

}
