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

import net.kuujo.vertigo.messaging.DefaultJsonMessage;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

/**
 * Message tests.
 *
 * @author Jordan Halterman
 */
public class MessageTest {

  @Test
  public void testCreateMessage() {
    JsonMessage message = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"));
    assertEquals("Hello world!", message.body().getString("body"));
    assertNotNull(message.id());
    assertNull(message.parent());
    assertNull(message.ancestor());
    assertNull(message.source());
    assertNull(message.tag());
  }

  @Test
  public void testCreateMessageWithTag() {
    JsonMessage message = DefaultJsonMessage.create(new JsonObject().putString("body", "Hello world!"), "test");
    assertEquals("Hello world!", message.body().getString("body"));
    assertNotNull(message.id());
    assertNull(message.parent());
    assertNull(message.ancestor());
    assertNull(message.source());
    assertEquals("test", message.tag());
  }

  @Test
  public void testCreateMessageWithSource() {
    JsonMessage message = DefaultJsonMessage.create("test", new JsonObject().putString("body", "Hello world!"));
    assertEquals("Hello world!", message.body().getString("body"));
    assertNotNull(message.id());
    assertNull(message.parent());
    assertNull(message.ancestor());
    assertEquals("test", message.source());
    assertNull(message.tag());
  }

  @Test
  public void testCreateMessageWithSourceAndTag() {
    JsonMessage message = DefaultJsonMessage.create("test", new JsonObject().putString("body", "Hello world!"), "foo");
    assertEquals("Hello world!", message.body().getString("body"));
    assertNotNull(message.id());
    assertNull(message.parent());
    assertNull(message.ancestor());
    assertEquals("test", message.source());
    assertEquals("foo", message.tag());
  }

  @Test
  public void testCopyMessage() {
    JsonMessage message = DefaultJsonMessage.create("test", new JsonObject().putString("body", "Hello world!"), "foo");
    assertEquals("Hello world!", message.body().getString("body"));
    assertNotNull(message.id());
    assertNull(message.parent());
    assertNull(message.ancestor());
    assertEquals("test", message.source());
    assertEquals("foo", message.tag());
    JsonMessage newMessage = message.copy();
    assertEquals("Hello world!", newMessage.body().getString("body"));
    assertNotNull(newMessage.id());
    assertFalse(newMessage.id().equals(message.id()));
    assertNull(newMessage.parent());
    assertNull(newMessage.ancestor());
    assertEquals("test", newMessage.source());
    assertEquals("foo", newMessage.tag());
  }

  @Test
  public void testCreateChild() {
    JsonMessage message = DefaultJsonMessage.create("test", new JsonObject().putString("body", "Hello world!"), "foo");
    assertEquals("Hello world!", message.body().getString("body"));
    assertNotNull(message.id());
    assertNull(message.parent());
    assertNull(message.ancestor());
    assertEquals("test", message.source());
    assertEquals("foo", message.tag());
    JsonMessage child = message.createChild(new JsonObject().putString("body2", "Hello world again!"));
    assertNull(child.body().getString("body"));
    assertEquals("Hello world again!", child.body().getString("body2"));
    assertNotNull(child.id());
    assertFalse(child.id().equals(message.id()));
    assertEquals(message.id(), child.parent());
    assertEquals(message.id(), child.ancestor());
    assertEquals("test", child.source());
    assertEquals("foo", child.tag());
  }

  @Test
  public void testCreateChildWithTag() {
    JsonMessage message = DefaultJsonMessage.create("test", new JsonObject().putString("body", "Hello world!"), "foo");
    assertEquals("Hello world!", message.body().getString("body"));
    assertNotNull(message.id());
    assertNull(message.parent());
    assertNull(message.ancestor());
    assertEquals("test", message.source());
    assertEquals("foo", message.tag());
    JsonMessage child = message.createChild(new JsonObject().putString("body2", "Hello world again!"), "bar");
    assertNull(child.body().getString("body"));
    assertEquals("Hello world again!", child.body().getString("body2"));
    assertNotNull(child.id());
    assertFalse(child.id().equals(message.id()));
    assertEquals(message.id(), child.parent());
    assertEquals(message.id(), child.ancestor());
    assertEquals("test", child.source());
    assertEquals("bar", child.tag());
  }

  @Test
  public void testLoadMessage() {
    JsonMessage message = DefaultJsonMessage.create("test", new JsonObject().putString("body", "Hello world!"), "foo");
    JsonMessage child = message.createChild(new JsonObject().putString("body2", "Hello world again!"), "bar");
    JsonMessage loaded = new DefaultJsonMessage(child.serialize());
    assertNull(loaded.body().getString("body"));
    assertEquals("Hello world again!", loaded.body().getString("body2"));
    assertNotNull(loaded.id());
    assertFalse(loaded.id().equals(message.id()));
    assertEquals(message.id(), loaded.parent());
    assertEquals(message.id(), loaded.ancestor());
    assertEquals("test", loaded.source());
    assertEquals("bar", loaded.tag());
  }

}
