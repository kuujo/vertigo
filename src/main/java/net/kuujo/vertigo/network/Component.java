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
package net.kuujo.vertigo.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A network component.
 *
 * @author Jordan Haltermam
 */
public abstract class Component<T extends Component<T>> implements Serializable {
  public static final String ADDRESS = "address";
  public static final String TYPE = "type";
  public static final String VERTICLE = "verticle";
  public static final String MODULE = "module";
  public static final String CONFIG = "config";
  public static final String INSTANCES = "instances";
  public static final String HEARTBEAT_INTERVAL = "heartbeat";
  public static final String HOOKS = "hooks";
  public static final String BARE_HOOKS = "bare";
  public static final String SERIALIZABLE_HOOKS = "serializable";
  public static final String INPUTS = "inputs";

  protected JsonObject definition;

  public Component() {
    definition = new JsonObject();
    init();
  }

  protected Component(JsonObject definition) {
    this.definition = definition;
  }

  public Component(String address) {
    definition = new JsonObject().putString(ADDRESS, address);
    init();
  }

  /**
   * Creates a component instance from JSON.
   *
   * @param json
   *   A JSON representation of the component instance.
   * @return
   *   A constructed component instance.
   * @throws MalformedNetworkException
   *   If the component definition is malformed.
   */
  public static Component<?> fromJson(JsonObject json) throws MalformedNetworkException {
    String type = json.getString(TYPE);
    if (type == null) {
      throw new MalformedNetworkException("Invalid component type. No type defined.");
    }

    switch (type) {
      case MODULE:
        return new Module(json);
      case VERTICLE:
        return new Verticle(json);
      default:
        throw new MalformedNetworkException(String.format("Invalid component type %s.", type));
    }
  }

  /**
   * Initializes the internal definition.
   */
  protected void init() {
    String address = definition.getString(ADDRESS);
    if (address == null) {
      address = UUID.randomUUID().toString();
      definition.putString(ADDRESS, address);
    }

    JsonArray inputs = definition.getArray(INPUTS);
    if (inputs == null) {
      definition.putArray(INPUTS, new JsonArray());
    }
  }

  /**
   * Returns the component address.
   *
   * This address is an event bus address at which the component will register
   * a handler to listen for connections when started. Thus, this address must
   * be unique.
   *
   * @return
   *   The component address.
   */
  public String getAddress() {
    return definition.getString(ADDRESS);
  }

  /**
   * Returns the component type, either "module" or "verticle".
   *
   * @return
   *   The component type.
   */
  public String getType() {
    return definition.getString(TYPE);
  }

  /**
   * Returns the component configuration.
   *
   * @return
   *   The component configuration.
   */
  public JsonObject getConfig() {
    return definition.getObject(CONFIG);
  }

  /**
   * Sets the component configuration.
   *
   * This configuration will be passed to component implementations as the verticle
   * or module configuration when the component is started.
   *
   * @param config
   *   The component configuration.
   * @return
   *   The called component instance.
   */
  @SuppressWarnings("unchecked")
  public T setConfig(JsonObject config) {
    definition.putObject(CONFIG, config);
    return (T) this;
  }

  /**
   * Returns the number of component instances.
   *
   * @return
   *   The number of component instances.
   */
  public int getInstances() {
    return definition.getInteger(INSTANCES, 1);
  }

  /**
   * Sets the number of component instances.
   *
   * @param instances
   *   The number of component instances.
   * @return
   *   The called component instance.
   */
  @SuppressWarnings("unchecked")
  public T setInstances(int instances) {
    definition.putNumber(INSTANCES, instances);

    JsonArray inputs = definition.getArray(INPUTS);
    JsonArray newInputs = new JsonArray();
    if (inputs == null) {
      inputs = new JsonArray();
    }

    for (Object inputInfo : inputs) {
      try {
        newInputs.add(Serializer.serialize(Serializer.<Input>deserialize((JsonObject) inputInfo).setCount(instances)));
      }
      catch (SerializationException e) {
        continue;
      }
    }
    definition.putArray(INPUTS, newInputs);
    return (T) this;
  }

  /**
   * Returns the component heartbeat interval.
   *
   * @return
   *   The component heartbeat interval.
   */
  public long getHeartbeatInterval() {
    return definition.getLong(HEARTBEAT_INTERVAL, 1000);
  }

  /**
   * Sets the component heartbeat interval.
   *
   * This is the interval at which the component will send heartbeat messages to
   * the network's coordinator. It may be necessary to increase heartbeat frequency
   * if the component blocks frequently.
   *
   * @param interval
   *   The component heartbeat interval.
   * @return
   *   The called component instance.
   */
  @SuppressWarnings("unchecked")
  public T setHeartbeatInterval(long interval) {
    definition.putNumber(HEARTBEAT_INTERVAL, interval);
    return (T) this;
  }

