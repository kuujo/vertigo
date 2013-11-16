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
package net.kuujo.vertigo.network;

import org.vertx.java.core.json.JsonObject;

/**
 * A verticle component.
 *
 * @author Jordan Halterman
 */
public class Verticle extends Component<Verticle> {
  public static final String MAIN = "main";

  public Verticle() {
    super();
    definition.putString(TYPE, VERTICLE);
  }

  public Verticle(String address) {
    super(address);
    definition.putString(TYPE, VERTICLE);
  }

  protected Verticle(JsonObject definition) {
    super(definition);
    definition.putString(TYPE, VERTICLE);
  }

  @Override
  public String getType() {
    return VERTICLE;
  }

  /**
   * Returns the verticle main.
   *
   * @return
   *   The verticle main.
   */
  public String getMain() {
    return definition.getString(MAIN);
  }

  /**
   * Sets the verticle main.
   *
   * @param main
   *   The verticle main.
   * @return
   *   The called verticle component.
   */
  public Verticle setMain(String main) {
    definition.putString(MAIN, main);
    return this;
  }

}
