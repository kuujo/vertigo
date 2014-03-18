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

import net.kuujo.vertigo.context.InputStreamContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.OutputStreamContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Module;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Verticle;
import net.kuujo.vertigo.context.impl.ContextBuilder;

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
    NetworkContext context = ContextBuilder.buildContext(network, null);
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
    NetworkContext context = ContextBuilder.buildContext(network, null);
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
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.name());
    assertEquals("test.feeder", verticleContext.address());
    assertEquals("feeder.py", verticleContext.main());
    assertEquals(Component.Type.FEEDER, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals(new JsonObject(), verticleContext.config());
    assertEquals(1, verticleContext.numInstances());
    assertFalse(verticleContext.isWorker());
    assertFalse(verticleContext.isMultiThreaded());
    assertEquals(0, verticleContext.hooks().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testConfiguredFeederVerticleContext() {
    Network network = new Network("test");
    Verticle verticle = network.addFeederVerticle("feeder", "feeder.py");
    verticle.setMain("feeder.py");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setDeploymentGroup("test");
    verticle.setWorker(true);
    verticle.setMultiThreaded(true);
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.name());
    assertEquals("test.feeder", verticleContext.address());
    assertEquals("feeder.py", verticleContext.main());
    assertEquals(Component.Type.FEEDER, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals("test", verticleContext.deploymentGroup());
    assertEquals(0, verticleContext.hooks().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultFeederModuleContext() {
    Network network = new Network("test");
    network.addFeederModule("feeder", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    ModuleContext moduleContext = context.component("feeder");
    assertEquals("feeder", moduleContext.name());
    assertEquals("test.feeder", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Component.Type.FEEDER, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
    assertEquals(0, moduleContext.hooks().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testConfiguredFeederModuleContext() {
    Network network = new Network("test");
    Module verticle = network.addFeederModule("feeder", "com.test~test-module~1.0");
    verticle.setModule("com.test~test-module~1.0");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setDeploymentGroup("test");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    ModuleContext moduleContext = context.component("feeder");
    assertEquals("feeder", moduleContext.name());
    assertEquals("test.feeder", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Component.Type.FEEDER, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals("test", moduleContext.deploymentGroup());
    assertEquals(0, moduleContext.hooks().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testDefaultWorkerVerticleContext() {
    Network network = new Network("test");
    network.addWorkerVerticle("worker", "worker.py");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("test.worker", verticleContext.address());
    assertEquals("worker.py", verticleContext.main());
    assertEquals(Component.Type.WORKER, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals(new JsonObject(), verticleContext.config());
    assertEquals(1, verticleContext.numInstances());
    assertFalse(verticleContext.isWorker());
    assertFalse(verticleContext.isMultiThreaded());
    assertEquals(0, verticleContext.hooks().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testConfiguredWorkerVerticleContext() {
    Network network = new Network("test");
    Verticle verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setMain("worker.py");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setNumInstances(2);
    verticle.setDeploymentGroup("test");
    verticle.setWorker(true);
    verticle.setMultiThreaded(true);
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("test.worker", verticleContext.address());
    assertEquals("worker.py", verticleContext.main());
    assertEquals(Component.Type.WORKER, verticleContext.type());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals("test", verticleContext.deploymentGroup());
    assertEquals(0, verticleContext.hooks().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultWorkerModuleContext() {
    Network network = new Network("test");
    network.addWorkerModule("worker", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    ModuleContext moduleContext = context.component("worker");
    assertEquals("worker", moduleContext.name());
    assertEquals("test.worker", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Component.Type.WORKER, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
    assertEquals(0, moduleContext.hooks().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testConfiguredWorkerModuleContext() {
    Network network = new Network("test");
    Module module = network.addWorkerModule("worker", "com.test~test-module~1.0");
    module.setModule("com.test~test-module~1.0");
    module.setConfig(new JsonObject().putString("foo", "bar"));
    module.setNumInstances(2);
    module.setDeploymentGroup("test");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    ModuleContext moduleContext = context.component("worker");
    assertEquals("worker", moduleContext.name());
    assertEquals("test.worker", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertEquals(Component.Type.WORKER, moduleContext.type());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals("test", moduleContext.deploymentGroup());
    assertEquals(0, moduleContext.hooks().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testHookContext() {
    Network network = new Network("test");
    Verticle verticle = network.addFeederVerticle("feeder", "feeder.py");
    verticle.addHook(new TestHook());
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals(1, verticleContext.hooks().size());
    assertTrue(verticleContext.hooks().get(0) instanceof TestHook);
  }

  @Test
  public void testInstanceContext() {
    Network network = new Network("test");
    Verticle verticle = network.addFeederVerticle("feeder", "feeder.py");
    verticle.setNumInstances(2);
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.name());
    assertEquals("test.feeder", verticleContext.address());
    assertEquals(2, verticleContext.instances().size());
    assertEquals("test.feeder-1", verticleContext.instances().get(0).address());
    assertEquals("test.feeder-2", verticleContext.instances().get(1).address());
    assertNotNull(verticleContext.instances().get(0).component());
  }

  @Test
  public void testInputOutputContext() {
    Network network = new Network("test");
    network.addFeederVerticle("feeder", "feeder.py");
    Verticle verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setNumInstances(2);
    verticle.addInput("feeder", "stream").randomGrouping();
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());

    VerticleContext feederContext = context.component("feeder");
    assertEquals("feeder", feederContext.name());
    assertEquals("test.feeder", feederContext.address());
    for (InstanceContext instanceContext : feederContext.instances()) {
      OutputStreamContext outputContext = instanceContext.output().streams().iterator().next();
      assertTrue(outputContext.connections().size() == 2);
      assertTrue(outputContext.stream().equals("stream"));
      assertTrue(outputContext.grouping() instanceof RandomGrouping);
    }

    VerticleContext workerContext = context.component("worker");
    assertEquals("worker", workerContext.name());
    assertEquals("test.worker", workerContext.address());
    for (InstanceContext instanceContext : workerContext.instances()) {
      InputStreamContext inputContext = instanceContext.input().streams().iterator().next();
      inputContext.stream().equals("stream");
    }
  }

  @Test
  public void testInputContextStream() {
    Network network = new Network("test");
    Verticle verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setNumInstances(2);
    verticle.addInput("input", "nondefault");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("test.worker", verticleContext.address());
  }

  @Test
  public void testInputContextGrouping() {
    Network network = new Network("test");
    Verticle verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setNumInstances(2);
    verticle.addInput("input").fieldsGrouping("foo", "bar");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("test.worker", verticleContext.address());
  }

  @Test
  public void testInputContextCount() {
    Network network = new Network("test");
    Verticle verticle = network.addWorkerVerticle("worker", "worker.py");
    verticle.setNumInstances(2);
    verticle.addInput("input");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("test.worker", verticleContext.address());
  }

  public static class TestHook implements ComponentHook {
    @Override
    public void handleStart(net.kuujo.vertigo.component.Component<?> subject) {
      
    }
    @Override
    public void handleStop(net.kuujo.vertigo.component.Component<?> subject) {
      
    }
    @Override
    public void handleReceive(String String) {
      
    }
    @Override
    public void handleAck(String String) {
      
    }
    @Override
    public void handleFail(String String) {
      
    }
    @Override
    public void handleEmit(String String) {
      
    }
    @Override
    public void handleAcked(String String) {
      
    }
    @Override
    public void handleFailed(String String) {
      
    }
    @Override
    public void handleTimeout(String String) {
      
    }
  }

}
