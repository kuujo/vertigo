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

import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.context.impl.ContextBuilder;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.network.Module;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Verticle;
import net.kuujo.vertigo.rpc.Executor;
import net.kuujo.vertigo.worker.Worker;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Network/component/instance/input context tests.
 *
 * @author Jordan Halterman
 */
public class ContextTest {

  @Test
  public void testDefaultNetworkContext() {
    Network network = new Network("test");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    assertEquals(1, context.auditors().size());
    assertEquals(1, context.numAuditors());
    assertTrue(context.isAckingEnabled());
    assertTrue(context.isMessageTimeoutsEnabled());
    assertEquals(30000, context.messageTimeout());
    assertEquals(0, context.components().size());
    try {
      context.component("foo");
      fail();
    }
    catch (Exception e) {
    }
  }

  @Test
  public void testConfiguredNetworkContext() {
    Network network = new Network("test");
    network.setAckingEnabled(true);
    network.setNumAuditors(2);
    network.setMessageTimeout(10000);
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    assertEquals(2, context.auditors().size());
    assertEquals(2, context.numAuditors());
    assertTrue(context.isAckingEnabled());
    assertTrue(context.isMessageTimeoutsEnabled());
    assertEquals(10000, context.messageTimeout());
    assertEquals(0, context.components().size());
    try {
      context.component("foo");
      fail();
    }
    catch (Exception e) {
    }
  }

