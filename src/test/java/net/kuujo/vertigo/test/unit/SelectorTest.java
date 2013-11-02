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
package net.kuujo.vertigo.test.unit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.kuujo.vertigo.messaging.DefaultJsonMessage;
import net.kuujo.vertigo.messaging.JsonMessage;
import net.kuujo.vertigo.output.Connection;
import net.kuujo.vertigo.output.DefaultConnection;
import net.kuujo.vertigo.output.DefaultPseudoConnection;
import net.kuujo.vertigo.output.selector.AllSelector;
import net.kuujo.vertigo.output.selector.FieldsSelector;
import net.kuujo.vertigo.output.selector.RandomSelector;
import net.kuujo.vertigo.output.selector.RoundSelector;
import net.kuujo.vertigo.output.selector.Selector;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Selector tests.
 *
 * @author Jordan Halterman
 */
public class SelectorTest {
  @SuppressWarnings("serial")
  private List<Connection> testConnections = new ArrayList<Connection>() {{
    add(new DefaultConnection("foo", null));
    add(new DefaultConnection("bar", null));
    add(new DefaultPseudoConnection(null));
  }};
  private JsonMessage testMessage = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "auditor");

  @Test
  public void testAllSelector() {
    Selector selector = new AllSelector();
    for (Connection connection : selector.select(testMessage, testConnections)) {
      assertTrue(testConnections.contains(connection));
    }
  }

  @Test
  public void testRandomSelector() {
    Selector selector = new RandomSelector();
    Set<Connection> selected = new HashSet<Connection>();
    while (selected.size() < testConnections.size()) {
      List<Connection> connections = selector.select(testMessage, testConnections);
      assertEquals(1, connections.size());
      Connection connection = connections.get(0);
      if (!selected.contains(connection)) {
        selected.add(connection);
      }
    }
  }

  @Test
  public void testRoundSelector() {
    Selector selector = new RoundSelector();
    for (int i = 0; i < 5; i++) {
      int expected = 0;
      for (int j = 0; j < testConnections.size(); j++) {
        List<Connection> connections = selector.select(testMessage, testConnections);
        assertEquals(1, connections.size());
        Connection connection = connections.get(0);
        assertEquals(testConnections.get(expected), connection);
        expected++;
      }
    }
  }

  @Test
  public void testFieldsSelector() {
    Selector selector = new FieldsSelector("test");

    JsonMessage test1 = DefaultJsonMessage.create(new JsonObject().putString("test", "a"), "auditor");
    List<Connection> connections1 = selector.select(test1, testConnections);
    assertEquals(1, connections1.size());
    List<Connection> connections2 = selector.select(test1, testConnections);
    assertEquals(1, connections2.size());
    assertEquals(connections1.get(0), connections2.get(0));

    JsonMessage test2 = DefaultJsonMessage.create(new JsonObject().putString("test", "a"), "auditor");
    List<Connection> connections3 = selector.select(test2, testConnections);
    assertEquals(1, connections3.size());
    List<Connection> connections4 = selector.select(test2, testConnections);
    assertEquals(1, connections4.size());
    assertEquals(connections3.get(0), connections4.get(0));

    JsonMessage test3 = DefaultJsonMessage.create(new JsonObject().putString("test", "a"), "auditor");
    List<Connection> connections5 = selector.select(test3, testConnections);
    assertEquals(1, connections5.size());
    List<Connection> connections6 = selector.select(test3, testConnections);
    assertEquals(1, connections6.size());
    assertEquals(connections5.get(0), connections6.get(0));
  }

}
