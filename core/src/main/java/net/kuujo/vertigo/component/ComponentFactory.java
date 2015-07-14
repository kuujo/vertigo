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
package net.kuujo.vertigo.component;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import net.kuujo.vertigo.util.Configs;

/**
 * Component verticle factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentFactory implements VerticleFactory {

  @Override
  public boolean requiresResolve() {
    return true;
  }

  @Override
  public void resolve(String identifier, DeploymentOptions options, ClassLoader classLoader, Future<String> resolution) {
    identifier = VerticleFactory.removePrefix(identifier);
    JsonObject config = Configs.load(identifier);
    String main = config.getString("identifier");
    if (main == null) {
      throw new VertxException(identifier + " does not contain a identifier field");
    }

    JsonObject deployment = config.getJsonObject("deployment");
    if (deployment != null) {
      if (deployment.containsKey("worker")) {
        options.setWorker(deployment.getBoolean("worker"));
      }
      if (deployment.containsKey("multi-threaded")) {
        options.setMultiThreaded(deployment.getBoolean("multi-threaded"));
      }
    }

    resolution.complete(main);
  }

  @Override
  public String prefix() {
    return "component";
  }

  @Override
  public Verticle createVerticle(String verticle, ClassLoader classLoader) throws Exception {
    throw new IllegalStateException();
  }

}
