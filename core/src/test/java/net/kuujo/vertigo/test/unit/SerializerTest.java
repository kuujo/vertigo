/*
 * Copyright 2014 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.kuujo.vertigo.util.serialization.JsonSerializable;
import net.kuujo.vertigo.util.serialization.SerializationException;
import net.kuujo.vertigo.util.serialization.Serializer;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.junit.Test;

/**
 * Selector tests.
 *
 * @author Jordan Halterman
 */
public class SerializerTest {

  public static interface SomeInterface extends JsonSerializable {}

  public static interface SomeExtendingInterface extends SomeInterface{}

  public static interface SomeOtherInterface {}

  public static interface SomeOtherExtendingInterface extends SomeOtherInterface {}

  public static class SomeObject implements SomeExtendingInterface, SomeOtherInterface {}

  public static class SomeExtendingObject extends SomeObject {}

  @Test
  public void testFindSerializer() {
    Serializer serializer1 = SerializerFactory.getSerializer(SomeExtendingObject.class);
    Serializer serializer2 = SerializerFactory.getSerializer(SomeObject.class);
    assertEquals(serializer1, serializer2);
    Serializer serializer3 = SerializerFactory.getSerializer(SomeExtendingInterface.class);
    assertEquals(serializer1, serializer3);
  }

  @Test
  public void testFindSerializerFail() {
    try {
      SerializerFactory.getSerializer(SomeOtherInterface.class);
      fail("Returned invalid serializer.");
    } catch (SerializationException e) {
    }
  }

}
