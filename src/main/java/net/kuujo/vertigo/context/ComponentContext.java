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
import java.util.Map;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.serializer.Serializer;
import net.kuujo.vertigo.serializer.Serializers;

/**
 * A component context.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
  @JsonSubTypes.Type(value=ModuleContext.class, name=ComponentContext.MODULE),
  @JsonSubTypes.Type(value=VerticleContext.class, name=ComponentContext.VERTICLE)
})
public abstract class ComponentContext {
  public static final String VERTICLE = "verticle";
  public static final String MODULE = "module";

  protected String address;
  protected Map<String, Object> config;
  protected List<InstanceContext> instances = new ArrayList<>();
  protected long heartbeat = 5000;
  protected List<ComponentHook> hooks = new ArrayList<>();
  protected List<Input> inputs = new ArrayList<>();
  protected @JsonIgnore NetworkContext network;

  protected ComponentContext() {
  }

  /**
   * Creates a component context from JSON.
   *
   * @param context
   *   A JSON representation of the component context.
   * @return
   *   A component context instance.
   * @throws MalformedContextException
   *   If the context is malformed.
   */
  public static ComponentContext fromJson(JsonObject context) {
    Serializer serializer = Serializers.getDefault();
    ComponentContext component = serializer.deserialize(context.getObject("component"), ComponentContext.class);
    NetworkContext network = NetworkContext.fromJson(context);
    return component.setParent(network);
  }

  /**
   * Serializes a component context to JSON.
   *
   * @param context
   *   The component context to serialize.
   * @return
   *   A Json representation of the component context.
   */
  public static JsonObject toJson(ComponentContext context) {
    Serializer serializer = Serializers.getDefault();
    JsonObject json = NetworkContext.toJson(context.getNetwork());
    json.putObject("component", serializer.serialize(context));
    return json;
  }

  /**
   * Sets the component parent.
   */
  ComponentContext setParent(NetworkContext network) {
    this.network = network;
    return this;
  }

  /**
   * Gets the component address.
   *
   * @return
   *   The component address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Gets the component type.
   *
   * @return
   *   The component type.
   */
  public abstract String getType();

  /**
   * Returns a boolean indicating whether the component is a module.
   *
   * @return
   *   Indicates whether the component is a module.
   */
  public boolean isModule() {
    return getType().equals(ComponentContext.MODULE);
  }

  /**
   * Returns a boolean indicating whether the component is a verticle.
   *
   * @return
   *   Indicates whether the component is a verticle.
   */
  public boolean isVerticle() {
    return getType().equals(ComponentContext.VERTICLE);
  }

  /**
   * Gets the component configuration.
   *
   * @return
   *   The component configuration.
   */
  public JsonObject getConfig() {
    return config != null ? new JsonObject(config) : new JsonObject();
  }

  /**
   * Gets a list of all component instances.
   *
   * @return
   *   A list of component instance contexts.
   */
  public List<InstanceContext> getInstances() {
    for (InstanceContext instance : instances) {
      instance.setParent(this);
    }
    return instances;
  }

  /**
   * Gets a component instance by ID.
   *
   * @param id
   *   The instance ID.
   * @return
   *   A component instance or null if the instance doesn't exist.
   */
  public InstanceContext getInstance(String id) {
    for (InstanceContext instance : instances) {
      if (instance.id().equals(id)) {
        return instance.setParent(this);
      }
    }
    return null;
  }

  /**
   * Gets the component heartbeat interval.
   *
   * @return
   *   The component heartbeat interval.
   */
  public long getHeartbeatInterval() {
    return heartbeat;
  }

  /**
   * Gets a list of component hooks.
   *
   * @return
   *   A list of component hooks.
   */
  public List<ComponentHook> getHooks() {
    return hooks;
  }

  /**
   * Returns a list of component inputs.
   *
   * @return
   *   A list of component inputs.
   */
  public List<Input> getInputs() {
    return inputs;
  }

  /**
   * Returns the parent network context.
   *
   * @return
   *   The parent network context.
   */
  public NetworkContext getNetwork() {
    return network;
  }

}
