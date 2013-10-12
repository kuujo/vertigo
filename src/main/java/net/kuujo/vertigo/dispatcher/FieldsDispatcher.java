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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.kuujo.vertigo.messaging.Connection;
import net.kuujo.vertigo.messaging.ConnectionPool;
import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * A fields-based dispatcher implementation.
 *
 * @author Jordan Halterman
 */
public class FieldsDispatcher implements Dispatcher {

  private String fieldName;

  private List<Connection> items;

  private int size;

  public FieldsDispatcher(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void init(ConnectionPool connections) {
    this.items = new ArrayList<Connection>();
    Iterator<? extends Connection> iterator = connections.iterator();
    while (iterator.hasNext()) {
      items.add(iterator.next());
    }
    size = items.size();
  }

  @Override
  public void dispatch(JsonMessage message) {
    String value = message.body().getString(fieldName);
    if (value != null) {
      int length = value.length();
      items.get(length % size).write(message);
    }
  }

}
