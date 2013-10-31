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
package net.kuujo.vertigo;

import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.Future;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * A Vert.igo verticle implementation.
 *
 * The VertigoVerticle is an extension of the core Vert.x verticle that allows users
 * access to a Vert.igo instance for creating feeders, workers, and executors.
 *
 * @author Jordan Halterman
 */
public class VertigoVerticle extends Verticle {
  protected Vertigo vertigo;
  protected ComponentContext context;
  protected JsonObject config;
  protected Logger logger;

  @Override
  public void setContainer(Container container) {
    super.setContainer(container);
  }

  @Override
  public void setVertx(Vertx vertx) {
    super.setVertx(vertx);
  }

  @Override
  public void start(Future<Void> startedResult) {
    vertigo = new DefaultVertigo(vertx, container);
    config = container.config();
    logger = container.logger();
    JsonObject contextInfo = config.getObject("__context__");
    if (contextInfo == null) {
      logger.error("Invalid component context.");
    }

    config.removeField("__context__");
    try {
      context = Serializer.<ComponentContext>deserialize(contextInfo);
    }
    catch (SerializationException e) {
      logger.error(e);
    }
    vertigo.setContext(context);
    super.start(startedResult);
  }

}
