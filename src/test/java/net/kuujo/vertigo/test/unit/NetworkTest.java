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

import java.util.List;

import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.grouping.AllGrouping;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Input;
import net.kuujo.vertigo.network.Module;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Verticle;
import net.kuujo.vertigo.rpc.Executor;
import net.kuujo.vertigo.worker.Worker;

import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Network configuration tests.
 *
 * @author Jordan Halterman
 */
public class NetworkTest {

  @Test
  public void testNetworkDefaults() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    assertEquals(true, network.isAckingEnabled());
    assertEquals(1, network.getNumAuditors());
    assertEquals(true, network.isMessageTimeoutsEnabled());
    assertEquals(30000, network.getMessageTimeout());
  }

  @Test
  public void testNetworkConfig() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    network.enableAcking();
    assertTrue(network.isAckingEnabled());
    network.disableAcking();
    assertFalse(network.isAckingEnabled());
    network.setAckingEnabled(true);
    assertTrue(network.isAckingEnabled());
    network.setNumAuditors(2);
    assertEquals(2, network.getNumAuditors());
    network.enableMessageTimeouts();
    assertTrue(network.isMessageTimeoutsEnabled());
    network.disableMessageTimeouts();
    assertFalse(network.isMessageTimeoutsEnabled());
    network.setMessageTimeoutsEnabled(true);
    assertTrue(network.isMessageTimeoutsEnabled());
    network.setMessageTimeout(10000);
    assertEquals(10000, network.getMessageTimeout());
  }

  @Test
  public void testVerticleDefaults() {
    Verticle<Feeder> verticle = new Verticle<Feeder>(Feeder.class, "test", "test.py");
    assertEquals("test", verticle.getAddress());
    assertEquals(Feeder.class, verticle.getType());
    assertFalse(verticle.isModule());
    assertTrue(verticle.isVerticle());
    assertEquals("test.py", verticle.getMain());
    assertEquals(new JsonObject(), verticle.getConfig());
    assertEquals(1, verticle.getNumInstances());
    assertEquals(5000, verticle.getHeartbeatInterval());
    assertFalse(verticle.isWorker());
    assertFalse(verticle.isMultiThreaded());
    assertEquals(0, verticle.getHooks().size());
    assertEquals(0, verticle.getInputs().size());
  }

  @Test
  public void testVerticleConfig() {
    Verticle<Feeder> verticle = new Verticle<Feeder>(Feeder.class, "test", "test.py");
    assertEquals("test", verticle.getAddress());
    assertEquals(Feeder.class, verticle.getType());
    assertFalse(verticle.isModule());
    assertTrue(verticle.isVerticle());
    assertEquals("test.py", verticle.getMain());
    verticle.setMain("foo.js");
    assertEquals("foo.js", verticle.getMain());
    assertEquals(new JsonObject(), verticle.getConfig());
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(1, verticle.getNumInstances());
    verticle.setNumInstances(4);
    assertEquals(4, verticle.getNumInstances());
    assertEquals(5000, verticle.getHeartbeatInterval());
    verticle.setHeartbeatInterval(1000);
    assertEquals(1000, verticle.getHeartbeatInterval());
    assertFalse(verticle.isWorker());
    verticle.setWorker(true);
    assertTrue(verticle.isWorker());
    assertFalse(verticle.isMultiThreaded());
    verticle.setMultiThreaded(true);
    assertTrue(verticle.isMultiThreaded());
    verticle.setWorker(false);
    assertFalse(verticle.isMultiThreaded());
  }

  @Test
  public void testAddInvalidModule() {
    Network network = new Network("test");
    try {
      network.addFeederModule("feeder", "feeder.py");
      fail();
    }
    catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testModuleDefaults() {
    Module<Feeder> verticle = new Module<Feeder>(Feeder.class, "test", "com.test~test-module~1.0");
    assertEquals("test", verticle.getAddress());
    assertEquals(Feeder.class, verticle.getType());
    assertTrue(verticle.isModule());
    assertFalse(verticle.isVerticle());
    assertEquals("com.test~test-module~1.0", verticle.getModule());
    assertEquals(new JsonObject(), verticle.getConfig());
    assertEquals(1, verticle.getNumInstances());
    assertEquals(5000, verticle.getHeartbeatInterval());
    assertEquals(0, verticle.getHooks().size());
    assertEquals(0, verticle.getInputs().size());
  }

  @Test
  public void testModuleConfig() {
    Module<Feeder> verticle = new Module<Feeder>(Feeder.class, "test", "com.test~test-module~1.0");
    assertEquals("test", verticle.getAddress());
    assertEquals(Feeder.class, verticle.getType());
    assertTrue(verticle.isModule());
    assertFalse(verticle.isVerticle());
    assertEquals("com.test~test-module~1.0", verticle.getModule());
    verticle.setModule("com.foo~foo~1.0");
    assertEquals("com.foo~foo~1.0", verticle.getModule());
    assertEquals(new JsonObject(), verticle.getConfig());
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(1, verticle.getNumInstances());
    verticle.setNumInstances(4);
    assertEquals(4, verticle.getNumInstances());
    assertEquals(5000, verticle.getHeartbeatInterval());
    verticle.setHeartbeatInterval(1000);
    assertEquals(1000, verticle.getHeartbeatInterval());
  }

  @Test
  public void testInputDefaults() {
    Input input = new Input("test");
    assertEquals("test", input.getAddress());
    assertEquals("default", input.getStream());
    assertTrue(input.getGrouping() instanceof RoundGrouping);
  }

  @Test
  public void testInputConfig() {
    Input input = new Input("test");
    assertEquals("test", input.getAddress());
    assertEquals("default", input.getStream());
    input.setStream("nondefault");
    assertEquals("nondefault", input.getStream());
    assertTrue(input.getGrouping() instanceof RoundGrouping);
    input.randomGrouping();
    assertTrue(input.getGrouping() instanceof RandomGrouping);
    input.roundGrouping();
    assertTrue(input.getGrouping() instanceof RoundGrouping);
    input.fieldsGrouping("foo", "bar");
    assertTrue(input.getGrouping() instanceof FieldsGrouping);
    input.allGrouping();
    assertTrue(input.getGrouping() instanceof AllGrouping);
  }

  @Test
  public void testAddInput() {
    Verticle<Worker> worker = new Verticle<Worker>(Worker.class, "worker", "worker.py");
    Verticle<Feeder> feeder = new Verticle<Feeder>(Feeder.class, "feeder", "feeder.py");
    assertEquals(0, worker.getInputs().size());
    Input input1 = worker.addInput(feeder);
    assertEquals("feeder", input1.getAddress());
    assertEquals("default", input1.getStream());
    assertTrue(input1.getGrouping() instanceof RoundGrouping);
    Input input2 = worker.addInput(feeder, "nondefault");
    assertEquals("feeder", input2.getAddress());
    assertEquals("nondefault", input2.getStream());
    assertTrue(input2.getGrouping() instanceof RoundGrouping);
    Input input3 = worker.addInput(feeder, new RandomGrouping());
    assertEquals("feeder", input3.getAddress());
    assertEquals("default", input3.getStream());
    assertTrue(input3.getGrouping() instanceof RandomGrouping);
    Input input4 = worker.addInput(feeder, "nondefault", new RandomGrouping());
    assertEquals("feeder", input4.getAddress());
    assertEquals("nondefault", input4.getStream());
    assertTrue(input4.getGrouping() instanceof RandomGrouping);
    Input input5 = worker.addInput("feeder");
    assertEquals("feeder", input5.getAddress());
    assertEquals("default", input5.getStream());
    assertTrue(input5.getGrouping() instanceof RoundGrouping);
    Input input6 = worker.addInput("feeder", "nondefault");
    assertEquals("feeder", input6.getAddress());
    assertEquals("nondefault", input6.getStream());
    assertTrue(input6.getGrouping() instanceof RoundGrouping);
    Input input7 = worker.addInput("feeder", new RandomGrouping());
    assertEquals("feeder", input7.getAddress());
    assertEquals("default", input7.getStream());
    assertTrue(input7.getGrouping() instanceof RandomGrouping);
    Input input8 = worker.addInput("feeder", "nondefault", new RandomGrouping());
    assertEquals("feeder", input8.getAddress());
    assertEquals("nondefault", input8.getStream());
    assertTrue(input8.getGrouping() instanceof RandomGrouping);
  }

  @Test
  public void testAddFeeder() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Component<Feeder> verticle1 = network.addFeeder("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getAddress());
    assertFalse(verticle1.isModule());
    assertTrue(verticle1.isVerticle());
    assertEquals("verticle1.py", ((Verticle<Feeder>) verticle1).getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getNumInstances());
    Component<Feeder> verticle2 = network.addFeeder("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getAddress());
    assertFalse(verticle2.isModule());
    assertTrue(verticle2.isVerticle());
    assertEquals("verticle2.py", ((Verticle<Feeder>) verticle2).getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getNumInstances());
    Component<Feeder> verticle3 = network.addFeeder("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getAddress());
    assertFalse(verticle3.isModule());
    assertTrue(verticle3.isVerticle());
    assertEquals("verticle3.py", ((Verticle<Feeder>) verticle3).getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getNumInstances());
    Component<Feeder> verticle4 = network.addFeeder("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getAddress());
    assertFalse(verticle4.isModule());
    assertTrue(verticle4.isVerticle());
    assertEquals("verticle4.py", ((Verticle<Feeder>) verticle4).getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getNumInstances());
    Component<Feeder> module1 = network.addFeeder("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getAddress());
    assertTrue(module1.isModule());
    assertFalse(module1.isVerticle());
    assertEquals("com.test~test-module~1.0", ((Module<Feeder>) module1).getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getNumInstances());
    Component<Feeder> module2 = network.addFeeder("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getAddress());
    assertTrue(module2.isModule());
    assertFalse(module2.isVerticle());
    assertEquals("com.test~test-module~2.0", ((Module<Feeder>) module2).getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getNumInstances());
    Component<Feeder> module3 = network.addFeeder("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getAddress());
    assertTrue(module3.isModule());
    assertFalse(module3.isVerticle());
    assertEquals("com.test~test-module~3.0", ((Module<Feeder>) module3).getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getNumInstances());
    Component<Feeder> module4 = network.addFeeder("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getAddress());
    assertTrue(module4.isModule());
    assertFalse(module4.isVerticle());
    assertEquals("com.test~test-module~4.0", ((Module<Feeder>) module4).getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getNumInstances());
  }

  @Test
  public void testAddFeederVerticle() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Verticle<Feeder> verticle1 = network.addFeederVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getAddress());
    assertFalse(verticle1.isModule());
    assertTrue(verticle1.isVerticle());
    assertEquals("verticle1.py", verticle1.getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getNumInstances());
    Verticle<Feeder> verticle2 = network.addFeederVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getAddress());
    assertFalse(verticle2.isModule());
    assertTrue(verticle2.isVerticle());
    assertEquals("verticle2.py", verticle2.getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getNumInstances());
    Verticle<Feeder> verticle3 = network.addFeederVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getAddress());
    assertFalse(verticle3.isModule());
    assertTrue(verticle3.isVerticle());
    assertEquals("verticle3.py", verticle3.getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getNumInstances());
    Verticle<Feeder> verticle4 = network.addFeederVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getAddress());
    assertFalse(verticle4.isModule());
    assertTrue(verticle4.isVerticle());
    assertEquals("verticle4.py", verticle4.getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getNumInstances());
  }

  @Test
  public void testAddFeederModule() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Module<Feeder> module1 = network.addFeederModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getAddress());
    assertTrue(module1.isModule());
    assertFalse(module1.isVerticle());
    assertEquals("com.test~test-module~1.0", module1.getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getNumInstances());
    Module<Feeder> module2 = network.addFeederModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getAddress());
    assertTrue(module2.isModule());
    assertFalse(module2.isVerticle());
    assertEquals("com.test~test-module~2.0", module2.getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getNumInstances());
    Module<Feeder> module3 = network.addFeederModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getAddress());
    assertTrue(module3.isModule());
    assertFalse(module3.isVerticle());
    assertEquals("com.test~test-module~3.0", module3.getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getNumInstances());
    Module<Feeder> module4 = network.addFeederModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getAddress());
    assertTrue(module4.isModule());
    assertFalse(module4.isVerticle());
    assertEquals("com.test~test-module~4.0", module4.getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getNumInstances());
  }

  @Test
  public void testAddWorker() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Component<Worker> verticle1 = network.addWorker("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getAddress());
    assertFalse(verticle1.isModule());
    assertTrue(verticle1.isVerticle());
    assertEquals("verticle1.py", ((Verticle<Worker>) verticle1).getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getNumInstances());
    Component<Worker> verticle2 = network.addWorker("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getAddress());
    assertFalse(verticle2.isModule());
    assertTrue(verticle2.isVerticle());
    assertEquals("verticle2.py", ((Verticle<Worker>) verticle2).getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getNumInstances());
    Component<Worker> verticle3 = network.addWorker("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getAddress());
    assertFalse(verticle3.isModule());
    assertTrue(verticle3.isVerticle());
    assertEquals("verticle3.py", ((Verticle<Worker>) verticle3).getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getNumInstances());
    Component<Worker> verticle4 = network.addWorker("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getAddress());
    assertFalse(verticle4.isModule());
    assertTrue(verticle4.isVerticle());
    assertEquals("verticle4.py", ((Verticle<Worker>) verticle4).getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getNumInstances());
    Component<Worker> module1 = network.addWorker("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getAddress());
    assertTrue(module1.isModule());
    assertFalse(module1.isVerticle());
    assertEquals("com.test~test-module~1.0", ((Module<Worker>) module1).getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getNumInstances());
    Component<Worker> module2 = network.addWorker("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getAddress());
    assertTrue(module2.isModule());
    assertFalse(module2.isVerticle());
    assertEquals("com.test~test-module~2.0", ((Module<Worker>) module2).getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getNumInstances());
    Component<Worker> module3 = network.addWorker("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getAddress());
    assertTrue(module3.isModule());
    assertFalse(module3.isVerticle());
    assertEquals("com.test~test-module~3.0", ((Module<Worker>) module3).getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getNumInstances());
    Component<Worker> module4 = network.addWorker("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getAddress());
    assertTrue(module4.isModule());
    assertFalse(module4.isVerticle());
    assertEquals("com.test~test-module~4.0", ((Module<Worker>) module4).getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getNumInstances());
  }

  @Test
  public void testAddWorkerVerticle() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Verticle<Worker> verticle1 = network.addWorkerVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getAddress());
    assertFalse(verticle1.isModule());
    assertTrue(verticle1.isVerticle());
    assertEquals("verticle1.py", verticle1.getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getNumInstances());
    Verticle<Worker> verticle2 = network.addWorkerVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getAddress());
    assertFalse(verticle2.isModule());
    assertTrue(verticle2.isVerticle());
    assertEquals("verticle2.py", verticle2.getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getNumInstances());
    Verticle<Worker> verticle3 = network.addWorkerVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getAddress());
    assertFalse(verticle3.isModule());
    assertTrue(verticle3.isVerticle());
    assertEquals("verticle3.py", verticle3.getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getNumInstances());
    Verticle<Worker> verticle4 = network.addWorkerVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getAddress());
    assertFalse(verticle4.isModule());
    assertTrue(verticle4.isVerticle());
    assertEquals("verticle4.py", verticle4.getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getNumInstances());
  }

  @Test
  public void testAddWorkerModule() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Module<Worker> module1 = network.addWorkerModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getAddress());
    assertTrue(module1.isModule());
    assertFalse(module1.isVerticle());
    assertEquals("com.test~test-module~1.0", module1.getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getNumInstances());
    Module<Worker> module2 = network.addWorkerModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getAddress());
    assertTrue(module2.isModule());
    assertFalse(module2.isVerticle());
    assertEquals("com.test~test-module~2.0", module2.getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getNumInstances());
    Module<Worker> module3 = network.addWorkerModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getAddress());
    assertTrue(module3.isModule());
    assertFalse(module3.isVerticle());
    assertEquals("com.test~test-module~3.0", module3.getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getNumInstances());
    Module<Worker> module4 = network.addWorkerModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getAddress());
    assertTrue(module4.isModule());
    assertFalse(module4.isVerticle());
    assertEquals("com.test~test-module~4.0", module4.getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getNumInstances());
  }

  @Test
  public void testAddExecutor() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Component<Executor> verticle1 = network.addExecutor("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getAddress());
    assertFalse(verticle1.isModule());
    assertTrue(verticle1.isVerticle());
    assertEquals("verticle1.py", ((Verticle<Executor>) verticle1).getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getNumInstances());
    Component<Executor> verticle2 = network.addExecutor("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getAddress());
    assertFalse(verticle2.isModule());
    assertTrue(verticle2.isVerticle());
    assertEquals("verticle2.py", ((Verticle<Executor>) verticle2).getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getNumInstances());
    Component<Executor> verticle3 = network.addExecutor("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getAddress());
    assertFalse(verticle3.isModule());
    assertTrue(verticle3.isVerticle());
    assertEquals("verticle3.py", ((Verticle<Executor>) verticle3).getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getNumInstances());
    Component<Executor> verticle4 = network.addExecutor("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getAddress());
    assertFalse(verticle4.isModule());
    assertTrue(verticle4.isVerticle());
    assertEquals("verticle4.py", ((Verticle<Executor>) verticle4).getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getNumInstances());
    Component<Executor> module1 = network.addExecutor("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getAddress());
    assertTrue(module1.isModule());
    assertFalse(module1.isVerticle());
    assertEquals("com.test~test-module~1.0", ((Module<Executor>) module1).getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getNumInstances());
    Component<Executor> module2 = network.addExecutor("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getAddress());
    assertTrue(module2.isModule());
    assertFalse(module2.isVerticle());
    assertEquals("com.test~test-module~2.0", ((Module<Executor>) module2).getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getNumInstances());
    Component<Executor> module3 = network.addExecutor("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getAddress());
    assertTrue(module3.isModule());
    assertFalse(module3.isVerticle());
    assertEquals("com.test~test-module~3.0", ((Module<Executor>) module3).getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getNumInstances());
    Component<Executor> module4 = network.addExecutor("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getAddress());
    assertTrue(module4.isModule());
    assertFalse(module4.isVerticle());
    assertEquals("com.test~test-module~4.0", ((Module<Executor>) module4).getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getNumInstances());
  }

  @Test
  public void testAddExecutorVerticle() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Verticle<Executor> verticle1 = network.addExecutorVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getAddress());
    assertFalse(verticle1.isModule());
    assertTrue(verticle1.isVerticle());
    assertEquals("verticle1.py", verticle1.getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getNumInstances());
    Verticle<Executor> verticle2 = network.addExecutorVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getAddress());
    assertFalse(verticle2.isModule());
    assertTrue(verticle2.isVerticle());
    assertEquals("verticle2.py", verticle2.getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getNumInstances());
    Verticle<Executor> verticle3 = network.addExecutorVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getAddress());
    assertFalse(verticle3.isModule());
    assertTrue(verticle3.isVerticle());
    assertEquals("verticle3.py", verticle3.getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getNumInstances());
    Verticle<Executor> verticle4 = network.addExecutorVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getAddress());
    assertFalse(verticle4.isModule());
    assertTrue(verticle4.isVerticle());
    assertEquals("verticle4.py", verticle4.getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getNumInstances());
  }

  @Test
  public void testAddExecutorModule() {
    Network network = new Network("test");
    assertEquals("test", network.getAddress());
    Module<Executor> module1 = network.addExecutorModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getAddress());
    assertTrue(module1.isModule());
    assertFalse(module1.isVerticle());
    assertEquals("com.test~test-module~1.0", module1.getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getNumInstances());
    Module<Executor> module2 = network.addExecutorModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getAddress());
    assertTrue(module2.isModule());
    assertFalse(module2.isVerticle());
    assertEquals("com.test~test-module~2.0", module2.getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getNumInstances());
    Module<Executor> module3 = network.addExecutorModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getAddress());
    assertTrue(module3.isModule());
    assertFalse(module3.isVerticle());
    assertEquals("com.test~test-module~3.0", module3.getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getNumInstances());
    Module<Executor> module4 = network.addExecutorModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getAddress());
    assertTrue(module4.isModule());
    assertFalse(module4.isVerticle());
    assertEquals("com.test~test-module~4.0", module4.getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getNumInstances());
  }

  @Test
  public void testNetworkFromJson() {
    JsonObject json = new JsonObject()
        .putString(Network.NETWORK_ADDRESS, "test")
        .putBoolean(Network.NETWORK_ACKING_ENABLED, true)
        .putNumber(Network.NETWORK_NUM_AUDITORS, 3)
        .putNumber(Network.NETWORK_MESSAGE_TIMEOUT, 10000)
        .putObject(Network.NETWORK_COMPONENTS, new JsonObject());
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    assertTrue(network.isAckingEnabled());
    assertEquals(3, network.getNumAuditors());
    assertTrue(network.isMessageTimeoutsEnabled());
    assertEquals(10000, network.getMessageTimeout());
  }

  @Test
  public void testNetworkTimeoutsDisabledFromJson() {
    JsonObject json = new JsonObject()
        .putString(Network.NETWORK_ADDRESS, "test")
        .putBoolean(Network.NETWORK_MESSAGE_TIMEOUTS_ENABLED, false);
    Network network = Network.fromJson(json);
    assertFalse(network.isMessageTimeoutsEnabled());
  }

  @Test
  public void testAddFeederModuleFromJson() {
    JsonObject json = new JsonObject().putString(Network.NETWORK_ADDRESS, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(Module.COMPONENT_ADDRESS, "feeder")
        .putString(Module.COMPONENT_TYPE, "feeder")
        .putString(Module.COMPONENT_DEPLOYMENT_METHOD, "module")
        .putString(Module.MODULE_NAME, "com.test~test-module~1.0")
        .putObject(Module.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(Module.COMPONENT_NUM_INSTANCES, 2);
    json.putObject(Network.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    Module<Feeder> module = network.getComponent("feeder");
    assertEquals("feeder", module.getAddress());
    assertEquals(Feeder.class, module.getType());
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals("bar", module.getConfig().getString("foo"));
    assertEquals(2, module.getNumInstances());
    assertFalse(module.isVerticle());
    assertTrue(module.isModule());
    assertEquals(0, module.getHooks().size());
    assertEquals(0, module.getInputs().size());
  }

  @Test
  public void testAddFeederVerticleFromJson() {
    JsonObject json = new JsonObject().putString(Network.NETWORK_ADDRESS, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(Verticle.COMPONENT_ADDRESS, "feeder")
        .putString(Verticle.COMPONENT_TYPE, "feeder")
        .putString(Verticle.COMPONENT_DEPLOYMENT_METHOD, "verticle")
        .putString(Verticle.VERTICLE_MAIN, "test.py")
        .putObject(Verticle.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(Verticle.COMPONENT_NUM_INSTANCES, 2)
        .putBoolean(Verticle.VERTICLE_IS_WORKER, true)
        .putBoolean(Verticle.VERTICLE_IS_MULTI_THREADED, true);
    json.putObject(Network.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    Verticle<Feeder> verticle = network.getComponent("feeder");
    assertEquals("feeder", verticle.getAddress());
    assertEquals(Feeder.class, verticle.getType());
    assertEquals("test.py", verticle.getMain());
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(2, verticle.getNumInstances());
    assertTrue(verticle.isVerticle());
    assertFalse(verticle.isModule());
    assertTrue(verticle.isWorker());
    assertTrue(verticle.isMultiThreaded());
    assertEquals(0, verticle.getHooks().size());
    assertEquals(0, verticle.getInputs().size());
  }

  @Test
  public void testAddWorkerModuleFromJson() {
    JsonObject json = new JsonObject().putString(Network.NETWORK_ADDRESS, "test");
    JsonObject jsonWorker = new JsonObject()
        .putString(Module.COMPONENT_ADDRESS, "worker")
        .putString(Module.COMPONENT_TYPE, "worker")
        .putString(Module.COMPONENT_DEPLOYMENT_METHOD, "module")
        .putString(Module.MODULE_NAME, "com.test~test-module~1.0")
        .putObject(Module.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(Module.COMPONENT_NUM_INSTANCES, 2);
    json.putObject(Network.NETWORK_COMPONENTS, new JsonObject().putObject("worker", jsonWorker));
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    Module<Feeder> module = network.getComponent("worker");
    assertEquals("worker", module.getAddress());
    assertEquals(Worker.class, module.getType());
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals("bar", module.getConfig().getString("foo"));
    assertEquals(2, module.getNumInstances());
    assertFalse(module.isVerticle());
    assertTrue(module.isModule());
    assertEquals(0, module.getHooks().size());
    assertEquals(0, module.getInputs().size());
  }

  @Test
  public void testAddWorkerVerticleFromJson() {
    JsonObject json = new JsonObject().putString(Network.NETWORK_ADDRESS, "test");
    JsonObject jsonWorker = new JsonObject()
        .putString(Verticle.COMPONENT_ADDRESS, "worker")
        .putString(Verticle.COMPONENT_TYPE, "worker")
        .putString(Verticle.COMPONENT_DEPLOYMENT_METHOD, "verticle")
        .putString(Verticle.VERTICLE_MAIN, "test.py")
        .putObject(Verticle.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(Verticle.COMPONENT_NUM_INSTANCES, 2)
        .putBoolean(Verticle.VERTICLE_IS_WORKER, true)
        .putBoolean(Verticle.VERTICLE_IS_MULTI_THREADED, true);
    json.putObject(Network.NETWORK_COMPONENTS, new JsonObject().putObject("worker", jsonWorker));
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    Verticle<Feeder> verticle = network.getComponent("worker");
    assertEquals("worker", verticle.getAddress());
    assertEquals(Worker.class, verticle.getType());
    assertEquals("test.py", verticle.getMain());
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(2, verticle.getNumInstances());
    assertTrue(verticle.isVerticle());
    assertFalse(verticle.isModule());
    assertTrue(verticle.isWorker());
    assertTrue(verticle.isMultiThreaded());
    assertEquals(0, verticle.getHooks().size());
    assertEquals(0, verticle.getInputs().size());
  }

  @Test
  public void testAddExecutorModuleFromJson() {
    JsonObject json = new JsonObject().putString(Network.NETWORK_ADDRESS, "test");
    JsonObject jsonExecutor = new JsonObject()
        .putString(Module.COMPONENT_ADDRESS, "executor")
        .putString(Module.COMPONENT_TYPE, "executor")
        .putString(Module.COMPONENT_DEPLOYMENT_METHOD, "module")
        .putString(Module.MODULE_NAME, "com.test~test-module~1.0")
        .putObject(Module.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(Module.COMPONENT_NUM_INSTANCES, 2);
    json.putObject(Network.NETWORK_COMPONENTS, new JsonObject().putObject("executor", jsonExecutor));
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    Module<Feeder> module = network.getComponent("executor");
    assertEquals("executor", module.getAddress());
    assertEquals(Executor.class, module.getType());
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals("bar", module.getConfig().getString("foo"));
    assertEquals(2, module.getNumInstances());
    assertFalse(module.isVerticle());
    assertTrue(module.isModule());
    assertEquals(0, module.getHooks().size());
    assertEquals(0, module.getInputs().size());
  }

  @Test
  public void testAddExecutorVerticleFromJson() {
    JsonObject json = new JsonObject().putString(Network.NETWORK_ADDRESS, "test");
    JsonObject jsonExecutor = new JsonObject()
        .putString(Verticle.COMPONENT_ADDRESS, "executor")
        .putString(Verticle.COMPONENT_TYPE, "executor")
        .putString(Verticle.COMPONENT_DEPLOYMENT_METHOD, "verticle")
        .putString(Verticle.VERTICLE_MAIN, "test.py")
        .putObject(Verticle.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(Verticle.COMPONENT_NUM_INSTANCES, 2)
        .putBoolean(Verticle.VERTICLE_IS_WORKER, true)
        .putBoolean(Verticle.VERTICLE_IS_MULTI_THREADED, true);
    json.putObject(Network.NETWORK_COMPONENTS, new JsonObject().putObject("executor", jsonExecutor));
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    Verticle<Feeder> verticle = network.getComponent("executor");
    assertEquals("executor", verticle.getAddress());
    assertEquals(Executor.class, verticle.getType());
    assertEquals("test.py", verticle.getMain());
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(2, verticle.getNumInstances());
    assertTrue(verticle.isVerticle());
    assertFalse(verticle.isModule());
    assertTrue(verticle.isWorker());
    assertTrue(verticle.isMultiThreaded());
    assertEquals(0, verticle.getHooks().size());
    assertEquals(0, verticle.getInputs().size());
  }

  @Test
  public void testAddHookFromJson() {
    JsonObject json = new JsonObject().putString(Network.NETWORK_ADDRESS, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(Verticle.COMPONENT_ADDRESS, "feeder")
        .putString(Verticle.COMPONENT_TYPE, "feeder")
        .putString(Verticle.COMPONENT_DEPLOYMENT_METHOD, "verticle")
        .putString(Verticle.VERTICLE_MAIN, "test.py");
    JsonObject jsonHook = new JsonObject().putString("type", TestHook.class.getName());
    jsonFeeder.putArray(Verticle.COMPONENT_HOOKS, new JsonArray().add(jsonHook));
    json.putObject(Network.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    Verticle<Feeder> feeder = network.getComponent("feeder");
    assertNotNull(feeder);
    List<ComponentHook> hooks = feeder.getHooks();
    assertEquals(1, hooks.size());
    assertTrue(hooks.get(0) instanceof TestHook);
  }

  @Test
  public void testAddInputsFromJson() {
    JsonObject json = new JsonObject().putString(Network.NETWORK_ADDRESS, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(Verticle.COMPONENT_ADDRESS, "feeder")
        .putString(Verticle.COMPONENT_TYPE, "feeder")
        .putString(Verticle.COMPONENT_DEPLOYMENT_METHOD, "verticle")
        .putString(Verticle.VERTICLE_MAIN, "test.py");

    JsonArray jsonInputs = new JsonArray();
    jsonInputs.add(new JsonObject().putString(Input.INPUT_ADDRESS, "input1"));
    jsonInputs.add(new JsonObject().putString(Input.INPUT_ADDRESS, "input2")
        .putString(Input.INPUT_STREAM, "nondefault"));
    jsonInputs.add(new JsonObject().putString(Input.INPUT_ADDRESS, "input3")
        .putObject(Input.INPUT_GROUPING, new JsonObject().putString("type", "random")));
    jsonInputs.add(new JsonObject().putString(Input.INPUT_ADDRESS, "input4")
        .putObject(Input.INPUT_GROUPING, new JsonObject().putString("type", "fields")
            .putArray("fields", new JsonArray().add("foo").add("bar"))));
    jsonFeeder.putArray(Verticle.COMPONENT_INPUTS, jsonInputs);

    json.putObject(Network.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    Network network = Network.fromJson(json);
    assertEquals("test", network.getAddress());
    Verticle<Feeder> feeder = network.getComponent("feeder");
    assertNotNull(feeder);

    List<Input> inputs = feeder.getInputs();
    assertEquals(4, inputs.size());
    Input input1 = inputs.get(0);
    assertNotNull(input1.id());
    assertEquals("input1", input1.getAddress());
    assertEquals("default", input1.getStream());
    assertTrue(input1.getGrouping() instanceof RoundGrouping);
    Input input2 = inputs.get(1);
    assertNotNull(input2.id());
    assertEquals("input2", input2.getAddress());
    assertEquals("nondefault", input2.getStream());
    assertTrue(input2.getGrouping() instanceof RoundGrouping);
    Input input3 = inputs.get(2);
    assertNotNull(input3.id());
    assertEquals("input3", input3.getAddress());
    assertEquals("default", input3.getStream());
    assertTrue(input3.getGrouping() instanceof RandomGrouping);
    Input input4 = inputs.get(3);
    assertNotNull(input4.id());
    assertEquals("input4", input4.getAddress());
    assertEquals("default", input4.getStream());
    assertTrue(input4.getGrouping() instanceof FieldsGrouping);
    assertTrue(((FieldsGrouping) input4.getGrouping()).getFields().contains("foo"));
    assertTrue(((FieldsGrouping) input4.getGrouping()).getFields().contains("bar"));
  }

  public static class TestHook implements ComponentHook {
    @Override
    public void handleStart(net.kuujo.vertigo.component.Component<?> subject) {
      
    }
    @Override
    public void handleStop(net.kuujo.vertigo.component.Component<?> subject) {
      
    }
    @Override
    public void handleReceive(MessageId messageId) {
      
    }
    @Override
    public void handleAck(MessageId messageId) {
      
    }
    @Override
    public void handleFail(MessageId messageId) {
      
    }
    @Override
    public void handleEmit(MessageId messageId) {
      
    }
    @Override
    public void handleAcked(MessageId messageId) {
      
    }
    @Override
    public void handleFailed(MessageId messageId) {
      
    }
    @Override
    public void handleTimeout(MessageId messageId) {
      
    }
  }

}