  /**
   * Adds a component hook to the component.
   *
   * The output hook can be used to receive notifications on events that occur
   * within the component instance's inputs and outputs. If the hook requires
   * constructor arguments or otherwise contains any state, the hook should
   * implement the {@link Serializable} interface. Serializable hooks will be
   * serialized with state, but non-serializable hooks will be reconstructed from
   * the class name only. This means for the hook to be properly started, it must
   * be available on the class path of the deployed component verticle or module.
   *
   * @param hook
   *   A component hook.
   * @return
   *   The called component instance.
   * @see {@link ComponentHook}
   */
  @SuppressWarnings("unchecked")
  public T addHook(ComponentHook hook) {
    JsonObject hookInfo = definition.getObject(HOOKS);
    if (hookInfo == null) {
      hookInfo = new JsonObject();
      definition.putObject(HOOKS, hookInfo);
    }

    if (hook instanceof Serializable) {
      JsonArray serializable = hookInfo.getArray(SERIALIZABLE_HOOKS);
      if (serializable == null) {
        serializable = new JsonArray();
        hookInfo.putArray(SERIALIZABLE_HOOKS, serializable);
      }
      serializable.add(Serializer.serialize((Serializable) hook));
    }
    else {
      JsonArray bare = hookInfo.getArray(BARE_HOOKS);
      if (bare == null) {
        bare = new JsonArray();
        hookInfo.putArray(BARE_HOOKS, bare);
      }
      bare.add(new JsonObject().putString("type", hook.getClass().getName()));
    }
    return (T) this;
  }

  /**
   * Returns a list of all component hooks.
   *
   * @return
   *   A list of component hooks.
   */
  public List<ComponentHook> getHooks() {
    List<ComponentHook> hooks = new ArrayList<>();
    JsonObject hookInfo = definition.getObject(HOOKS);
    if (hookInfo == null) {
      hookInfo = new JsonObject();
      definition.putObject(HOOKS, hookInfo);
    }

    JsonArray serializableHooks = hookInfo.getArray(SERIALIZABLE_HOOKS);
    if (serializableHooks != null) {
      for (Object serializedHook : serializableHooks) {
        try {
          ComponentHook hook = Serializer.deserialize((JsonObject) serializedHook);
          if (hook != null) {
            hooks.add(hook);
          }
        }
        catch (SerializationException e) {
          continue;
        }
      }
    }

    JsonArray bareHooks = hookInfo.getArray(BARE_HOOKS);
    if (bareHooks != null) {
      for (Object bareHook : bareHooks) {
        String className = ((JsonObject) bareHook).getString("type");
        try {
          hooks.add((ComponentHook) Class.forName(className).newInstance());
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
          continue;
        }
      }
    }
    return hooks;
  }

  /**
   * Gets a list of component inputs.
   *
   * @return
   *   A list of component inputs.
   */
  public List<Input> getInputs() {
    JsonArray inputsInfo = definition.getArray(INPUTS);
    if (inputsInfo == null) {
      inputsInfo = new JsonArray();
      definition.putArray(INPUTS, inputsInfo);
    }

    List<Input> inputs = new ArrayList<Input>();
    for (Object inputInfo : inputsInfo) {
      try {
        inputs.add(Serializer.<Input>deserialize((JsonObject) inputInfo));
      }
      catch (SerializationException e) {
        // Do nothing.
      }
    }
    return inputs;
  }

  /**
   * Adds a component input.
   *
   * @param input
   *   The input to add.
   * @return
   *   The new input instance.
   */
  public Input addInput(Input input) {
    JsonArray inputs = definition.getArray(INPUTS);
    if (inputs == null) {
      inputs = new JsonArray();
      definition.putArray(INPUTS, inputs);
    }
    inputs.add(Serializer.serialize(input.setCount(getInstances())));
    return input;
  }

  /**
   * Adds a component input.
   *
   * @param address
   *   The input address. This is the event bus address of a component to which
   *   this component will listen for output.
   * @return
   *   The new input instance.
   */
  public Input addInput(String address) {
    return addInput(new Input(address));
  }

  /**
   * Adds a component input with a grouping.
   *
   * @param address
   *   The input address. This is the event bus address of a component to which
   *   this component will listen for output.
   * @param grouping
   *   An input grouping. This input grouping helps determine how messages will
   *   be distributed among multiple instances of this component.
   * @return
   *   The new input instance.
   */
  public Input addInput(String address, Grouping grouping) {
    return addInput(new Input(address).groupBy(grouping));
  }

  @Override
  public JsonObject getState() {
    return definition;
  }

  @Override
  public void setState(JsonObject state) {
    definition = state;
  }

}