  @Test
  public void testDefaultFeederVerticleContext() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", "feeder.py");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Feeder> verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.address());
    assertEquals("feeder.py", verticleContext.main());
    assertEquals(Feeder.class, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals(new JsonObject(), verticleContext.config());
    assertEquals(1, verticleContext.numInstances());
    assertEquals(5000, verticleContext.heartbeatInterval());
    assertFalse(verticleContext.isWorker());
    assertFalse(verticleContext.isMultiThreaded());
    assertEquals(0, verticleContext.hooks().size());
    assertEquals(0, verticleContext.inputs().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testConfiguredFeederVerticleContext() {
    Network network = new Network("test");
    Verticle<Feeder> verticle = network.addFeederVerticle("feeder", "feeder.py");
    verticle.setMain("feeder.py");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setHeartbeatInterval(1000);
    verticle.setWorker(true);
    verticle.setMultiThreaded(true);
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Feeder> verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.address());
    assertEquals("feeder.py", verticleContext.main());
    assertEquals(Feeder.class, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertEquals(1000, verticleContext.heartbeatInterval());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals(0, verticleContext.hooks().size());
    assertEquals(0, verticleContext.inputs().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultFeederModuleContext() {
    Network network = new Network("test");
    network.addFeederModule("feeder", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    ModuleContext<Feeder> moduleContext = context.component("feeder");
    assertEquals("feeder", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Feeder.class, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
    assertEquals(5000, moduleContext.heartbeatInterval());
    assertEquals(0, moduleContext.hooks().size());
    assertEquals(0, moduleContext.inputs().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testConfiguredFeederModuleContext() {
    Network network = new Network("test");
    Module<Feeder> verticle = network.addFeederModule("feeder", "com.test~test-module~1.0");
    verticle.setModule("com.test~test-module~1.0");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setHeartbeatInterval(1000);
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    ModuleContext<Feeder> moduleContext = context.component("feeder");
    assertEquals("feeder", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Feeder.class, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals(1000, moduleContext.heartbeatInterval());
    assertEquals(0, moduleContext.hooks().size());
    assertEquals(0, moduleContext.inputs().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testDefaultWorkerVerticleContext() {
    Network network = new Network("test");
    network.addWorkerVerticle("worker", "worker.py");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Worker> verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.address());
    assertEquals("worker.py", verticleContext.main());
    assertEquals(Worker.class, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals(new JsonObject(), verticleContext.config());
    assertEquals(1, verticleContext.numInstances());
    assertEquals(5000, verticleContext.heartbeatInterval());
    assertFalse(verticleContext.isWorker());
    assertFalse(verticleContext.isMultiThreaded());
    assertEquals(0, verticleContext.hooks().size());
    assertEquals(0, verticleContext.inputs().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testConfiguredWorkerVerticleContext() {
    Network network = new Network("test");
    Verticle<Worker> verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setMain("worker.py");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setHeartbeatInterval(1000);
    verticle.setWorker(true);
    verticle.setMultiThreaded(true);
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Worker> verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.address());
    assertEquals("worker.py", verticleContext.main());
    assertEquals(Worker.class, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertEquals(1000, verticleContext.heartbeatInterval());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals(0, verticleContext.hooks().size());
    assertEquals(0, verticleContext.inputs().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultWorkerModuleContext() {
    Network network = new Network("test");
    network.addWorkerModule("worker", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    ModuleContext<Worker> moduleContext = context.component("worker");
    assertEquals("worker", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Worker.class, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
    assertEquals(5000, moduleContext.heartbeatInterval());
    assertEquals(0, moduleContext.hooks().size());
    assertEquals(0, moduleContext.inputs().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testConfiguredWorkerModuleContext() {
    Network network = new Network("test");
    Module<Worker> verticle = network.addWorkerModule("worker", "com.test~test-module~1.0");
    verticle.setModule("com.test~test-module~1.0");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setHeartbeatInterval(1000);
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    ModuleContext<Worker> moduleContext = context.component("worker");
    assertEquals("worker", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Worker.class, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals(1000, moduleContext.heartbeatInterval());
    assertEquals(0, moduleContext.hooks().size());
    assertEquals(0, moduleContext.inputs().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testDefaultExecutorVerticleContext() {
    Network network = new Network("test");
    network.addExecutorVerticle("executor", "executor.py");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Executor> verticleContext = context.component("executor");
    assertEquals("executor", verticleContext.address());
    assertEquals("executor.py", verticleContext.main());
    assertEquals(Executor.class, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals(new JsonObject(), verticleContext.config());
    assertEquals(1, verticleContext.numInstances());
    assertEquals(5000, verticleContext.heartbeatInterval());
    assertFalse(verticleContext.isWorker());
    assertFalse(verticleContext.isMultiThreaded());
    assertEquals(0, verticleContext.hooks().size());
    assertEquals(0, verticleContext.inputs().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testConfiguredExecutorVerticleContext() {
    Network network = new Network("test");
    Verticle<Executor> verticle = network.addExecutorVerticle("executor", "executor.py");
    verticle.setMain("executor.py");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setHeartbeatInterval(1000);
    verticle.setWorker(true);
    verticle.setMultiThreaded(true);
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Executor> verticleContext = context.component("executor");
    assertEquals("executor", verticleContext.address());
    assertEquals("executor.py", verticleContext.main());
    assertEquals(Executor.class, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertEquals(1000, verticleContext.heartbeatInterval());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals(0, verticleContext.hooks().size());
    assertEquals(0, verticleContext.inputs().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultExecutorModuleContext() {
    Network network = new Network("test");
    network.addExecutorModule("executor", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    ModuleContext<Executor> moduleContext = context.component("executor");
    assertEquals("executor", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Executor.class, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
    assertEquals(5000, moduleContext.heartbeatInterval());
    assertEquals(0, moduleContext.hooks().size());
    assertEquals(0, moduleContext.inputs().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testConfiguredExecutorModuleContext() {
    Network network = new Network("test");
    Module<Executor> verticle = network.addExecutorModule("executor", "com.test~test-module~1.0");
    verticle.setModule("com.test~test-module~1.0");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setHeartbeatInterval(1000);
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    ModuleContext<Executor> moduleContext = context.component("executor");
    assertEquals("executor", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Executor.class, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals(1000, moduleContext.heartbeatInterval());
    assertEquals(0, moduleContext.hooks().size());
    assertEquals(0, moduleContext.inputs().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testHookContext() {
    Network network = new Network("test");
    Verticle<Feeder> verticle = network.addFeederVerticle("feeder", "feeder.py");
    verticle.addHook(new TestHook());
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Feeder> verticleContext = context.component("feeder");
    assertEquals(1, verticleContext.hooks().size());
    assertTrue(verticleContext.hooks().get(0) instanceof TestHook);
  }

  @Test
  public void testInstanceContext() {
    Network network = new Network("test");
    Verticle<Feeder> verticle = network.addFeederVerticle("feeder", "feeder.py");
    verticle.setNumInstances(2);
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Feeder> verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.address());
    assertEquals(2, verticleContext.instances().size());
    assertEquals("feeder-1", verticleContext.instances().get(0).address());
    assertEquals("feeder-2", verticleContext.instances().get(1).address());
    assertNotNull(verticleContext.instances().get(0).component());
  }

  @Test
  public void testInputContextDefaults() {
    Network network = new Network("test");
    Verticle<Worker> verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setNumInstances(2);
    verticle.addInput("input");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Worker> verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.address());
    assertEquals(1, verticleContext.inputs().size());
    InputContext inputContext = verticleContext.inputs().get(0);
    assertEquals("input", inputContext.address());
    assertEquals("default", inputContext.stream());
    assertTrue(inputContext.grouping() instanceof RoundGrouping);
    assertNotNull(inputContext.component());
  }

  @Test
  public void testInputContextStream() {
    Network network = new Network("test");
    Verticle<Worker> verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setNumInstances(2);
    verticle.addInput("input", "nondefault");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Worker> verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.address());
    assertEquals(1, verticleContext.inputs().size());
    InputContext inputContext = verticleContext.inputs().get(0);
    assertEquals("input", inputContext.address());
    assertEquals("nondefault", inputContext.stream());
    assertTrue(inputContext.grouping() instanceof RoundGrouping);
  }

  @Test
  public void testInputContextGrouping() {
    Network network = new Network("test");
    Verticle<Worker> verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setNumInstances(2);
    verticle.addInput("input").fieldsGrouping("foo", "bar");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Worker> verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.address());
    assertEquals(1, verticleContext.inputs().size());
    InputContext inputContext = verticleContext.inputs().get(0);
    assertEquals("input", inputContext.address());
    assertEquals("default", inputContext.stream());
    assertTrue(inputContext.grouping() instanceof FieldsGrouping);
    assertTrue(((FieldsGrouping) inputContext.grouping()).getFields().contains("foo"));
    assertTrue(((FieldsGrouping) inputContext.grouping()).getFields().contains("bar"));
  }

  @Test
  public void testInputContextCount() {
    Network network = new Network("test");
    Verticle<Worker> verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setNumInstances(2);
    verticle.addInput("input");
    NetworkContext context = ContextBuilder.buildContext(network);
    assertEquals("test", context.address());
    VerticleContext<Worker> verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.address());
    assertEquals(1, verticleContext.inputs().size());
    InputContext inputContext = verticleContext.inputs().get(0);
    assertEquals("input", inputContext.address());
    assertEquals("default", inputContext.stream());
    assertTrue(inputContext.grouping() instanceof RoundGrouping);
    assertEquals(2, inputContext.count());
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
