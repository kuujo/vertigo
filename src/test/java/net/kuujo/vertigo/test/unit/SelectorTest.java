/*
 * Copyright 2013-2014 the original author or authors.
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
import net.kuujo.vertigo.message.impl.ReliableJsonMessage;
import net.kuujo.vertigo.output.selector.AllSelector;
import net.kuujo.vertigo.output.selector.FieldsSelector;
import net.kuujo.vertigo.output.selector.RandomSelector;
import net.kuujo.vertigo.output.selector.RoundSelector;
import net.kuujo.vertigo.output.selector.MessageSelector;

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
  private List<String> testConnections = new ArrayList<String>() {{
    add("foo");
    add("bar");
    add("baz");
  }};

  private JsonMessage testMessage = ReliableJsonMessage.Builder.newBuilder()
      .setBody(new JsonObject().putString("body", "Hello world!")).build();

  @Test
  public void testAllSelector() {
    MessageSelector selector = new AllSelector();
    for (String outputConnection : selector.select(testMessage, testConnections)) {
      assertTrue(testConnections.contains(outputConnection));
    }
  }

  @Test
  public void testRandomSelector() {
    MessageSelector selector = new RandomSelector();
    Set<String> selected = new HashSet<>();
    while (selected.size() < testConnections.size()) {
      List<String> outputConnections = selector.select(testMessage, testConnections);
      assertEquals(1, outputConnections.size());
      String outputConnection = outputConnections.get(0);
      if (!selected.contains(outputConnection)) {
        selected.add(outputConnection);
      }
    }
  }

  @Test
  public void testRoundSelector() {
    MessageSelector selector = new RoundSelector();
    for (int i = 0; i < 5; i++) {
      int expected = 0;
      for (int j = 0; j < testConnections.size(); j++) {
        List<String> outputConnections = selector.select(testMessage, testConnections);
        assertEquals(1, outputConnections.size());
        String outputConnection = outputConnections.get(0);
        assertEquals(testConnections.get(expected), outputConnection);
        expected++;
      }
    }
  }

  @Test
  public void testFieldsSelector() {
    MessageSelector selector = new FieldsSelector("test");

    JsonMessage test1 = ReliableJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test", "a")).build();
    List<String> connections1 = selector.select(test1, testConnections);
    assertEquals(1, connections1.size());
    List<String> connections2 = selector.select(test1, testConnections);
    assertEquals(1, connections2.size());
    assertEquals(connections1.get(0), connections2.get(0));

    JsonMessage test2 = ReliableJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test", "a")).build();
    List<String> connections3 = selector.select(test2, testConnections);
    assertEquals(1, connections3.size());
    List<String> connections4 = selector.select(test2, testConnections);
    assertEquals(1, connections4.size());
    assertEquals(connections3.get(0), connections4.get(0));

    JsonMessage test3 = ReliableJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test", "a")).build();
    List<String> connections5 = selector.select(test3, testConnections);
    assertEquals(1, connections5.size());
    List<String> connections6 = selector.select(test3, testConnections);
    assertEquals(1, connections6.size());
    assertEquals(connections5.get(0), connections6.get(0));

    MessageSelector multiSelector = new FieldsSelector("test1", "test2");

    JsonMessage test4 = ReliableJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test1", "a")).build();
    List<String> connections7 = multiSelector.select(test4, testConnections);
    assertEquals(1, connections7.size());
    List<String> connections8 = multiSelector.select(test4, testConnections);
    assertEquals(1, connections8.size());
    assertEquals(connections7.get(0), connections8.get(0));

    JsonMessage test5 = ReliableJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test2", "ab")).build();
    List<String> connections9 = multiSelector.select(test5, testConnections);
    assertEquals(1, connections9.size());
    List<String> connections10 = multiSelector.select(test5, testConnections);
    assertEquals(1, connections10.size());
    assertEquals(connections9.get(0), connections10.get(0));

    JsonMessage test6 = ReliableJsonMessage.Builder.newBuilder().setBody(new JsonObject().putString("test1", "ab").putString("test2", "abc")).build();
    List<String> connections11 = multiSelector.select(test6, testConnections);
    assertEquals(1, connections11.size());
    List<String> connections12 = multiSelector.select(test6, testConnections);
    assertEquals(1, connections12.size());
    assertEquals(connections11.get(0), connections12.get(0));
  }

}
