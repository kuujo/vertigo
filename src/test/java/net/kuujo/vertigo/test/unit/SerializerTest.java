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
    assertNotNull(serialized.getString("class"));
    assertNotNull(serialized.getObject("state"));
    try {
      TestSerializable unserialized = Serializer.unserialize(serialized);
      assertTrue(unserialized instanceof TestSerializable);
      assertEquals("bar", unserialized.getField("foo"));
    }
    catch (SerializationException e) {
      fail(e.getMessage());
    }
  }

}
