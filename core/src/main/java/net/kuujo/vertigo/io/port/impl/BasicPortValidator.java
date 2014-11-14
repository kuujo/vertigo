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
package net.kuujo.vertigo.io.port.impl;

import net.kuujo.vertigo.io.port.PortConfig;
import net.kuujo.vertigo.network.ValidationException;
import net.kuujo.vertigo.spi.PortValidator;

/**
 * Basic port validator.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class BasicPortValidator implements PortValidator {

  @Override
  public void validate(PortConfig port) {
    if (port.getName() == null) {
      throw new ValidationException("Port name cannot be null");
    }
    if (port.getType() == null) {
      throw new ValidationException("Port type cannot be null");
    }
  }

}
