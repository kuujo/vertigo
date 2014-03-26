/*
 * Copyright 2013-2014 the original author or authors.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.context.impl.ContextBuilder;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.network.ModuleConfig;
import net.kuujo.vertigo.network.VerticleConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

/**
 * Network/component/instance/input context tests.
 *
 * @author Jordan Halterman
 */
public class ContextTest {

  @Test
  public void testDefaultNetworkContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", "feeder.py");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.name());
    assertEquals("test.feeder", verticleContext.address());
    assertEquals("feeder.py", verticleContext.main());
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    VerticleConfig verticle = network.addVerticle("feeder", "feeder.py");
    verticle.setMain("feeder.py");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setInstances(2);
    verticle.setGroup("test");
    verticle.setWorker(true);
    verticle.setMultiThreaded(true);
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.name());
    assertEquals("test.feeder", verticleContext.address());
    assertEquals("feeder.py", verticleContext.main());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals("test", verticleContext.group());
    assertEquals(0, verticleContext.hooks().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultFeederModuleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    network.addModule("feeder", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    ModuleContext moduleContext = context.component("feeder");
    assertEquals("feeder", moduleContext.name());
    assertEquals("test.feeder", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
    assertEquals(0, moduleContext.hooks().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testConfiguredFeederModuleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    ModuleConfig verticle = network.addModule("feeder", "com.test~test-module~1.0");
    verticle.setModule("com.test~test-module~1.0");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setInstances(2);
    verticle.setGroup("test");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    ModuleContext moduleContext = context.component("feeder");
    assertEquals("feeder", moduleContext.name());
    assertEquals("test.feeder", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals("test", moduleContext.group());
    assertEquals(0, moduleContext.hooks().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testDefaultWorkerVerticleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("worker", "worker.py");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("test.worker", verticleContext.address());
    assertEquals("worker.py", verticleContext.main());
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    VerticleConfig verticle = network.addVerticle("worker", "worker.py");
    verticle.setMain("worker.py");
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    verticle.setInstances(2);
    verticle.setGroup("test");
    verticle.setWorker(true);
    verticle.setMultiThreaded(true);
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("test.worker", verticleContext.address());
    assertEquals("worker.py", verticleContext.main());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals("test", verticleContext.group());
    assertEquals(0, verticleContext.hooks().size());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultWorkerModuleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    network.addModule("worker", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    ModuleContext moduleContext = context.component("worker");
    assertEquals("worker", moduleContext.name());
    assertEquals("test.worker", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
    assertEquals(0, moduleContext.hooks().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testConfiguredWorkerModuleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    ModuleConfig module = network.addModule("worker", "com.test~test-module~1.0");
    module.setModule("com.test~test-module~1.0");
    module.setConfig(new JsonObject().putString("foo", "bar"));
    module.setInstances(2);
    module.setGroup("test");
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    ModuleContext moduleContext = context.component("worker");
    assertEquals("worker", moduleContext.name());
    assertEquals("test.worker", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals("test", moduleContext.group());
    assertEquals(0, moduleContext.hooks().size());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testHookContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    VerticleConfig verticle = network.addVerticle("feeder", "feeder.py");
    verticle.addHook(new TestHook());
    NetworkContext context = ContextBuilder.buildContext(network, null);
    assertEquals("test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals(1, verticleContext.hooks().size());
    assertTrue(verticleContext.hooks().get(0) instanceof TestHook);
  }

  @Test
  public void testInstanceContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    VerticleConfig verticle = network.addVerticle("feeder", "feeder.py");
    verticle.setInstances(2);
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

  public static class TestHook implements ComponentHook {
    @Override
    public void handleStart(net.kuujo.vertigo.component.Component subject) {
      
    }
    @Override
    public void handleStop(net.kuujo.vertigo.component.Component subject) {
      
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
