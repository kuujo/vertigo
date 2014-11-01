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

import io.vertx.codegen.annotations.VertxGen;
import net.kuujo.vertigo.TypeInfo;
import net.kuujo.vertigo.component.impl.InstanceInfoImpl;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.OutputInfo;

/**
 * A component instance context which contains information regarding a specific component
 * (module or verticle) instance within a network.<p>
 *
 * The instance context is the primary immutable configuration used by
 * each instance of a component verticle or module to construct its input
 * and output ports/connections. Note that the context is observable, and
 * component instance observe the context for changes within the cluster
 * in order to support runtime network configuration changes by automatically
 * updating their input and output connections when the context changes.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface InstanceInfo extends TypeInfo<InstanceInfo> {

  /**
   * Returns a new instance info builder.
   *
   * @return A new instance info builder.
   */
  static TypeInfo.Builder<InstanceInfo> builder() {
    return new InstanceInfoImpl.Builder();
  }

  /**
   * Returns a new instance info builder.
   *
   * @param instance An existing instance info object to wrap.
   * @return An instance info builder wrapper.
   */
  static TypeInfo.Builder<InstanceInfo> builder(InstanceInfo instance) {
    return new InstanceInfoImpl.Builder((InstanceInfoImpl) instance);
  }

  /**
   * Returns the instance number.
   * 
   * @return The instance number.
   */
  int number();

  /**
   * Returns the instance input context.
   *
   * @return The instance input context.
   */
  InputInfo input();

  /**
   * Returns the instance output context.
   *
   * @return The instance output context.
   */
  OutputInfo output();

  /**
   * Returns the parent component context.
   * 
   * @return The parent component context.
   */
  ComponentInfo component();

}
