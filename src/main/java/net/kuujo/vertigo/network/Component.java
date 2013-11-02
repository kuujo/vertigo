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
  public static final String NUM_INSTANCES = "num_instances";
  public static final String HEARTBEAT_INTERVAL = "heartbeat";
  public static final String INPUTS = "inputs";

  protected JsonObject definition;

  public Component() {
    definition = new JsonObject();
    init();
  }

  public Component(String address) {
    definition = new JsonObject().putString(ADDRESS, address);
    init();
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
   * @return
   *   The component address.
   */
  public String getAddress() {
    return definition.getString(ADDRESS);
  }

  /**
   * Returns the component type.
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
  public int getNumInstances() {
    return definition.getInteger(NUM_INSTANCES, 1);
  }

  /**
   * Sets the number of component instances.
   *
   * @param numInstances
   *   The number of component instances.
   * @return
   *   The called component instance.
   */
  @SuppressWarnings("unchecked")
  public T setNumInstances(int numInstances) {
    definition.putNumber(NUM_INSTANCES, numInstances);

    JsonArray inputs = definition.getArray(INPUTS);
    JsonArray newInputs = new JsonArray();
    if (inputs == null) {
      inputs = new JsonArray();
    }

    for (Object inputInfo : inputs) {
      try {
        newInputs.add(Serializer.serialize(Serializer.<Input>deserialize((JsonObject) inputInfo).setCount(numInstances)));
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
    inputs.add(Serializer.serialize(input.setCount(getNumInstances())));
    return input;
  }

  /**
   * Adds a component input.
   *
   * @param address
   *   The input address.
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
   *   The input address.
   * @param selector
   *   An input selector.
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
   *   The input address.
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
   *   The input address.
   * @param grouping
   *   An input grouping.
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

    JsonArray inputs = context.getArray(INPUTS);
    if (inputs == null) {
      inputs = new JsonArray();
      context.putArray(INPUTS, inputs);
    }

    JsonArray instances = new JsonArray();
    int numInstances = getNumInstances();
    for (int i = 0; i < numInstances; i++) {
      String id = UUID.randomUUID().toString();
      instances.add(Serializer.serialize(new InstanceContext(new JsonObject().putString("id", id))));
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
