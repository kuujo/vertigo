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

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

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

  @JsonProperty              protected String address;
  @JsonProperty              protected JsonObject config;
  @JsonProperty              protected List<InstanceContext> instances = new ArrayList<>();
  @JsonProperty("heartbeat") protected long heartbeatInterval;
  @JsonProperty              protected List<ComponentHook> hooks = new ArrayList<>();
  @JsonProperty              protected List<Input> inputs = new ArrayList<>();
  @JsonProperty              protected NetworkContext parent;

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
  public static ComponentContext fromJson(JsonObject context) throws MalformedContextException {
    try {
      return Serializer.getInstance().deserialize(context, ComponentContext.class);
    }
    catch (SerializationException e) {
      throw new MalformedContextException(e);
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
    if (config == null) {
      config = new JsonObject();
    }
    return config;
  }

  @JsonGetter("config")
  protected String getConfigEncoded() {
    return config != null ? config.encode() : null;
  }

  @JsonSetter("config")
  protected void setConfigEncoded(String encoded) {
    config = new JsonObject(encoded);
  }

  /**
   * Gets a list of all component instances.
   *
   * @return
   *   A list of component instance contexts.
   */
  public List<InstanceContext> getInstances() {
    return instances;
  }

  /**
   * Gets the component heartbeat interval.
   *
   * @return
   *   The component heartbeat interval.
   */
  public long getHeartbeatInterval() {
    return heartbeatInterval;
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
    return parent;
  }

}
