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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import net.kuujo.vertigo.component.ComponentDescriptor;
import net.kuujo.vertigo.component.ComponentLocator;
import net.kuujo.vertigo.util.Configs;

/**
 * Component locator implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentLocatorImpl implements ComponentLocator {

  @Override
  public ComponentDescriptor locateComponent(String component) {
    Config config = ConfigFactory.parseResourcesAnySyntax(component)
      .withFallback(ConfigFactory.empty().withValue("vertigo.component", ConfigValueFactory.fromAnyRef(component)));
    return new ComponentDescriptorImpl(Configs.configObjectToJson(config));
  }

}
