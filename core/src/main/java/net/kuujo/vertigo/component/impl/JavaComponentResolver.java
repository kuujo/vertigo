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
package net.kuujo.vertigo.component.impl;

import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.port.PortInfo;
import net.kuujo.vertigo.spi.ComponentResolver;

/**
 * Java annotations based component resolver.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class JavaComponentResolver implements ComponentResolver {

  @Override
  public int weight() {
    return -100;
  }

  @Override
  public ComponentConfig resolve(ComponentConfig component) {
    Class<?> clazz;
    try {
      clazz = Class.forName(component.getIdentifier());
    } catch (ClassNotFoundException e) {
      return component;
    }

    ComponentInfo componentInfo = clazz.getAnnotation(ComponentInfo.class);
    if (componentInfo != null) {
      component.setWorker(componentInfo.worker());
      component.setMultiThreaded(componentInfo.multiThreaded());
      component.setStateful(componentInfo.stateful());
    }

    InputInfo inputInfo = clazz.getAnnotation(InputInfo.class);
    if (inputInfo != null) {
      for (PortInfo portInfo : inputInfo.value()) {
        component.getInput().addPort(portInfo.name())
          .setType(portInfo.type())
          .setCodec(portInfo.codec())
          .setPersistent(portInfo.persistent());
      }
    }

    OutputInfo outputInfo = clazz.getAnnotation(OutputInfo.class);
    if (outputInfo != null) {
      for (PortInfo portInfo : outputInfo.value()) {
        component.getOutput().addPort(portInfo.name())
          .setType(portInfo.type())
          .setCodec(portInfo.codec())
          .setPersistent(portInfo.persistent());
      }
    }
    return component;
  }

}
