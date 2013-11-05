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

import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * A serializer test.
 *
 * @author Jordan Halterman
 */
public class SerializerTest {

  public static class TestSerializable implements Serializable {
    private JsonObject data;
    public TestSerializable() {
      data = new JsonObject();
    }
    public TestSerializable(JsonObject data) {
      this.data = data;
    }
    public <T> T getField(String name) {
      return data.<T>getValue(name);
    }
    @Override
    public JsonObject getState() {
      return data.copy();
    }
    @Override
    public void setState(JsonObject state) {
      data = state.copy();
    }
  }

  @Test
  public void testSerialize() {
    TestSerializable serializable = new TestSerializable(new JsonObject().putString("foo", "bar"));
    JsonObject serialized = Serializer.serialize(serializable);
    assertNotNull(serialized.getString("type"));
    try {
      TestSerializable unserialized = Serializer.deserialize(serialized);
      assertTrue(unserialized instanceof TestSerializable);
      assertEquals("bar", unserialized.getField("foo"));
    }
    catch (SerializationException e) {
      fail(e.getMessage());
    }
  }

}
