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

import net.kuujo.vertigo.context.ContextBuilder;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.network.MalformedNetworkException;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Definition to context tests.
 *
 * @author Jordan Halterman
 */
public class ContextTest {

  @Test
  public void testNetworkContext() {
    Serializer serializer = Serializer.getInstance();
    Network network = new Network("test");
    network.setNumAuditors(2);
    network.enableAcking();

    network.addVerticle("test1", "net.kuujo.vertigo.VertigoVerticle");
    network.addVerticle("test2", "net.kuujo.vertigo.VertigoVerticle").addInput("test1");

    NetworkContext context;
    try {
      context = ContextBuilder.buildContext(network);
      assertEquals(2, context.getAuditors().size());
      JsonObject serialized = serializer.serialize(context);
      assertNotNull(serialized);
      context = serializer.deserialize(serialized, NetworkContext.class);
    }
    catch (SerializationException | MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testContextFromJson() {
    JsonObject json = new JsonObject();
    json.putString("address", "test");
    JsonObject components = new JsonObject();
    components.putObject("test2", new JsonObject()
      .putString("type", "verticle").putString("main", "net.kuujo.vertigo.input.grouping.RandomGrouping"));
    components.putObject("test2", new JsonObject()
      .putString("type", "verticle").putString("main", "net.kuujo.vertigo.VertigoVerticle")
      .putArray("inputs", new JsonArray().add(new JsonObject().putString("address", "test2").putObject("grouping",
          new JsonObject().putString("type", "net.kuujo.vertigo.input.grouping.RoundGrouping")))));

    try {
      Network network = Network.fromJson(json);
      ContextBuilder.buildContext(network);
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

}
