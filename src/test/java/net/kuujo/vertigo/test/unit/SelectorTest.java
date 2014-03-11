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

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.impl.DefaultJsonMessage;
import net.kuujo.vertigo.output.OutputConnection;
import net.kuujo.vertigo.output.impl.DefaultOutputConnection;
import net.kuujo.vertigo.output.selector.AllSelector;
import net.kuujo.vertigo.output.selector.FieldsSelector;
import net.kuujo.vertigo.output.selector.RandomSelector;
import net.kuujo.vertigo.output.selector.RoundSelector;
import net.kuujo.vertigo.output.selector.Selector;

import org.junit.Test;
import org.vertx.java.core.impl.DefaultVertx;
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
  private List<OutputConnection> testConnections = new ArrayList<OutputConnection>() {{
    add(new DefaultOutputConnection("foo", new DefaultVertx()));
    add(new DefaultOutputConnection("bar", new DefaultVertx()));
    add(new DefaultOutputConnection("baz", new DefaultVertx()));
  }};

  private JsonMessage testMessage = DefaultJsonMessage.Builder.newBuilder()
      .setBody(new JsonObject().putString("body", "Hello world!")).build();

  @Test
  public void testAllSelector() {
    Selector selector = new AllSelector();
    for (OutputConnection outputConnection : selector.select(testMessage, testConnections)) {
      assertTrue(testConnections.contains(outputConnection));
    }
  }

  @Test
  public void testRandomSelector() {
    Selector selector = new RandomSelector();
    Set<OutputConnection> selected = new HashSet<OutputConnection>();
    while (selected.size() < testConnections.size()) {
      List<OutputConnection> outputConnections = selector.select(testMessage, testConnections);
      assertEquals(1, outputConnections.size());
      OutputConnection outputConnection = outputConnections.get(0);
      if (!selected.contains(outputConnection)) {
        selected.add(outputConnection);
      }
    }
  }

  @Test
  public void testRoundSelector() {
    Selector selector = new RoundSelector();
    for (int i = 0; i < 5; i++) {
      int expected = 0;
      for (int j = 0; j < testConnections.size(); j++) {
        List<OutputConnection> outputConnections = selector.select(testMessage, testConnections);
        assertEquals(1, outputConnections.size());
        OutputConnection outputConnection = outputConnections.get(0);
        assertEquals(testConnections.get(expected), outputConnection);
        expected++;
      }
    }
  }

  @Test
  public void testFieldsSelector() {
    Selector selector = new FieldsSelector("test");

    JsonMessage test1 = DefaultJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test", "a")).build();
    List<OutputConnection> connections1 = selector.select(test1, testConnections);
    assertEquals(1, connections1.size());
    List<OutputConnection> connections2 = selector.select(test1, testConnections);
    assertEquals(1, connections2.size());
    assertEquals(connections1.get(0), connections2.get(0));

    JsonMessage test2 = DefaultJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test", "a")).build();
    List<OutputConnection> connections3 = selector.select(test2, testConnections);
    assertEquals(1, connections3.size());
    List<OutputConnection> connections4 = selector.select(test2, testConnections);
    assertEquals(1, connections4.size());
    assertEquals(connections3.get(0), connections4.get(0));

    JsonMessage test3 = DefaultJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test", "a")).build();
    List<OutputConnection> connections5 = selector.select(test3, testConnections);
    assertEquals(1, connections5.size());
    List<OutputConnection> connections6 = selector.select(test3, testConnections);
    assertEquals(1, connections6.size());
    assertEquals(connections5.get(0), connections6.get(0));

    Selector multiSelector = new FieldsSelector("test1", "test2");

    JsonMessage test4 = DefaultJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test1", "a")).build();
    List<OutputConnection> connections7 = multiSelector.select(test4, testConnections);
    assertEquals(1, connections7.size());
    List<OutputConnection> connections8 = multiSelector.select(test4, testConnections);
    assertEquals(1, connections8.size());
    assertEquals(connections7.get(0), connections8.get(0));

    JsonMessage test5 = DefaultJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test2", "ab")).build();
    List<OutputConnection> connections9 = multiSelector.select(test5, testConnections);
    assertEquals(1, connections9.size());
    List<OutputConnection> connections10 = multiSelector.select(test5, testConnections);
    assertEquals(1, connections10.size());
    assertEquals(connections9.get(0), connections10.get(0));

    JsonMessage test6 = DefaultJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test1", "ab").putString("test2", "abc")).build();
    List<OutputConnection> connections11 = multiSelector.select(test6, testConnections);
    assertEquals(1, connections11.size());
    List<OutputConnection> connections12 = multiSelector.select(test6, testConnections);
    assertEquals(1, connections12.size());
    assertEquals(connections11.get(0), connections12.get(0));
  }

}
