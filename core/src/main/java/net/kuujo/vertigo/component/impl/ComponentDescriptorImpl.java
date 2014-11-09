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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.component.ComponentDescriptor;
import net.kuujo.vertigo.util.Args;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Component descriptor implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ComponentDescriptorImpl implements ComponentDescriptor {
  private final String id;
  private final String main;
  private final JsonObject config;
  private final int partitions;
  private final boolean worker;
  private final boolean multiThreaded;
  private final Set<String> input;
  private final Set<String> output;
  private final List<String> resources;

  @SuppressWarnings("unchecked")
  public ComponentDescriptorImpl(JsonObject component) {
    this.id = component.getString("id", UUID.randomUUID().toString());
    this.main = Args.checkNotNull(component.getString("main"));
    this.config = component.getJsonObject("config", new JsonObject());
    this.partitions = component.getInteger("partitions", 1);
    this.worker = component.getBoolean("worker");
    this.multiThreaded = component.getBoolean("multi-threaded");
    this.input = new HashSet(component.getJsonArray("input", new JsonArray()).getList());
    this.output = new HashSet(component.getJsonArray("output", new JsonArray()).getList());
    this.resources = component.getJsonArray("resources", new JsonArray()).getList();
  }

  @Override
  public String id() {
    return id;
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
  public int partitions() {
    return partitions;
  }

  @Override
  public boolean worker() {
    return worker;
  }

  @Override
  public boolean multiThreaded() {
    return multiThreaded;
  }

  @Override
  public Set<String> input() {
    return input;
  }

  @Override
  public Set<String> output() {
    return output;
  }

  @Override
  public List<String> resources() {
    return resources;
  }

}
