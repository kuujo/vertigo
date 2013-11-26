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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A verticle component.
 *
 * @author Jordan Halterman
 */
public class Verticle extends Component<Verticle> {
  @JsonProperty private String main;

  public Verticle() {
    super();
  }

  public Verticle(String address) {
    super(address);
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
    return main;
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
    this.main = main;
    return this;
  }

}
