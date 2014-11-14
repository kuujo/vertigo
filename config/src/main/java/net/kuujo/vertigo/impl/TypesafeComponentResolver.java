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
package net.kuujo.vertigo.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.spi.ComponentResolver;

/**
 * Typesafe component resolver.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class TypesafeComponentResolver extends AbstractResolver implements ComponentResolver {
  private static final String COMPONENT_DEFAULTS = "component-defaults";

  @Override
  public int weight() {
    return -10;
  }

  @Override
  public ComponentConfig resolve(ComponentConfig component) {
    Config config = ConfigFactory.parseResourcesAnySyntax(component.getName())
      .withFallback(ConfigFactory.parseResourcesAnySyntax(COMPONENT_DEFAULTS)).resolve();
    component.update(configObjectToJson(config));
    return component;
  }

}
