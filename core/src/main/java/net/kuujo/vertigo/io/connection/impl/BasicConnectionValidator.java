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
package net.kuujo.vertigo.io.connection.impl;

import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.network.ValidationException;
import net.kuujo.vertigo.spi.ConnectionValidator;

/**
 * Basic connection validator.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class BasicConnectionValidator implements ConnectionValidator {

  @Override
  public void validate(ConnectionConfig connection) {
    if (connection.getSource() == null) {
      throw new ValidationException("Connection source cannot be null");
    }
    if (connection.getSource().getComponent() == null) {
      throw new ValidationException("Connection source component cannot be null");
    }
    if (connection.getSource().getPort() == null) {
      throw new ValidationException("Connection source port cannot be null");
    }
    if (connection.getTarget().getComponent() == null) {
      throw new ValidationException("Connection source component cannot be null");
    }
    if (connection.getTarget().getPort() == null) {
      throw new ValidationException("Connection source port cannot be null");
    }
  }

}
