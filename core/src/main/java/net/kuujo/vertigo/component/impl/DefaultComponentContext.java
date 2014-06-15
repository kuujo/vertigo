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
package net.kuujo.vertigo.component.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.component.ModuleContext;
import net.kuujo.vertigo.component.VerticleContext;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.impl.BaseContext;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;

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
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
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
  protected String name;
  protected String status;
  protected String group;
  protected Map<String, Object> config;
  protected List<DefaultInstanceContext> instances = new ArrayList<>();
  protected List<ComponentHook> hooks = new ArrayList<>();
  @JsonIgnore
  protected DefaultNetworkContext network;

  /**
   * Returns the component deployment type.
   */
  @JsonGetter("type")
  protected abstract String type();

  @SuppressWarnings("unchecked")
  public T setNetworkContext(DefaultNetworkContext network) {
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
    return (ModuleContext) this;
  }

  @Override
  public VerticleContext asVerticle() {
    return (VerticleContext) this;
  }

  @Override
  public NetworkContext network() {
    return network;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void notify(T update) {
    if (update == null) {
      for (InstanceContext instance : instances) {
        instance.notify(null);
      }
      instances.clear();
    } else {
      Iterator<DefaultInstanceContext> iter = instances.iterator();
      while (iter.hasNext()) {
        DefaultInstanceContext instance = iter.next();
        InstanceContext match = null;
        for (InstanceContext i : update.instances()) {
          if (instance.equals(i)) {
            match = i;
            break;
          }
        }
        if (match != null) {
          instance.notify(match);
        } else {
          instance.notify(null);
          iter.remove();
        }
      }
    }
    super.notify((T) this);
  }

  @Override
  public String uri() {
    return String.format("%s://%s/%s", network.cluster(), network.name(), name);
  }

  @Override
  public String toString() {
    return address();
  }

}
