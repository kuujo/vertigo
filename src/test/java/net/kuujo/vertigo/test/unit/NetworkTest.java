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

import java.util.List;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.network.ComponentConfig;
import net.kuujo.vertigo.network.Configs;
import net.kuujo.vertigo.network.ConnectionConfig;
import net.kuujo.vertigo.network.ModuleConfig;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.VerticleConfig;
import net.kuujo.vertigo.network.impl.DefaultConnectionConfig;
import net.kuujo.vertigo.network.impl.DefaultModuleConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultVerticleConfig;

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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
  }

  @Test
  public void testVerticleDefaults() {
    DefaultVerticleConfig verticle = new DefaultVerticleConfig("test", "test.py", new DefaultNetworkConfig("test"));
    assertEquals("test", verticle.getName());
    assertTrue(verticle.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("test.py", verticle.getMain());
    assertEquals(new JsonObject(), verticle.getConfig());
    assertEquals(1, verticle.getInstances());
    assertEquals("__DEFAULT__", verticle.getGroup());
    assertFalse(verticle.isWorker());
    assertFalse(verticle.isMultiThreaded());
    assertEquals(0, verticle.getHooks().size());
  }

  @Test
  public void testDefaultVerticleConfig() {
    DefaultVerticleConfig verticle = new DefaultVerticleConfig("test", "test.py", new DefaultNetworkConfig("test"));
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    try {
      network.addModule("feeder", "feeder.py");
      fail();
    }
    catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testModuleDefaults() {
    DefaultModuleConfig module = new DefaultModuleConfig("test", "com.test~test-module~1.0", new DefaultNetworkConfig("test"));
    assertEquals("test", module.getName());
    assertTrue(module.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals(new JsonObject(), module.getConfig());
    assertEquals(1, module.getInstances());
    assertEquals("__DEFAULT__", module.getGroup());
    assertEquals(0, module.getHooks().size());
  }

  @Test
  public void testDefaultModuleConfig() {
    DefaultModuleConfig module = new DefaultModuleConfig("test", "com.test~test-module~1.0", new DefaultNetworkConfig("test"));
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
    ComponentConfig<?> verticle1 = network.addVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getName());
    assertTrue(verticle1.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle1.py", ((DefaultVerticleConfig) verticle1).getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getInstances());
    ComponentConfig<?> verticle2 = network.addVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getName());
    assertTrue(verticle2.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle2.py", ((DefaultVerticleConfig) verticle2).getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getInstances());
    ComponentConfig<?> verticle3 = network.addVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getName());
    assertTrue(verticle3.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle3.py", ((DefaultVerticleConfig) verticle3).getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getInstances());
    ComponentConfig<?> verticle4 = network.addVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getName());
    assertTrue(verticle4.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle4.py", ((DefaultVerticleConfig) verticle4).getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getInstances());
    ComponentConfig<?> module1 = network.addModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getName());
    assertTrue(module1.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", ((DefaultModuleConfig) module1).getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getInstances());
    ComponentConfig<?> module2 = network.addModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getName());
    assertTrue(module2.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~2.0", ((DefaultModuleConfig) module2).getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getInstances());
    ComponentConfig<?> module3 = network.addModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getName());
    assertTrue(module3.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~3.0", ((DefaultModuleConfig) module3).getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getInstances());
    ComponentConfig<?> module4 = network.addModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getName());
    assertTrue(module4.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~4.0", ((DefaultModuleConfig) module4).getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getInstances());
  }

  @Test
  public void testAddFeederVerticle() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
    assertEquals("test", network.getName());
    ComponentConfig<?> verticle1 = network.addVerticle("verticle1", "verticle1.py");
    assertEquals("verticle1", verticle1.getName());
    assertTrue(verticle1.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle1.py", ((DefaultVerticleConfig) verticle1).getMain());
    assertEquals(new JsonObject(), verticle1.getConfig());
    assertEquals(1, verticle1.getInstances());
    ComponentConfig<?> verticle2 = network.addVerticle("verticle2", "verticle2.py", new JsonObject().putString("foo", "bar"));
    assertEquals("verticle2", verticle2.getName());
    assertTrue(verticle2.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle2.py", ((DefaultVerticleConfig) verticle2).getMain());
    assertEquals("bar", verticle2.getConfig().getString("foo"));
    assertEquals(1, verticle2.getInstances());
    ComponentConfig<?> verticle3 = network.addVerticle("verticle3", "verticle3.py", 2);
    assertEquals("verticle3", verticle3.getName());
    assertTrue(verticle3.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle3.py", ((DefaultVerticleConfig) verticle3).getMain());
    assertEquals(new JsonObject(), verticle3.getConfig());
    assertEquals(2, verticle3.getInstances());
    ComponentConfig<?> verticle4 = network.addVerticle("verticle4", "verticle4.py", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("verticle4", verticle4.getName());
    assertTrue(verticle4.getType().equals(ComponentConfig.Type.VERTICLE));
    assertEquals("verticle4.py", ((DefaultVerticleConfig) verticle4).getMain());
    assertEquals("bar", verticle4.getConfig().getString("foo"));
    assertEquals(2, verticle4.getInstances());
    ComponentConfig<?> module1 = network.addModule("module1", "com.test~test-module~1.0");
    assertEquals("module1", module1.getName());
    assertTrue(module1.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~1.0", ((DefaultModuleConfig) module1).getModule());
    assertEquals(new JsonObject(), module1.getConfig());
    assertEquals(1, module1.getInstances());
    ComponentConfig<?> module2 = network.addModule("module2", "com.test~test-module~2.0", new JsonObject().putString("foo", "bar"));
    assertEquals("module2", module2.getName());
    assertTrue(module2.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~2.0", ((DefaultModuleConfig) module2).getModule());
    assertEquals("bar", module2.getConfig().getString("foo"));
    assertEquals(1, module2.getInstances());
    ComponentConfig<?> module3 = network.addModule("module3", "com.test~test-module~3.0", 2);
    assertEquals("module3", module3.getName());
    assertTrue(module3.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~3.0", ((DefaultModuleConfig) module3).getModule());
    assertEquals(new JsonObject(), module3.getConfig());
    assertEquals(2, module3.getInstances());
    ComponentConfig<?> module4 = network.addModule("module4", "com.test~test-module~4.0", new JsonObject().putString("foo", "bar"), 2);
    assertEquals("module4", module4.getName());
    assertTrue(module4.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals("com.test~test-module~4.0", ((DefaultModuleConfig) module4).getModule());
    assertEquals("bar", module4.getConfig().getString("foo"));
    assertEquals(2, module4.getInstances());
  }

  @Test
  public void testAddWorkerVerticle() {
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
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
    DefaultNetworkConfig network = new DefaultNetworkConfig("test");
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
  public void testConnectionParsedPort() {
    ConnectionConfig connection = new DefaultConnectionConfig("foo:notout", "bar:notin", new DefaultNetworkConfig("test"));
    assertEquals("foo", connection.getSource().getComponent());
    assertEquals("notout", connection.getSource().getPort());
    assertEquals("bar", connection.getTarget().getComponent());
    assertEquals("notin", connection.getTarget().getPort());
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
  public void testCreateConnectionParsedPort() {
    NetworkConfig network = new DefaultNetworkConfig("test");
    ConnectionConfig connection = network.createConnection("foo:notout", "bar:notin");
    assertEquals("foo", connection.getSource().getComponent());
    assertEquals("notout", connection.getSource().getPort());
    assertEquals("bar", connection.getTarget().getComponent());
    assertEquals("notin", connection.getTarget().getPort());
    boolean exists = false;
    for (ConnectionConfig other : network.getConnections()) {
      if (other.equals(connection)) {
        exists = true;
      }
    }
    assertTrue(exists);
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
        .putString(DefaultNetworkConfig.NETWORK_NAME, "test")
        .putObject(DefaultNetworkConfig.NETWORK_COMPONENTS, new JsonObject());
    NetworkConfig network = new Vertigo(null, null).createNetworkFromJson(json);
    assertEquals("test", network.getName());
  }

  @Test
  public void testAddFeederModuleFromJson() {
    JsonObject json = new JsonObject().putString(DefaultNetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(DefaultModuleConfig.COMPONENT_NAME, "feeder")
        .putString(DefaultModuleConfig.COMPONENT_TYPE, DefaultModuleConfig.COMPONENT_TYPE_MODULE)
        .putString(DefaultModuleConfig.MODULE_NAME, "com.test~test-module~1.0")
        .putObject(DefaultModuleConfig.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(DefaultModuleConfig.COMPONENT_NUM_INSTANCES, 2);
    json.putObject(DefaultNetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    NetworkConfig network = new Vertigo(null, null).createNetworkFromJson(json);
    assertEquals("test", network.getName());
    DefaultModuleConfig module = network.getComponent("feeder");
    assertEquals("feeder", module.getName());
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals("bar", module.getConfig().getString("foo"));
    assertEquals(2, module.getInstances());
    assertTrue(module.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals(0, module.getHooks().size());
  }

  @Test
  public void testAddFeederVerticleFromJson() {
    JsonObject json = new JsonObject().putString(DefaultNetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(DefaultVerticleConfig.COMPONENT_NAME, "feeder")
        .putString(DefaultVerticleConfig.COMPONENT_TYPE, DefaultVerticleConfig.COMPONENT_TYPE_VERTICLE)
        .putString(DefaultVerticleConfig.VERTICLE_MAIN, "test.py")
        .putObject(DefaultVerticleConfig.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(DefaultVerticleConfig.COMPONENT_NUM_INSTANCES, 2)
        .putBoolean(DefaultVerticleConfig.VERTICLE_IS_WORKER, true)
        .putBoolean(DefaultVerticleConfig.VERTICLE_IS_MULTI_THREADED, true);
    json.putObject(DefaultNetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    NetworkConfig network = new Vertigo(null, null).createNetworkFromJson(json);
    assertEquals("test", network.getName());
    DefaultVerticleConfig verticle = network.getComponent("feeder");
    assertEquals("feeder", verticle.getName());
    assertEquals("test.py", verticle.getMain());
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(2, verticle.getInstances());
    assertTrue(verticle.getType().equals(ComponentConfig.Type.VERTICLE));
    assertTrue(verticle.isWorker());
    assertTrue(verticle.isMultiThreaded());
    assertEquals(0, verticle.getHooks().size());
  }

  @Test
  public void testAddWorkerModuleFromJson() {
    JsonObject json = new JsonObject().putString(DefaultNetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonWorker = new JsonObject()
        .putString(DefaultModuleConfig.COMPONENT_NAME, "worker")
        .putString(DefaultModuleConfig.COMPONENT_TYPE, DefaultModuleConfig.COMPONENT_TYPE_MODULE)
        .putString(DefaultModuleConfig.MODULE_NAME, "com.test~test-module~1.0")
        .putObject(DefaultModuleConfig.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(DefaultModuleConfig.COMPONENT_NUM_INSTANCES, 2);
    json.putObject(DefaultNetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("worker", jsonWorker));
    NetworkConfig network = new Vertigo(null, null).createNetworkFromJson(json);
    assertEquals("test", network.getName());
    DefaultModuleConfig module = network.getComponent("worker");
    assertEquals("worker", module.getName());
    assertEquals("com.test~test-module~1.0", module.getModule());
    assertEquals("bar", module.getConfig().getString("foo"));
    assertEquals(2, module.getInstances());
    assertTrue(module.getType().equals(ComponentConfig.Type.MODULE));
    assertEquals(0, module.getHooks().size());
  }

  @Test
  public void testAddWorkerVerticleFromJson() {
    JsonObject json = new JsonObject().putString(DefaultNetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonWorker = new JsonObject()
        .putString(DefaultVerticleConfig.COMPONENT_NAME, "worker")
        .putString(DefaultVerticleConfig.COMPONENT_TYPE, DefaultVerticleConfig.COMPONENT_TYPE_VERTICLE)
        .putString(DefaultVerticleConfig.VERTICLE_MAIN, "test.py")
        .putObject(DefaultVerticleConfig.COMPONENT_CONFIG, new JsonObject().putString("foo", "bar"))
        .putNumber(DefaultVerticleConfig.COMPONENT_NUM_INSTANCES, 2)
        .putBoolean(DefaultVerticleConfig.VERTICLE_IS_WORKER, true)
        .putBoolean(DefaultVerticleConfig.VERTICLE_IS_MULTI_THREADED, true);
    json.putObject(DefaultNetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("worker", jsonWorker));
    NetworkConfig network = new Vertigo(null, null).createNetworkFromJson(json);
    assertEquals("test", network.getName());
    DefaultVerticleConfig verticle = network.getComponent("worker");
    assertEquals("worker", verticle.getName());
    assertEquals("test.py", verticle.getMain());
    assertEquals("bar", verticle.getConfig().getString("foo"));
    assertEquals(2, verticle.getInstances());
    assertTrue(verticle.getType().equals(ComponentConfig.Type.VERTICLE));
    assertTrue(verticle.isWorker());
    assertTrue(verticle.isMultiThreaded());
    assertEquals(0, verticle.getHooks().size());
  }

  @Test
  public void testAddConnectionFromJson() {
    JsonObject json = new JsonObject().putString(DefaultNetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonConnection = new JsonObject()
        .putObject("source", new JsonObject().putString("component", "foo").putString("port", "notout"))
        .putObject("target", new JsonObject().putString("component", "bar").putString("port", "notin"))
        .putObject("grouping", new JsonObject().putString("type", "random"));
    json.putArray("connections", new JsonArray().add(jsonConnection));
    NetworkConfig network = new Vertigo(null, null).createNetworkFromJson(json);
    assertEquals("test", network.getName());
    ConnectionConfig connection = network.getConnections().iterator().next();
    assertEquals("foo", connection.getSource().getComponent());
    assertEquals("notout", connection.getSource().getPort());
    assertEquals("bar", connection.getTarget().getComponent());
    assertEquals("notin", connection.getTarget().getPort());
  }

  @Test
  public void testAddHookFromJson() {
    JsonObject json = new JsonObject().putString(DefaultNetworkConfig.NETWORK_NAME, "test");
    JsonObject jsonFeeder = new JsonObject()
        .putString(DefaultVerticleConfig.COMPONENT_NAME, "feeder")
        .putString(DefaultVerticleConfig.COMPONENT_TYPE, DefaultVerticleConfig.Type.VERTICLE.getName())
        .putString(DefaultVerticleConfig.VERTICLE_MAIN, "test.py");
    JsonObject jsonHook = new JsonObject().putString("type", TestHook.class.getName());
    jsonFeeder.putArray(DefaultVerticleConfig.COMPONENT_HOOKS, new JsonArray().add(jsonHook));
    json.putObject(DefaultNetworkConfig.NETWORK_COMPONENTS, new JsonObject().putObject("feeder", jsonFeeder));
    NetworkConfig network = new Vertigo(null, null).createNetworkFromJson(json);
    assertEquals("test", network.getName());
    DefaultVerticleConfig feeder = network.getComponent("feeder");
    assertNotNull(feeder);
    List<ComponentHook> hooks = feeder.getHooks();
    assertEquals(1, hooks.size());
    assertTrue(hooks.get(0) instanceof TestHook);
  }

  public static class TestHook implements ComponentHook {
    @Override
    public void handleStart(net.kuujo.vertigo.component.Component subject) {
      
    }
    @Override
    public void handleStop(net.kuujo.vertigo.component.Component subject) {
      
    }
    @Override
    public void handleReceive(String port, String String) {
      
    }
    @Override
    public void handleSend(String port, String String) {
      
    }
  }

}
