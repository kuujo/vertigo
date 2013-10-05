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
package net.kuujo.vertigo.dispatcher;

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.messaging.Connection;
import net.kuujo.vertigo.messaging.ConnectionPool;
import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * An abstract dispatcher implementation.
 *
 * @author Jordan Halterman
 */
public class AllDispatcher implements Dispatcher {

  private ConnectionPool connections;

  private Map<String, String> options = new HashMap<String, String>();

  @Override
  public void init(ConnectionPool connections) {
    this.connections = connections;
  }

  @Override
  public Dispatcher setOption(String option, String value) {
    options.put(option, value);
    return this;
  }

  @Override
  public String getOption(String option) {
    return options.get(option);
  }

  @Override
  public String getOption(String option, String defaultValue) {
    if (options.containsKey(option)) {
      return options.get(option);
    }
    return defaultValue;
  }

  @Override
  public void dispatch(JsonMessage message) {
    for (Connection connection : connections) {
      connection.write(message);
    }
  }

}
