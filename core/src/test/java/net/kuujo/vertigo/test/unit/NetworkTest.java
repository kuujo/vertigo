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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.component.ModuleConfig;
import net.kuujo.vertigo.component.VerticleConfig;
import net.kuujo.vertigo.component.impl.DefaultModuleConfig;
import net.kuujo.vertigo.component.impl.DefaultVerticleConfig;
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.io.connection.impl.DefaultConnectionConfig;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.util.Configs;

import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Network configuration tests.
 *
 * @author Jordan Halterman
 */
public class NetworkTest {

  @Test
  public void testDefaultNetworkConfig() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
  }

  @Test
  public void testVerticleDefaults() {
    VerticleConfig verticle = new DefaultVerticleConfig("test", "test.py", new DefaultNetworkConfig("test"));
    assertEquals("test", verticle.getName());
    assertTrue(verticle.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("test.py", verticle.getMain());
    assertEquals(new JsonObject(), verticle.getConfig());
    assertEquals(1, verticle.getInstances());
    assertEquals("__DEFAULT__", verticle.getGroup());
    assertFalse(verticle.isWorker());
    assertFalse(verticle.isMultiThreaded());
  }

  @Test
  public void testDefaultVerticleConfig() {
    VerticleConfig verticle = new DefaultVerticleConfig("test", "test.py", new DefaultNetworkConfig("test"));
    assertEquals("test", verticle.getName());
    assertTrue(verticle.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("test.py", verticle.getMain());
    verticle.setMain("foo.js");
    assertEquals("foo.js", verticle.getMain());
    assertEquals(new JsonObject(), verticle.getConfig());
    verticle.setConfig(new JsonObject().putString("foo", "bar"));
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(1, verticle.getInstances());
    verticle.setInstances(4);
    assertEquals(4, verticle.getInstances());
    assertEquals("__DEFAULT__", verticle.getGroup());
    verticle.setGroup("test");
    assertEquals("test", verticle.getGroup());
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
    NetworkConfig network = new DefaultNetworkConfig("test");
    try {
      network.addModule("feeder", "feeder.py");
      fail();
    }
    catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testModuleDefaults() {
    ModuleConfig module = new DefaultModuleConfig("test", "com.test~test-module~1.0", new DefaultNetworkConfig("test"));
    assertEquals("test", module.getName());
    assertTrue(module.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals(new JsonObject(), module.getConfig());
    assertEquals(1, module.getInstances());
    assertEquals("__DEFAULT__", module.getGroup());
  }

  @Test
  public void testDefaultModuleConfig() {
    ModuleConfig module = new DefaultModuleConfig("test", "com.test~test-module~1.0", new DefaultNetworkConfig("test"));
    assertEquals("test", module.getName());
    assertTrue(module.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", module.getModule());
    module.setModule("com.foo~foo~1.0");
    assertEquals("com.foo~foo~1.0", module.getModule());
    assertEquals(new JsonObject(), module.getConfig());
    module.setConfig(new JsonObject().putString("foo", "bar"));
    assertEquals("bar", module.getConfig().getString("foo"));
    assertEquals(1, module.getInstances());
    module.setInstances(4);
    assertEquals(4, module.getInstances());
    assertEquals("__DEFAULT__", module.getGroup());
    module.setGroup("test");
    assertEquals("test", module.getGroup());
  }

  @Test
  public void testAddFeeder() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
    ComponentConfig<?> verticle1 = network.addVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getName());
    assertTrue(verticle1.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle1.py", ((VerticleConfig) verticle1).getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getInstances());
    ComponentConfig<?> verticle2 = network.addVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getName());
    assertTrue(verticle2.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle2.py", ((VerticleConfig) verticle2).getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getInstances());
    ComponentConfig<?> verticle3 = network.addVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getName());
    assertTrue(verticle3.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle3.py", ((VerticleConfig) verticle3).getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getInstances());
    ComponentConfig<?> verticle4 = network.addVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getName());
    assertTrue(verticle4.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle4.py", ((VerticleConfig) verticle4).getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getInstances());
    ComponentConfig<?> module1 = network.addModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getName());
    assertTrue(module1.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", ((ModuleConfig) module1).getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getInstances());
    ComponentConfig<?> module2 = network.addModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getName());
    assertTrue(module2.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~2.0", ((ModuleConfig) module2).getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getInstances());
    ComponentConfig<?> module3 = network.addModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getName());
    assertTrue(module3.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~3.0", ((ModuleConfig) module3).getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getInstances());
    ComponentConfig<?> module4 = network.addModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getName());
    assertTrue(module4.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~4.0", ((ModuleConfig) module4).getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getInstances());
  }

  @Test
  public void testAddFeederVerticle() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
    VerticleConfig verticle1 = network.addVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getName());
    assertTrue(verticle1.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle1.py", verticle1.getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getInstances());
    VerticleConfig verticle2 = network.addVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getName());
    assertTrue(verticle2.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle2.py", verticle2.getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getInstances());
    VerticleConfig verticle3 = network.addVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getName());
    assertTrue(verticle3.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle3.py", verticle3.getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getInstances());
    VerticleConfig verticle4 = network.addVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getName());
    assertTrue(verticle4.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle4.py", verticle4.getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getInstances());
  }

  @Test
  public void testAddFeederModule() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
    ModuleConfig module1 = network.addModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getName());
    assertTrue(module1.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", module1.getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getInstances());
    ModuleConfig module2 = network.addModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getName());
    assertTrue(module2.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~2.0", module2.getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getInstances());
    ModuleConfig module3 = network.addModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getName());
    assertTrue(module3.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~3.0", module3.getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getInstances());
    ModuleConfig module4 = network.addModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getName());
    assertTrue(module4.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~4.0", module4.getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getInstances());
  }

  @Test
  public void testAddWorker() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
    ComponentConfig<?> verticle1 = network.addVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getName());
    assertTrue(verticle1.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle1.py", ((VerticleConfig) verticle1).getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getInstances());
    ComponentConfig<?> verticle2 = network.addVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getName());
    assertTrue(verticle2.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle2.py", ((VerticleConfig) verticle2).getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getInstances());
    ComponentConfig<?> verticle3 = network.addVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getName());
    assertTrue(verticle3.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle3.py", ((VerticleConfig) verticle3).getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getInstances());
    ComponentConfig<?> verticle4 = network.addVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getName());
    assertTrue(verticle4.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle4.py", ((VerticleConfig) verticle4).getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getInstances());
    ComponentConfig<?> module1 = network.addModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getName());
    assertTrue(module1.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", ((ModuleConfig) module1).getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getInstances());
    ComponentConfig<?> module2 = network.addModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getName());
    assertTrue(module2.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~2.0", ((ModuleConfig) module2).getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getInstances());
    ComponentConfig<?> module3 = network.addModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getName());
    assertTrue(module3.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~3.0", ((ModuleConfig) module3).getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getInstances());
    ComponentConfig<?> module4 = network.addModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getName());
    assertTrue(module4.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~4.0", ((ModuleConfig) module4).getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getInstances());
  }

  @Test
  public void testAddWorkerVerticle() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
    VerticleConfig verticle1 = network.addVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getName());
    assertTrue(verticle1.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle1.py", verticle1.getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getInstances());
    VerticleConfig verticle2 = network.addVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getName());
    assertTrue(verticle2.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle2.py", verticle2.getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getInstances());
    VerticleConfig verticle3 = network.addVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getName());
    assertTrue(verticle3.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle3.py", verticle3.getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getInstances());
    VerticleConfig verticle4 = network.addVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getName());
    assertTrue(verticle4.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle4.py", verticle4.getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getInstances());
  }

  @Test
  public void testAddWorkerModule() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
    ModuleConfig module1 = network.addModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getName());
    assertTrue(module1.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", module1.getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getInstances());
    ModuleConfig module2 = network.addModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getName());
    assertTrue(module2.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~2.0", module2.getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getInstances());
    ModuleConfig module3 = network.addModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getName());
    assertTrue(module3.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~3.0", module3.getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getInstances());
    ModuleConfig module4 = network.addModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getName());
    assertTrue(module4.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~4.0", module4.getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getInstances());
  }

  @Test
  public void testConnectionEquals() {
    ConnectionConfig connection1 = new DefaultConnectionConfig("foo:bar", "bar:baz", new DefaultNetworkConfig("test"));
    ConnectionConfig connection2 = new DefaultConnectionConfig("foo:bar", "bar:baz", new DefaultNetworkConfig("test"));
    assertTrue(connection1.equals(connection2));
  }

  @Test
  public void testConnectionDefaultPort() {
    ConnectionConfig connection = new DefaultConnectionConfig("foo", "bar", new DefaultNetworkConfig("test"));
    assertEquals("foo", connection.getSource().getComponent());
    assertEquals("out", connection.getSource().getPort());
    assertEquals("bar", connection.getTarget().getComponent());
    assertEquals("in", connection.getTarget().getPort());
  }

  @Test
  public void testCreateConnectionDefaultPort() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    ConnectionConfig connection = network.createConnection("foo", "bar");
    assertEquals("foo", connection.getSource().getComponent());
    assertEquals("out", connection.getSource().getPort());
    assertEquals("bar", connection.getTarget().getComponent());
    assertEquals("in", connection.getTarget().getPort());
  }

  @Test
  public void testDestroyConnection() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    ConnectionConfig connection = network.createConnection("foo", "bar");
    network.destroyConnection("foo", "bar");
    boolean exists = false;
    for (ConnectionConfig other : network.getConnections()) {
      if (other.equals(connection)) {
        exists = true;
      }
    }
    assertFalse(exists);
  }

  @Test
  public void testMergeNetworks() {
    NetworkConfig network1 = new DefaultNetworkConfig("test");
    network1.addComponent("foo", "foo.py", 2);
    NetworkConfig network2 = new DefaultNetworkConfig("test");
    network2.addComponent("bar", "bar.js", 4);
    network2.createConnection("foo", "bar");
    NetworkConfig network3 = Configs.mergeNetworks(network1, network2);
    assertTrue(network3.hasComponent("foo"));
    assertTrue(network3.hasComponent("bar"));
    boolean exists = false;
    for (ConnectionConfig connection : network3.getConnections()) {
      if (connection.equals(new DefaultConnectionConfig("foo", "bar", null))) {
        exists = true;
        break;
      }
    }
    assertTrue(exists);
  }

  @Test
  public void testUnmergeNetworks() {
    NetworkConfig network1 = new DefaultNetworkConfig("test");
    network1.addComponent("foo", "foo.py", 2);
    network1.addComponent("bar", "bar.js", 4);
    network1.createConnection("foo", "bar");
    NetworkConfig network2 = new DefaultNetworkConfig("test");
    network2.addComponent("bar", "bar.js", 4);
    network2.createConnection("foo", "bar");
    NetworkConfig network3 = Configs.unmergeNetworks(network1, network2);
    assertTrue(network3.hasComponent("foo"));
    assertFalse(network3.hasComponent("bar"));
    boolean exists = false;
    for (ConnectionConfig connection : network3.getConnections()) {
      if (connection.equals(new DefaultConnectionConfig("foo", "bar", null))) {
        exists = true;
        break;
      }
    }
    assertFalse(exists);
  }

  @Test
  public void testMergeNetworksFail() {
    NetworkConfig network1 = new DefaultNetworkConfig("test");
    NetworkConfig network2 = new DefaultNetworkConfig("nottest");
    try {
      Configs.mergeNetworks(network1, network2);
      fail();
    } catch(Exception e) {
    }
  }

  @Test
  public void testNetworkFromJson() {
    JsonObject json = new JsonObject()
        .putString(NetworkConfig.NETWORK_NAME, "test")
        .putObject(NetworkConfig.NETWORK_COMPONENTS, new JsonObject());
    NetworkConfig network = new Vertigo(null, null).createNetwork(json);
    assertEquals("test", network.getName());
  }

  @Test
  public void testAddFeederModuleFromJson() {
    JsonObject json = new JsonObject().putString(NetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(ModuleConfig.COMPONENT_NAME, "feeder")
        .putString(ModuleConfig.COMPONENT_TYPE, ModuleConfig.COMPONENT_TYPE_MODULE)
        .putString(ModuleConfig.MODULE_NAME, "com.test~test-module~1.0")
        .putObject(ModuleConfig.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(ModuleConfig.COMPONENT_NUM_INSTANCES, 2);
    json.putObject(NetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    NetworkConfig network = new Vertigo(null, null).createNetwork(json);
    assertEquals("test", network.getName());
    ModuleConfig module = network.getComponent("feeder");
    assertEquals("feeder", module.getName());
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals("bar", module.getConfig().getString("foo"));
    assertEquals(2, module.getInstances());
    assertTrue(module.getType().equals(ComponentConfig.Type.MODULE));
  }

  @Test
  public void testAddFeederVerticleFromJson() {
    JsonObject json = new JsonObject().putString(NetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(VerticleConfig.COMPONENT_NAME, "feeder")
        .putString(VerticleConfig.COMPONENT_TYPE, VerticleConfig.COMPONENT_TYPE_VERTICLE)
        .putString(VerticleConfig.VERTICLE_MAIN, "test.py")
        .putObject(VerticleConfig.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(VerticleConfig.COMPONENT_NUM_INSTANCES, 2)
        .putBoolean(VerticleConfig.VERTICLE_IS_WORKER, true)
        .putBoolean(VerticleConfig.VERTICLE_IS_MULTI_THREADED, true);
    json.putObject(NetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    NetworkConfig network = new Vertigo(null, null).createNetwork(json);
    assertEquals("test", network.getName());
    VerticleConfig verticle = network.getComponent("feeder");
    assertEquals("feeder", verticle.getName());
    assertEquals("test.py", verticle.getMain());
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(2, verticle.getInstances());
    assertTrue(verticle.getType().equals(ComponentConfig.Type.VERTICLE));
    assertTrue(verticle.isWorker());
    assertTrue(verticle.isMultiThreaded());
  }

  @Test
  public void testAddWorkerModuleFromJson() {
    JsonObject json = new JsonObject().putString(NetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonWorker = new JsonObject()
        .putString(ModuleConfig.COMPONENT_NAME, "worker")
        .putString(ModuleConfig.COMPONENT_TYPE, ModuleConfig.COMPONENT_TYPE_MODULE)
        .putString(ModuleConfig.MODULE_NAME, "com.test~test-module~1.0")
        .putObject(ModuleConfig.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(ModuleConfig.COMPONENT_NUM_INSTANCES, 2);
    json.putObject(NetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("worker", jsonWorker));
    NetworkConfig network = new Vertigo(null, null).createNetwork(json);
    assertEquals("test", network.getName());
    ModuleConfig module = network.getComponent("worker");
    assertEquals("worker", module.getName());
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals("bar", module.getConfig().getString("foo"));
    assertEquals(2, module.getInstances());
    assertTrue(module.getType().equals(ComponentConfig.Type.MODULE));
  }

  @Test
  public void testAddWorkerVerticleFromJson() {
    JsonObject json = new JsonObject().putString(NetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonWorker = new JsonObject()
        .putString(VerticleConfig.COMPONENT_NAME, "worker")
        .putString(VerticleConfig.COMPONENT_TYPE, VerticleConfig.COMPONENT_TYPE_VERTICLE)
        .putString(VerticleConfig.VERTICLE_MAIN, "test.py")
        .putObject(VerticleConfig.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(VerticleConfig.COMPONENT_NUM_INSTANCES, 2)
        .putBoolean(VerticleConfig.VERTICLE_IS_WORKER, true)
        .putBoolean(VerticleConfig.VERTICLE_IS_MULTI_THREADED, true);
    json.putObject(NetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("worker", jsonWorker));
    NetworkConfig network = new Vertigo(null, null).createNetwork(json);
    assertEquals("test", network.getName());
    VerticleConfig verticle = network.getComponent("worker");
    assertEquals("worker", verticle.getName());
    assertEquals("test.py", verticle.getMain());
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(2, verticle.getInstances());
    assertTrue(verticle.getType().equals(ComponentConfig.Type.VERTICLE));
    assertTrue(verticle.isWorker());
    assertTrue(verticle.isMultiThreaded());
  }

  @Test
  public void testAddConnectionFromJson() {
    JsonObject json = new JsonObject().putString(NetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonConnection = new JsonObject()
        .putObject("source", new JsonObject().putString("component", "foo").putString("port", "notout"))
        .putObject("target", new JsonObject().putString("component", "bar").putString("port", "notin"))
        .putObject("grouping", new JsonObject().putString("type", "random"));
    json.putArray("connections", new JsonArray().add(jsonConnection));
    NetworkConfig network = new Vertigo(null, null).createNetwork(json);
    assertEquals("test", network.getName());
    ConnectionConfig connection = network.getConnections().iterator().next();
    assertEquals("foo", connection.getSource().getComponent());
    assertEquals("notout", connection.getSource().getPort());
    assertEquals("bar", connection.getTarget().getComponent());
    assertEquals("notin", connection.getTarget().getPort());
  }

}
