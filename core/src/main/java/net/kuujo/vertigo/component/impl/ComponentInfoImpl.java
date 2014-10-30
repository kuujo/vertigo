/*
 * Copyright 2014 the original author or authors.
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

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.component.InstanceInfo;
import net.kuujo.vertigo.hook.ComponentHook;
import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.network.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Component info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentInfoImpl extends BaseTypeInfoImpl<ComponentInfo> implements ComponentInfo {
  private String name;
  private String main;
  private JsonObject config;
  private List<InstanceInfo> instances = new ArrayList<>();
  private boolean worker;
  private boolean multiThreaded;
  private List<ComponentHook> hooks = new ArrayList<>();
  private NetworkInfo network;

  @Override
  public String name() {
    return null;
  }

  @Override
  public String main() {
    return main;
  }

  @Override
  public JsonObject config() {
    return config;
  }

  @Override
  public List<InstanceInfo> instances() {
    return instances;
  }

  @Override
  public int numInstances() {
    return instances.size();
  }

  @Override
  public InstanceInfo instance(int instanceNumber) {
    InstanceInfo instance = null;
    for (InstanceInfo info : instances) {
      if (info.number() == instanceNumber) {
        instance = info;
        break;
      }
    }
    return instance;
  }

  @Override
  public InstanceInfo instance(String id) {
    InstanceInfo instance = null;
    for (InstanceInfo info : instances) {
      if (info.id().equals(id)) {
        instance = info;
        break;
      }
    }
    return instance;
  }

  @Override
  public boolean isWorker() {
    return worker;
  }

  @Override
  public boolean isMultiThreaded() {
    return multiThreaded;
  }

  @Override
  public List<ComponentHook> hooks() {
    return hooks;
  }

  @Override
  public NetworkInfo network() {
    return network;
  }

}
