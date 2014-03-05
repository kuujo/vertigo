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

import static net.kuujo.vertigo.util.Component.deserializeType;
import static net.kuujo.vertigo.util.Component.serializeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Input;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

/**
 * A component context which contains information regarding each
 * component instance within a single network component. Contexts
 * are immutable as they are constructed once a network has been
 * deployed.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="deploy")
@JsonSubTypes({
  @JsonSubTypes.Type(value=ModuleContext.class, name=Component.COMPONENT_DEPLOYMENT_MODULE),
  @JsonSubTypes.Type(value=VerticleContext.class, name=Component.COMPONENT_DEPLOYMENT_VERTICLE)
})
@SuppressWarnings("rawtypes")
public abstract class ComponentContext<T extends net.kuujo.vertigo.component.Component> implements Context {
  private String address;
  private Class<T> type;
  private Map<String, Object> config;
  private List<InstanceContext<T>> instances = new ArrayList<>();
  private long heartbeat = 5000;
  private List<ComponentHook> hooks = new ArrayList<>();
  private List<InputContext> inputs = new ArrayList<>();
  private @JsonIgnore NetworkContext network;

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
  @SuppressWarnings("unchecked")
  public static <T extends ComponentContext> T fromJson(JsonObject context) {
    Serializer serializer = SerializerFactory.getSerializer(Context.class);
    T component = (T) serializer.deserializeObject(context.getObject("component"), ComponentContext.class);
    NetworkContext network = NetworkContext.fromJson(context);
    return (T) component.setNetworkContext(network);
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
    Serializer serializer = SerializerFactory.getSerializer(Context.class);
    JsonObject json = NetworkContext.toJson(context.network());
    json.putObject("component", serializer.serializeToObject(context));
    return json;
  }

  /**
   * Returns the component deployment type.
   */
  @JsonGetter("deploy")
  protected abstract String getDeploymentType();

  /**
   * Sets the component parent.
   */
  ComponentContext setNetworkContext(NetworkContext network) {
    this.network = network;
    return this;
  }

  /**
   * Gets the unique component address.
   *
   * @return
   *   The component address.
   */
  public String address() {
    return address;
  }

  @Deprecated
  public String getAddress() {
    return address();
  }

  /**
   * Gets the component type.
   *
   * @return
   *   The component type.
   */
  public Class<T> type() {
    return type;
  }

  @Deprecated
  public Class<T> getType() {
    return type();
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
   * Returns a boolean indicating whether the component is a module.
   *
   * @return
   *   Indicates whether the component is a module.
   */
  public boolean isModule() {
    return false;
  }

  @Deprecated
  public String module() {
    return isModule() ? ((ModuleContext) this).module() : null;
  }

  @Deprecated
  public String getModule() {
    return module();
  }

  /**
   * Returns a boolean indicating whether the component is a verticle.
   *
   * @return
   *   Indicates whether the component is a verticle.
   */
  public boolean isVerticle() {
    return false;
  }

  @Deprecated
  public String main() {
    return isVerticle() ? ((VerticleContext) this).main() : null;
  }

  @Deprecated
  public String getMain() {
    return main();
  }

  /**
   * Gets the component configuration.
   *
   * @return
   *   The component configuration.
   */
  public JsonObject config() {
    return config != null ? new JsonObject(config) : new JsonObject();
  }

  @Deprecated
  public JsonObject getConfig() {
    return config();
  }

  /**
   * Gets a list of all component instance contexts.
   *
   * @return
   *   A list of component instance contexts.
   */
  public List<InstanceContext<T>> instances() {
    for (InstanceContext<T> instance : instances) {
      instance.setComponentContext(this);
    }
    return instances;
  }

  @Deprecated
  public List<InstanceContext<T>> getInstances() {
    return instances();
  }

  /**
   * Returns the number of component instances.
   *
   * @return
   *   The number of component instances.
   */
  public int numInstances() {
    return instances.size();
  }

  /**
   * Gets a component instance context by instance ID.
   *
   * @param id
   *   The instance ID.
   * @return
   *   A component instance or <code>null</code> if the instance doesn't exist.
   */
  public InstanceContext<T> instance(int instanceNumber) {
    for (InstanceContext<T> instance : instances) {
      if (instance.number() == instanceNumber) {
        return instance.setComponentContext(this);
      }
    }
    return null;
  }

  /**
   * Gets a component instance context by instance address.
   *
   * @param address
   *   The instance address.
   * @return
   *   A component instance or <code>null</code> if the instance doesn't exist.
   */
  public InstanceContext<T> instance(String address) {
    for (InstanceContext<T> instance : instances) {
      if (instance.address().equals(address)) {
        return instance.setComponentContext(this);
      }
    }
    return null;
  }

  @Deprecated
  public InstanceContext<T> getInstance(String address) {
    return instance(address);
  }

  /**
   * Gets the component heartbeat interval.
   *
   * @return
   *   The component heartbeat interval.
   */
  public long heartbeatInterval() {
    return heartbeat;
  }

  @Deprecated
  public long getHeartbeatInterval() {
    return heartbeatInterval();
  }

  /**
   * Gets a list of component hooks.
   *
   * @return
   *   A list of component hooks.
   */
  public List<ComponentHook> hooks() {
    return hooks;
  }

  @Deprecated
  public List<ComponentHook> getHooks() {
    return hooks();
  }

  /**
   * Returns a list of component input contexts.
   *
   * @return
   *   A list of component input contexts.
   */
  public List<InputContext> inputs() {
    for (InputContext input : inputs) {
      input.setComponentContext(this);
    }
    return inputs;
  }

  @Deprecated
  public List<Input> getInputs() {
    // Convert input contexts back to inputs to ensure backwards compatibility.
    Serializer serializer = SerializerFactory.getSerializer(Context.class);
    List<Input> inputs = new ArrayList<>();
    for (InputContext context : this.inputs) {
      inputs.add(serializer.deserializeString(serializer.serializeToString(context), Input.class));
    }
    return inputs;
  }

  /**
   * Returns the parent network context.
   *
   * @return
   *   The parent network context.
   */
  public NetworkContext network() {
    return network;
  }

  @Deprecated
  public NetworkContext getNetwork() {
    return network();
  }

  @Override
  public String toString() {
    return address();
  }

}
