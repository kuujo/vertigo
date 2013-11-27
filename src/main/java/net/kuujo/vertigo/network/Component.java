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
import java.util.Map;
import java.util.UUID;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A network component.
 *
 * @author Jordan Haltermam
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
  @JsonSubTypes.Type(value=Module.class, name=Component.MODULE),
  @JsonSubTypes.Type(value=Verticle.class, name=Component.VERTICLE)
})
public abstract class Component<T extends Component<T>> {
  public static final String MODULE = "module";
  public static final String VERTICLE = "verticle";

  protected String address;
  protected Map<String, Object> config;
  protected int instances = 1;
  protected long heartbeat = 5000;
  protected List<ComponentHook> hooks = new ArrayList<>();
  protected List<Input> inputs = new ArrayList<>();

  public Component() {
    address = UUID.randomUUID().toString();
  }

  public Component(String address) {
    this.address = address;
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
    try {
      return Serializer.getInstance().deserialize(json, Component.class);
    }
    catch (SerializationException e) {
      throw new MalformedNetworkException(e);
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
    return address;
  }

  /**
   * Returns the component type, either "module" or "verticle".
   *
   * @return
   *   The component type.
   */
  public abstract String getType();

  /**
   * Returns the component configuration.
   *
   * @return
   *   The component configuration.
   */
  public JsonObject getConfig() {
    return config != null ? new JsonObject(config) : new JsonObject();
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
    this.config = config.toMap();
    return (T) this;
  }

  /**
   * Returns the number of component instances.
   *
   * @return
   *   The number of component instances.
   */
  public int getInstances() {
    return instances;
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
    this.instances = instances;
    for (Input input : inputs) {
      input.setCount(instances);
    }
    return (T) this;
  }

  /**
   * Returns the component heartbeat interval.
   *
   * @return
   *   The component heartbeat interval.
   */
  public long getHeartbeatInterval() {
    return heartbeat;
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
    heartbeat = interval;
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
    hooks.add(hook);
    return (T) this;
  }

  /**
   * Returns a list of all component hooks.
   *
   * @return
   *   A list of component hooks.
   */
  public List<ComponentHook> getHooks() {
    return hooks;
  }

  /**
   * Gets a list of component inputs.
   *
   * @return
   *   A list of component inputs.
   */
  public List<Input> getInputs() {
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
    inputs.add(input);
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

}
