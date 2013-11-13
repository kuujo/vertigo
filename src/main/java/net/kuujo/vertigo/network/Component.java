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

import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.input.filter.Filter;
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

  @SuppressWarnings("unchecked")
  public T addHook(ComponentHook hook) {
    JsonObject hooksInfo = definition.getObject(HOOKS);
    if (hooksInfo == null) {
      hooksInfo = new JsonObject();
      definition.putObject(HOOKS, hooksInfo);
    }

    // We support two types of hooks. If a hook class implement the Serializable
    // interface, then serialize and store its state, otherwise simply store the
    // class name. If only the class name is stored, it must have a default constructor.
    if (hook instanceof Serializable) {
      JsonArray serializable = hooksInfo.getArray(SERIALIZABLE_HOOKS);
      if (serializable == null) {
        serializable = new JsonArray();
        hooksInfo.putArray(SERIALIZABLE_HOOKS, serializable);
      }
      serializable.add(Serializer.serialize((Serializable) hook));
    }
    else {
      JsonArray bare = hooksInfo.getArray(BARE_HOOKS);
      if (bare == null) {
        bare = new JsonArray();
        hooksInfo.putArray(BARE_HOOKS, bare);
      }
      bare.add(new JsonObject().putString("type", hook.getClass().getName()));
    }
    return (T) this;
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

  /**
   * Adds a component input with filters.
   *
   * @param address
   *   The input address. This is the event bus address of a component to which
   *   this component will listen for output.
   * @param filters
   *   A list of input filters.
   * @return
   *   The new input instance.
   */
  public Input addInput(String address, Filter... filters) {
    Input input = addInput(new Input(address));
    for (Filter filter : filters) {
      input.filterBy(filter);
    }
    return input;
  }

  /**
   * Adds a component input with grouping and filters.
   *
   * @param address
   *   The input address. This is the event bus address of a component to which
   *   this component will listen for output.
   * @param grouping
   *   An input grouping. This input grouping helps determine how messages will
   *   be distributed among multiple instances of this component.
   * @param filters
   *   A list of input filters.
   * @return
   *   The new input instance.
   */
  public Input addInput(String address, Grouping grouping, Filter... filters) {
    Input input = addInput(new Input(address).groupBy(grouping));
    for (Filter filter : filters) {
      input.filterBy(filter);
    }
    return input;
  }

  /**
   * Creates a component context from the component definition.
   *
   * @return
   *   A component context.
   * @throws MalformedNetworkException
   *   If the component definition is invalid.
   */
  public abstract ComponentContext createContext() throws MalformedNetworkException;

  /**
   * Creates a JSON context.
   */
  protected JsonObject createJsonContext() {
    JsonObject context = definition.copy();
    String address = context.getString(ADDRESS);
    if (address == null) {
      address = UUID.randomUUID().toString();
      context.putString(ADDRESS, address);
    }

    JsonObject config = context.getObject(CONFIG);
    if (config == null) {
      config = new JsonObject();
      context.putObject(CONFIG, config);
    }

    JsonObject hooks = context.getObject(HOOKS);
    if (hooks == null) {
      hooks = new JsonObject();
      context.putObject(HOOKS, hooks);
    }

    JsonArray inputs = context.getArray(INPUTS);
    if (inputs == null) {
      inputs = new JsonArray();
      context.putArray(INPUTS, inputs);
    }

    JsonArray instances = new JsonArray();
    int numInstances = getInstances();
    for (int i = 0; i < numInstances; i++) {
      String id = UUID.randomUUID().toString();
      instances.add(InstanceContext.fromJson(new JsonObject().putString("id", id)).getState());
    }
    context.putArray(INSTANCES, instances);
    return context;
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
