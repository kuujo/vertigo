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
import net.kuujo.vertigo.component.ModuleConfig;
import net.kuujo.vertigo.component.ModuleContext;
import net.kuujo.vertigo.component.VerticleConfig;
import net.kuujo.vertigo.component.VerticleContext;
import net.kuujo.vertigo.impl.ContextBuilder;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
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
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    assertEquals(0, context.components().size());
  }

  @Test
  public void testConfiguredNetworkContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    assertEquals(0, context.components().size());
  }

  @Test
  public void testDefaultFeederVerticleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("feeder", "feeder.py");
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.name());
    assertEquals("vertigo.test.feeder", verticleContext.address());
    assertEquals("feeder.py", verticleContext.main());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals(new JsonObject(), verticleContext.config());
    assertEquals(1, verticleContext.numInstances());
    assertFalse(verticleContext.isWorker());
    assertFalse(verticleContext.isMultiThreaded());
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
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.name());
    assertEquals("vertigo.test.feeder", verticleContext.address());
    assertEquals("feeder.py", verticleContext.main());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals("test", verticleContext.group());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultFeederModuleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    network.addModule("feeder", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    ModuleContext moduleContext = context.component("feeder");
    assertEquals("feeder", moduleContext.name());
    assertEquals("vertigo.test.feeder", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
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
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    ModuleContext moduleContext = context.component("feeder");
    assertEquals("feeder", moduleContext.name());
    assertEquals("vertigo.test.feeder", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals("test", moduleContext.group());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testDefaultWorkerVerticleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("worker", "worker.py");
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("vertigo.test.worker", verticleContext.address());
    assertEquals("worker.py", verticleContext.main());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals(new JsonObject(), verticleContext.config());
    assertEquals(1, verticleContext.numInstances());
    assertFalse(verticleContext.isWorker());
    assertFalse(verticleContext.isMultiThreaded());
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
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    VerticleContext verticleContext = context.component("worker");
    assertEquals("worker", verticleContext.name());
    assertEquals("vertigo.test.worker", verticleContext.address());
    assertEquals("worker.py", verticleContext.main());
    assertTrue(verticleContext.isVerticle());
    assertFalse(verticleContext.isModule());
    assertEquals("bar", verticleContext.config().getString("foo"));
    assertEquals(2, verticleContext.numInstances());
    assertEquals(2, verticleContext.instances().size());
    assertTrue(verticleContext.isWorker());
    assertTrue(verticleContext.isMultiThreaded());
    assertEquals("test", verticleContext.group());
    assertNotNull(verticleContext.network());
  }

  @Test
  public void testDefaultWorkerModuleContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    network.addModule("worker", "com.test~test-module~1.0");
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    ModuleContext moduleContext = context.component("worker");
    assertEquals("worker", moduleContext.name());
    assertEquals("vertigo.test.worker", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals(new JsonObject(), moduleContext.config());
    assertEquals(1, moduleContext.numInstances());
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
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    ModuleContext moduleContext = context.component("worker");
    assertEquals("worker", moduleContext.name());
    assertEquals("vertigo.test.worker", moduleContext.address());
    assertEquals("com.test~test-module~1.0", moduleContext.module());
    assertFalse(moduleContext.isVerticle());
    assertTrue(moduleContext.isModule());
    assertEquals("bar", moduleContext.config().getString("foo"));
    assertEquals(2, moduleContext.numInstances());
    assertEquals(2, moduleContext.instances().size());
    assertEquals("test", moduleContext.group());
    assertNotNull(moduleContext.network());
  }

  @Test
  public void testInstanceContext() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    VerticleConfig verticle = network.addVerticle("feeder", "feeder.py");
    verticle.setInstances(2);
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    assertEquals("vertigo.test", context.address());
    VerticleContext verticleContext = context.component("feeder");
    assertEquals("feeder", verticleContext.name());
    assertEquals("vertigo.test.feeder", verticleContext.address());
    assertEquals(2, verticleContext.instances().size());
    assertEquals("vertigo.test.feeder-1", verticleContext.instances().get(0).address());
    assertEquals("vertigo.test.feeder-2", verticleContext.instances().get(1).address());
    assertNotNull(verticleContext.instances().get(0).component());
  }

  @Test
  public void testUpdateContext() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    network.addVerticle("sender", "sender.py", 2);
    NetworkContext context = ContextBuilder.buildContext(network, "vertigo");
    NetworkConfig network2 = new DefaultNetworkConfig("test");
    network2.addVerticle("receiver", "receiver.py", 2);
    NetworkContext context2 = ContextBuilder.buildContext(network2, "vertigo");
    assertNotNull(context2.component("receiver"));
    context.notify(context2);
    assertNotNull(context.component("receiver"));
  }

}
