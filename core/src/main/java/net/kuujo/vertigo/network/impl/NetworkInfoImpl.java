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

package net.kuujo.vertigo.network.impl;

import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.NetworkInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Network info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NetworkInfoImpl extends BaseTypeInfoImpl<NetworkInfo> implements NetworkInfo {
  private String name;
  private String version;
  private Network config;
  private Map<String, ComponentInfo> components = new HashMap<>();

  @Override
  public String name() {
    return name;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public Network config() {
    return config;
  }

  @Override
  public Collection<ComponentInfo> components() {
    return components.values();
  }

  @Override
  public boolean hasComponent(String name) {
    return components.containsKey(name);
  }

  @Override
  public ComponentInfo component(String name) {
    return components.get(name);
  }

}
