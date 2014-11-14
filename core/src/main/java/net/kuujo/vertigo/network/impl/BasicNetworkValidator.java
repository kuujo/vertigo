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
package net.kuujo.vertigo.network.impl;

import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.ValidationException;
import net.kuujo.vertigo.spi.NetworkValidator;

/**
 * Basic network validator.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class BasicNetworkValidator implements NetworkValidator {

  @Override
  public void validate(Network network) {
    if (network.getName() == null) {
      throw new ValidationException("Network name cannot be null");
    }
    if (network.getComponents() == null) {
      throw new ValidationException("Network components cannot be null");
    }
    if (network.getComponents().isEmpty()) {
      throw new ValidationException("Network components cannot be empty");
    }
    if (network.getConnections() == null) {
      throw new ValidationException("Network connections cannot be null");
    }
    if (network.getConnections().isEmpty()) {
      throw new ValidationException("Network connections cannot be empty");
    }
  }

}
