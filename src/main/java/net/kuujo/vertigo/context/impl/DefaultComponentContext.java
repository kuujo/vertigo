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
package net.kuujo.vertigo.context.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A component context which contains information regarding each component instance within
 * a single network component. Contexts are immutable as they are constructed once a
 * network has been deployed.
 * 
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.NAME,
  include=JsonTypeInfo.As.PROPERTY,
  property="type"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value=DefaultModuleContext.class, name="module"),
  @JsonSubTypes.Type(value=DefaultVerticleContext.class, name="verticle")
})
public abstract class DefaultComponentContext<T extends ComponentContext<T>> extends BaseContext<T> implements ComponentContext<T> {
  private static final String DEFAULT_GROUP = "__DEFAULT__";
  protected String name;
  protected String address;
  protected String status;
  protected String group = DEFAULT_GROUP;
  protected Map<String, Object> config;
  protected List<DefaultInstanceContext> instances = new ArrayList<>();
  protected List<ComponentHook> hooks = new ArrayList<>();
  private @JsonIgnore
  DefaultNetworkContext network;

  /**
   * Creates a component context from JSON.
   * 
   * @param context A JSON representation of the component context.
   * @return A component context instance.
   * @throws MalformedContextException If the context is malformed.
   */
  @SuppressWarnings("unchecked")
  public static <T extends DefaultComponentContext<T>> T fromJson(JsonObject context) {
    Serializer serializer = SerializerFactory.getSerializer(ComponentContext.class);
    T component = (T) serializer.deserializeObject(context.getObject("component"), ComponentContext.class);
    DefaultNetworkContext network = DefaultNetworkContext.fromJson(context);
    return (T) component.setNetworkContext(network);
  }

  /**
   * Serializes a component context to JSON.
   * 
   * @param context The component context to serialize.
   * @return A Json representation of the component context.
   */
  @SuppressWarnings("rawtypes")
  public static JsonObject toJson(ComponentContext context) {
    Serializer serializer = SerializerFactory.getSerializer(ComponentContext.class);
    JsonObject json = DefaultNetworkContext.toJson(context.network());
    json.putObject("component", serializer.serializeToObject(context));
    return json;
  }

  /**
   * Returns the component deployment type.
   */
  @JsonGetter("type")
  protected abstract String type();

  /**
   * Sets the component parent.
   */
  @SuppressWarnings("unchecked")
  T setNetworkContext(DefaultNetworkContext network) {
    this.network = network;
    return (T) this;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public String status() {
    return status;
  }

  @Override
  public boolean isModule() {
    return false;
  }

  @Override
  public boolean isVerticle() {
    return false;
  }

  @Override
  public JsonObject config() {
    return config != null ? new JsonObject(config) : new JsonObject();
  }

  @Override
  public List<InstanceContext> instances() {
    List<InstanceContext> instances = new ArrayList<>();
    for (DefaultInstanceContext instance : this.instances) {
      instances.add(instance.setComponentContext(this));
    }
    return instances;
  }

  @Override
  public int numInstances() {
    return instances.size();
  }

  @Override
  public InstanceContext instance(int instanceNumber) {
    for (DefaultInstanceContext instance : instances) {
      if (instance.number() == instanceNumber) {
        return instance.setComponentContext(this);
      }
    }
    return null;
  }

  @Override
  public InstanceContext instance(String address) {
    for (DefaultInstanceContext instance : instances) {
      if (instance.address().equals(address)) {
        return instance.setComponentContext(this);
      }
    }
    return null;
  }

  @Override
  public String group() {
    return group;
  }

  @Override
  public List<ComponentHook> hooks() {
    return hooks;
  }

  @Override
  public ModuleContext asModule() {
    return (DefaultModuleContext) this;
  }

  @Override
  public VerticleContext asVerticle() {
    return (DefaultVerticleContext) this;
  }

  /**
   * Returns the parent network context.
   * 
   * @return The parent network context.
   */
  public DefaultNetworkContext network() {
    return network;
  }

  @Override
  public void notify(T update) {
    super.notify(update);
    for (InstanceContext instance : instances) {
      boolean updated = false;
      for (InstanceContext i : update.instances()) {
        if (instance.equals(i)) {
          instance.notify(i);
          updated = true;
          break;
        }
      }
      if (!updated) {
        instance.notify(null);
      }
    }
  }

  @Override
  public String toString() {
    return address();
  }

}
