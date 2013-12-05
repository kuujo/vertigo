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

import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.ContextBuilder;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.hooks.EventBusHook;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.network.MalformedNetworkException;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;
import net.kuujo.vertigo.serializer.Serializers;
import net.kuujo.vertigo.worker.Worker;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * A serializer test.
 *
 * @author Jordan Halterman
 */
public class SerializerTest {

  @Test
  public void testSerializeNetworkContext() {
    Network network = new Network("test");
    network.setNumAuditors(2);
    network.setAckTimeout(10000);
    network.addFeeder("1", "1.py", 2).addHook(new EventBusHook());
    network.addWorker("2", "2.py", 2).addInput("1").groupBy(new RoundGrouping());

    try {
      NetworkContext context = ContextBuilder.buildContext(network);
      JsonObject toJsonNetwork = NetworkContext.toJson(context);
      NetworkContext fromJsonNetwork = NetworkContext.fromJson(toJsonNetwork);
      assertEquals("test", fromJsonNetwork.getAddress());

      ComponentContext<Feeder> component = context.getComponent("1");
      JsonObject toJsonComponent = ComponentContext.toJson(component);
      ComponentContext<Feeder> fromJsonComponent = ComponentContext.fromJson(toJsonComponent);
      assertEquals("test", fromJsonComponent.getNetwork().getAddress());

      InstanceContext<Feeder> instance = component.getInstances().get(0);
      JsonObject toJsonInstance = InstanceContext.toJson(instance);
      InstanceContext<Feeder> fromJsonInstance = InstanceContext.fromJson(toJsonInstance);
      assertEquals("test", fromJsonInstance.getComponent().getNetwork().getAddress());
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testSerializeNetwork() {
    Network network = new Network("test");
    network.setNumAuditors(2);
    network.setAckTimeout(10000);
    network.addFeeder("1", "1.py", 2).addHook(new EventBusHook());
    network.addWorker("2", "2.py", 2).addInput("1").groupBy(new RoundGrouping());

    Serializer serializer = Serializers.getDefault();
    try {
      JsonObject serialized = serializer.serialize(network);
      Network deserialized = serializer.deserialize(serialized, Network.class);
      assertTrue(deserialized.isAckingEnabled());
      assertEquals(10000, deserialized.getAckTimeout());
      assertEquals("test", deserialized.getAddress());
    }
    catch (SerializationException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testNetworkToContext() {
    Network network = new Network("test");
    network.setNumAuditors(2);
    network.setAckTimeout(10000);
    network.addFeeder("1", "1.py", 2).setConfig(new JsonObject().putString("foo", "bar")).addHook(new EventBusHook());
    network.addWorker("2", "2.py", 2).addInput("1").groupBy(new RoundGrouping());

    try {
      NetworkContext context = ContextBuilder.buildContext(network);
      assertTrue(context.isAckingEnabled());
      assertEquals(10000, context.getAckTimeout());
      assertEquals("test", context.getAddress());
      assertEquals(2, context.getAuditors().size());
      ComponentContext<Worker> component = context.getComponent("2");
      assertTrue(component.getType().equals(Worker.class));
      assertNotNull(component);
      assertEquals("2", component.getAddress());
      Input input = component.getInputs().get(0);
      assertNotNull(input);
      assertTrue(input.getGrouping() instanceof RoundGrouping);
      ComponentContext<Feeder> component2 = context.getComponent("1");
      assertTrue(component2.getType().equals(Feeder.class));
      ComponentHook hook = component2.getHooks().get(0);
      assertNotNull(hook);
      assertTrue(hook instanceof EventBusHook);
      InstanceContext<Feeder> instance = component2.getInstances().get(0);
      assertNotNull(instance);
      assertNotNull(instance.id());

      Serializer serializer = Serializers.getDefault();
      JsonObject serialized = serializer.serialize(context);
      serializer.deserialize(serialized, NetworkContext.class);
    }
    catch (MalformedNetworkException | SerializationException e) {
      fail(e.getMessage());
    }
  }

}
