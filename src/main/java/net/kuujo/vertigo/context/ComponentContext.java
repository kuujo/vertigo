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
package net.kuujo.vertigo.context;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A component context.
 *
 * @author Jordan Halterman
 */
public abstract class ComponentContext implements Serializable {
  public static final String VERTICLE = "verticle";
  public static final String MODULE = "module";

  protected JsonObject context;
  protected NetworkContext parent;

  public ComponentContext() {
    context = new JsonObject();
  }

  public ComponentContext(JsonObject context) {
    this.context = context;
    if (context.getFieldNames().contains("parent")) {
      try {
        parent = Serializer.deserialize(context.getObject("parent"));
      }
      catch (SerializationException e) {
        // Invalid parent.
      }
    }
  }

  /**
   * Sets the component parent.
   */
  ComponentContext setParent(NetworkContext context) {
    parent = context;
    return this;
  }

  /**
   * Gets the component address.
   *
   * @return
   *   The component address.
   */
  public String getAddress() {
    return context.getString(Component.ADDRESS);
  }

  /**
   * Gets the component type.
   *
   * @return
   *   The component type.
   */
  public String getType() {
    return context.getString(Component.TYPE);
  }

  /**
   * Returns a boolean indicating whether the component is a module.
   *
   * @return
   *   Indicates whether the component is a module.
   */
  public boolean isModule() {
    return getType().equals(Component.MODULE);
  }

  /**
   * Returns a boolean indicating whether the component is a verticle.
   *
   * @return
   *   Indicates whether the component is a verticle.
   */
  public boolean isVerticle() {
    return getType().equals(Component.VERTICLE);
  }

  /**
   * Gets the component configuration.
   *
   * @return
   *   The component configuration.
   */
  public JsonObject getConfig() {
    JsonObject config = context.getObject(Component.CONFIG);
    if (config == null) {
      config = new JsonObject();
    }
    return config;
  }

  /**
   * Gets the number of component instances.
   *
   * @return
   *   The number of component instances.
   */
  public int getNumInstances() {
    return context.getInteger(Component.NUM_INSTANCES, 1);
  }

  /**
   * Gets a list of all component instances.
   *
   * @return
   *   A list of component instance contexts.
   */
  public List<InstanceContext> getInstances() {
    JsonArray instancesInfo = context.getArray(Component.INSTANCES);
    if (instancesInfo == null) {
      instancesInfo = new JsonArray();
    }
    List<InstanceContext> instances = new ArrayList<InstanceContext>();
    for (Object instanceInfo : instancesInfo) {
      try {
        InstanceContext instance = Serializer.<InstanceContext>deserialize((JsonObject) instanceInfo).setParent(this);
        if (instance != null) {
          instances.add(instance);
        }
      }
      catch (SerializationException e) {
        continue;
      }
    }
    return instances;
  }

  /**
   * Gets the component heartbeat interval.
   *
   * @return
   *   The component heartbeat interval.
   */
  public long getHeartbeatInterval() {
    return context.getLong(Component.HEARTBEAT_INTERVAL, 1000);
  }

  /**
   * Returns a list of component inputs.
   *
   * @return
   *   A list of component inputs.
   */
  public List<Input> getInputs() {
    JsonArray inputsInfo = context.getArray(Component.INPUTS);
    if (inputsInfo == null) {
      inputsInfo = new JsonArray();
    }

    List<Input> inputs = new ArrayList<>();
    for (Object inputInfo : inputsInfo) {
      try {
        Input input = Serializer.deserialize((JsonObject) inputInfo);
        if (input != null) {
          inputs.add(input);
        }
      }
      catch (SerializationException e) {
        continue;
      }
    }
    return inputs;
  }

  /**
   * Returns the parent network context.
   *
   * @return
   *   The parent network context.
   */
  public NetworkContext getNetwork() {
    return parent;
  }

  @Override
  public JsonObject getState() {
    JsonObject context = this.context.copy();
    if (parent != null) {
      context.putObject("parent", Serializer.serialize(parent));
    }
    return context;
  }

  @Override
  public void setState(JsonObject state) {
    context = state.copy();
    if (context.getFieldNames().contains("parent")) {
      try {
        parent = Serializer.deserialize(context.getObject("parent"));
      }
      catch (SerializationException e) {
        // Invalid parent.
      }
    }
  }

}
