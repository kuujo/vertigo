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
@SuppressWarnings("rawtypes")
public class Verticle<T extends net.kuujo.vertigo.component.Component> extends ComponentType<Verticle<T>, T> {
  private String main;
  private boolean worker;
  @JsonProperty("multi-threaded")
  private boolean multiThreaded;

  public Verticle() {
  }

  public Verticle(Class<T> type, String address, String main) {
    super(type, address);
    setMain(main);
  }

  @Override
  public boolean isVerticle() {
    return true;
  }

  /**
   * Sets the verticle main.
   *
   * @param main
   *   The verticle main.
   * @return
   *   The verticle configuration.
   */
  public Verticle<T> setMain(String main) {
    this.main = main;
    return this;
  }

  /**
   * Gets the verticle main.
   *
   * @return
   *   The verticle main.
   */
  public String getMain() {
    return main;
  }

  /**
   * Sets the verticle worker option.
   *
   * @param isWorker
   *   Indicates whether the verticle should be deployed as a worker.
   * @return
   *   The verticle configuration.
   */
  public Verticle<T> setWorker(boolean isWorker) {
    worker = isWorker;
    return this;
  }

  /**
   * Returns a boolean indicating whether the verticle is a worker.
   *
   * @return
   *   Indicates whether the verticle is a worker.
   */
  public boolean isWorker() {
    return worker;
  }

  /**
   * Sets the verticle multi-threaded option. This option only applies to worker
   * verticles.
   *
   * @param isMultiThreaded
   *   Indicates whether the worker verticle is multi-threaded.
   * @return
   *   The verticle configuration.
   */
  public Verticle<T> setMultiThreaded(boolean isMultiThreaded) {
    multiThreaded = isMultiThreaded;
    return this;
  }

  /**
   * Returns a boolean indicating whether the verticle is a worker and is multi-threaded.
   *
   * @return
   *   Indicates whether the verticle is a worker and is multi-threaded. If the
   *   verticle is not a worker verticle then <code>false</code> will be returned.
   */
  public boolean isMultiThreaded() {
    return worker && multiThreaded;
  }

}
