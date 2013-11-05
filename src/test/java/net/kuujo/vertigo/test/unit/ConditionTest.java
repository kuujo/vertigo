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

import net.kuujo.vertigo.message.DefaultJsonMessage;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.output.condition.Condition;
import net.kuujo.vertigo.output.condition.FieldCondition;
import net.kuujo.vertigo.output.condition.SourceCondition;
import net.kuujo.vertigo.output.condition.TagsCondition;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Filter tests.
 *
 * @author Jordan Halterman
 */
public class ConditionTest {

  @Test
  public void testTagsCondition() {
    Condition condition = new TagsCondition("foo", "bar");

    JsonMessage message1 = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "auditor");
    assertFalse(condition.isValid(message1));

    JsonMessage message2 = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "baz", "auditor");
    assertFalse(condition.isValid(message2));

    JsonMessage message3 = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "foo", "auditor");
    assertTrue(condition.isValid(message3));

    JsonMessage message4 = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "bar", "auditor");
    assertTrue(condition.isValid(message4));
  }

  @Test
  public void testFieldCondition() {
    Condition condition = new FieldCondition("foo", "bar");

    JsonMessage message1 = DefaultJsonMessage.create(new JsonObject().putString("bar", "foo"), "auditor");
    assertFalse(condition.isValid(message1));

    JsonMessage message2 = DefaultJsonMessage.create(new JsonObject().putString("foo", "baz"), "auditor");
    assertFalse(condition.isValid(message2));

    JsonMessage message3 = DefaultJsonMessage.create(new JsonObject().putString("foo", "bar"), "auditor");
    assertTrue(condition.isValid(message3));
  }

  @Test
  public void testSourceCondition() {
    Condition condition = new SourceCondition("foo");

    JsonMessage message1 = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "auditor");
    assertFalse(condition.isValid(message1));

    JsonMessage message2 = DefaultJsonMessage.create("bar", new JsonObject().putString("body", "Hello world!"), "auditor");
    assertFalse(condition.isValid(message2));

    JsonMessage message3 = DefaultJsonMessage.create("foo", new JsonObject().putString("body", "Hello world!"), "auditor");
    assertTrue(condition.isValid(message3));
  }

}
