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
package net.kuujo.vertigo.context;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.network.Verticle;

/**
 * A verticle component context.
 *
 * @author Jordan Halterman
 */
public class VerticleContext extends ComponentContext {
 
  public VerticleContext() {
    super();
  }

  protected VerticleContext(JsonObject context) {
    super(context);
  }

  /**
   * Gets the verticle main.
   *
   * @return
   *   The verticle main.
   */
  public String getMain() {
    return context.getString(Verticle.MAIN);
  }

}
