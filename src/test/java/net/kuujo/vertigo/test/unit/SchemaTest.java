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

import net.kuujo.vertigo.message.schema.ArraySchema;
import net.kuujo.vertigo.message.schema.ObjectSchema;

import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A schema test.
 *
 * @author Jordan Halterman
 */
public class SchemaTest {

  @Test
  public void testObjectFieldValid() {
    JsonObject json = new JsonObject().putString("foo", "foo").putString("bar", "bar");
    ObjectSchema schema = new ObjectSchema();
    schema.addField("foo").addField("bar");
    assertTrue(schema.getValidator().validate(json));
  }

  @Test
  public void testObjectFieldInvalid() {
    JsonObject json = new JsonObject().putString("foo", "foo").putString("bar", "bar");
    ObjectSchema schema = new ObjectSchema();
    schema.addField("foo").addField("baz");
    assertFalse(schema.getValidator().validate(json));
  }

  @Test
  public void testObjectTypeValid() {
    JsonObject json = new JsonObject().putString("foo", "foo").putNumber("bar", 1);
    ObjectSchema schema = new ObjectSchema();
    schema.addField("foo", String.class).addField("bar", Integer.class);
    assertTrue(schema.getValidator().validate(json));
  }

  @Test
  public void testObjectTypeInvalid() {
    JsonObject json = new JsonObject().putString("foo", "foo").putNumber("bar", 1);
    ObjectSchema schema = new ObjectSchema();
    schema.addField("foo", String.class).addField("bar", String.class);
    assertFalse(schema.getValidator().validate(json));
  }

  @Test
  public void testObjectSchemaValid() {
    JsonObject json = new JsonObject().putString("foo", "foo").putNumber("bar", 1);
    json.putObject("baz", new JsonObject().putString("bar", "bar").putNumber("foo", 2));
    ObjectSchema schema = new ObjectSchema();
    schema.addField("foo", String.class).addField("bar", Integer.class);
    schema.addField("baz", new ObjectSchema().addField("bar", String.class).addField("foo", Integer.class));
    assertTrue(schema.getValidator().validate(json));
  }

  @Test
  public void testObjectSchemaInvalid() {
    JsonObject json = new JsonObject().putString("foo", "foo").putNumber("bar", 1);
    json.putObject("baz", new JsonObject().putString("bar", "bar").putNumber("foo", 2));
    ObjectSchema schema = new ObjectSchema();
    schema.addField("foo", String.class).addField("bar", Integer.class);
    schema.addField("baz", new ObjectSchema().addField("bar", String.class).addField("baz", Integer.class));
    assertFalse(schema.getValidator().validate(json));
  }

  @Test
  public void testArrayTypeValid() {
    JsonArray json = new JsonArray().add("foo").add("bar");
    ArraySchema schema = new ArraySchema().setElementType(String.class);
    assertTrue(schema.getValidator().validate(json));
  }

  @Test
  public void testArrayTypeInvalid() {
    JsonArray json = new JsonArray().add("foo").add("bar");
    ArraySchema schema = new ArraySchema().setElementType(Integer.class);
    assertTrue(schema.getValidator().validate(json));
  }

  @Test
  public void testArraySchemaValid() {
    JsonArray json = new JsonArray();
    json.add(new JsonObject().putString("foo", "bar"));
    json.add(new JsonObject().putString("foo", "baz"));
    ArraySchema schema = new ArraySchema().setElementType(JsonObject.class);
    schema.setElementSchema(new ObjectSchema().addField("foo", String.class));
    assertTrue(schema.getValidator().validate(json));
  }

  @Test
  public void testArraySchemaInvalid() {
    JsonArray json = new JsonArray();
    json.add(new JsonObject().putString("foo", "bar"));
    json.add(new JsonObject().putString("foo", "baz"));
    ArraySchema schema = new ArraySchema().setElementType(JsonObject.class);
    schema.setElementSchema(new ObjectSchema().addField("foo", Integer.class));
    assertFalse(schema.getValidator().validate(json));
  }

}
