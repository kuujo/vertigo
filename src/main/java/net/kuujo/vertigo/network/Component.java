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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.SerializerFactory;
import static net.kuujo.vertigo.util.Component.isModuleName;
import static net.kuujo.vertigo.util.Component.isVerticleMain;
import static net.kuujo.vertigo.util.Component.serializeType;
import static net.kuujo.vertigo.util.Component.deserializeType;

/**
 * A network component definition.<p>
 *
 * Components are the primary elements of processing in Vertigo. They can
 * be represented as <code>feeders</code>, <code>executors</code>,
 * <code>workers</code>, or even custom implementations. Each network may
 * consist of any number of components, each of which may subscribe to the
 * output of other components inside or outside of the network. Internally,
 * components are represented as Vert.x modules or verticles. Just as with
 * Vert.x modules and verticles, components may consist of several instances.<p>
 *
 * @author Jordan Haltermam
 */
@SuppressWarnings("rawtypes")
public class Component<T extends net.kuujo.vertigo.component.Component> implements Serializable {
  private String address;
  private Class<T> type;
  private String main;
  private Map<String, Object> config;
  private int instances = 1;
  private long heartbeat = 5000;
  private List<ComponentHook> hooks = new ArrayList<>();
  private List<Input> inputs = new ArrayList<>();

  public Component() {
    address = UUID.randomUUID().toString();
  }

  public Component(Class<T> type, String address, String main) {
    this.type = type;
    this.address = address;
    this.main = main;
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
  @SuppressWarnings("unchecked")
  public static <T extends net.kuujo.vertigo.component.Component<T>> Component<T> fromJson(JsonObject json) throws MalformedNetworkException {
    try {
      return SerializerFactory.getSerializer(Component.class).deserialize(json);
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
   * Sets the component type.
   *
   * @param type
   *   The component type.
   * @return
   *   The called component instance.
   */
  public Component<T> setType(Class<T> type) {
    this.type = type;
    return this;
  }

  /**
   * Gets the component type.
   *
   * @return
   *   The component type.
   */
  public Class<T> getType() {
    return type;
  }

  @JsonGetter("type")
  private String getSerializedType() {
    return serializeType(type);
  }

  @JsonSetter("type")
  @SuppressWarnings("unchecked")
  private void setSerializedType(String type) {
    this.type = (Class<T>) deserializeType(type);
  }

  /**
   * Sets the component module name.
   *
   * @param moduleName
   *   The component module name.
   * @return
   *   The called component instance.
   */
  public Component<T> setModule(String moduleName) {
    if (!isModuleName(moduleName)) {
      throw new IllegalArgumentException(moduleName + " is not a valid module name.");
    }
    main = moduleName;
    return this;
  }

  /**
   * Gets the component module name.
   *
   * @return
   *   The component module name.
   */
  public String getModule() {
    return main;
  }

  /**
   * Sets the component verticle main.
   *
   * @param main
   *   The component verticle main.
   * @return
   *   The called component instance.
   */
  public Component<T> setMain(String main) {
    if (!isVerticleMain(main)) {
      throw new IllegalArgumentException(main + " is not a valid main.");
    }
    this.main = main;
    return this;
  }

  /**
   * Gets the component verticle main.
   *
   * @return
   *   The component verticle main.
   */
  public String getMain() {
    return main;
  }

  /**
   * Returns a boolean indicating whether the component is a module.
   *
   * @return
   *   Indicates whether the component is a module.
   */
  public boolean isModule() {
    return main != null && isModuleName(main);
  }

  /**
   * Returns a boolean indicating whether the component is a verticle.
   *
   * @return
   *   Indicates whether the component is a verticle.
   */
  public boolean isVerticle() {
    return main != null && isVerticleMain(main);
  }

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
  public Component<T> setConfig(JsonObject config) {
    this.config = config.toMap();
    return this;
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
  public Component<T> setInstances(int instances) {
    this.instances = instances;
    for (Input input : inputs) {
      input.setCount(instances);
    }
    return this;
  }

  /**
   * Returns the component heartbeat interval.
   *
   * @return
   *   The component heartbeat interval.
   */
  @Deprecated
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
  @Deprecated
  public Component<T> setHeartbeatInterval(long interval) {
    heartbeat = interval;
    return this;
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
   * @see ComponentHook
   */
  public Component<T> addHook(ComponentHook hook) {
    hooks.add(hook);
    return this;
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
   * Adds a component input from another component.
   *
   * @param component
   *   The component from which to receive input.
   * @return
   *   The new input instance.
   */
  public Input addInput(Component<?> component) {
    return addInput(new Input(component.getAddress()));
  }

  /**
   * Adds a component input from another component on a specific stream.
   *
   * @param component
   *   The component from which to receive input.
   * @param stream
   *   The stream on which to receive input.
   * @return
   *   The new input instance.
   */
  public Input addInput(Component<?> component, String stream) {
    return addInput(new Input(component.getAddress(), stream));
  }

  /**
   * Adds a component input from another component.
   *
   * @param component
   *   The component from which to receive input.
   * @param grouping
   *   The grouping by which to group the input.
   * @return
   *   The new input instance.
   */
  public Input addInput(Component<?> component, Grouping grouping) {
    return addInput(new Input(component.getAddress()).groupBy(grouping));
  }

  /**
   * Adds a component input from another component on a specific stream.
   *
   * @param component
   *   The component from which to receive input.
   * @param stream
   *   The stream on which to receive input.
   * @param grouping
   *   The grouping by which to group the input.
   * @return
   *   The new input instance.
   */
  public Input addInput(Component<?> component, String stream, Grouping grouping) {
    return addInput(new Input(component.getAddress(), stream).groupBy(grouping));
  }

  /**
   * Adds a component input on the default stream.
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
   * Adds a component input on a specific stream.
   *
   * @param address
   *   The input address. This is the event bus address of a component to which
   *   this component will listen for output.
   * @param stream
   *   The stream on which to receive input.
   * @return
   *   The new input instance.
   */
  public Input addInput(String address, String stream) {
    return addInput(new Input(address, stream));
  }

  /**
   * Adds a component input on the default stream with a grouping.
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
   * Adds a component input on a specific stream with a grouping.
   *
   * @param address
   *   The input address. This is the event bus address of a component to which
   *   this component will listen for output.
   * @param stream
   *   The stream on which to receive input.
   * @param grouping
   *   An input grouping. This input grouping helps determine how messages will
   *   be distributed among multiple instances of this component.
   * @return
   *   The new input instance.
   */
  public Input addInput(String address, String stream, Grouping grouping) {
    return addInput(new Input(address, stream).groupBy(grouping));
  }

}
