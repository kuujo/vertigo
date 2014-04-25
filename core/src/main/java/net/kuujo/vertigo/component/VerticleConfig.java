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

import net.kuujo.vertigo.component.impl.DefaultVerticleConfig;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Verticle component configuration.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultVerticleConfig.class
)
public interface VerticleConfig extends ComponentConfig<VerticleConfig> {

  /**
   * <code>main</code> is a string indicating the verticle main. This field is required
   * for all verticle components.
   */
  public static final String VERTICLE_MAIN = "main";

  /**
   * <code>worker</code> is a boolean indicating whether this verticle should be deployed
   * as a worker verticle. Defaults to <code>false</code>
   */
  public static final String VERTICLE_IS_WORKER = "worker";

  /**
   * <code>multi-threaded</code> is a boolean indicating whether a worker verticle is
   * multi-threaded. This option only applies to verticles where <code>worker</code> is
   * <code>true</code>. Defaults to <code>false</code>
   */
  public static final String VERTICLE_IS_MULTI_THREADED = "multi-threaded";

  /**
   * Sets the verticle main.
   * 
   * @param main The verticle main.
   * @return The verticle configuration.
   */
  VerticleConfig setMain(String main);

  /**
   * Gets the verticle main.
   * 
   * @return The verticle main.
   */
  String getMain();

  /**
   * Sets the verticle worker option.
   * 
   * @param isWorker Indicates whether the verticle should be deployed as a worker.
   * @return The verticle configuration.
   */
  VerticleConfig setWorker(boolean isWorker);

  /**
   * Returns a boolean indicating whether the verticle is a worker.
   * 
   * @return Indicates whether the verticle is a worker.
   */
  boolean isWorker();

  /**
   * Sets the verticle multi-threaded option. This option only applies to worker
   * verticles.
   * 
   * @param isMultiThreaded Indicates whether the worker verticle is multi-threaded.
   * @return The verticle configuration.
   */
  VerticleConfig setMultiThreaded(boolean isMultiThreaded);

  /**
   * Returns a boolean indicating whether the verticle is a worker and is multi-threaded.
   * 
   * @return Indicates whether the verticle is a worker and is multi-threaded. If the
   *         verticle is not a worker verticle then <code>false</code> will be returned.
   */
  boolean isMultiThreaded();

}
