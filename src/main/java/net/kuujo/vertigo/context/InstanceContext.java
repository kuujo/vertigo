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

import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.Serializer;
import net.kuujo.vertigo.serializer.Serializers;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A component instance context.
 *
 * @author Jordan Halterman
 */
public final class InstanceContext implements Serializable {
  private String id;
  private @JsonIgnore ComponentContext component;

  private InstanceContext() {
  }

  /**
   * Creates a new instance context from JSON.
   *
   * @param context
   *   A JSON representation of the instance context.
   * @return
   *   A new instance context instance.
   * @throws MalformedContextException
   *   If the JSON context is malformed.
   */
  public static InstanceContext fromJson(JsonObject context) {
    Serializer serializer = Serializers.getDefault();
    InstanceContext instance = serializer.deserialize(context.getObject("instance"), InstanceContext.class);
    ComponentContext component = ComponentContext.fromJson(context);
    return instance.setParent(component);
  }

  /**
   * Serializes an instance context to JSON.
   *
   * @param context
   *   The instance context to serialize.
   * @return
   *   A Json representation of the instance context.
   */
  public static JsonObject toJson(InstanceContext context) {
    Serializer serializer = Serializers.getDefault();
    JsonObject json = ComponentContext.toJson(context.getComponent());
    return json.putObject("instance", serializer.serialize(context));
  }

  /**
   * Sets the instance parent.
   */
  InstanceContext setParent(ComponentContext component) {
    this.component = component;
    return this;
  }

  /**
   * Returns the instance id.
   *
   * @return
   *   The unique instance id.
   */
  public String id() {
    return id;
  }

  /**
   * Indicates whether the component is a feeder.
   */
  public boolean isFeeder() {
    return component.isFeeder();
  }

  /**
   * Indicates whether the component is an executor.
   */
  public boolean isExecutor() {
    return component.isExecutor();
  }

  /**
   * Indicates whether the component is a worker.
   */
  public boolean isWorker() {
    return component.isWorker();
  }

  /**
   * Indicates whether the component is a filter.
   */
  public boolean isFilter() {
    return component.isFilter();
  }

  /**
   * Indicates whether the component is a splitter.
   */
  public boolean isSplitter() {
    return component.isSplitter();
  }

  /**
   * Returns the parent component context.
   *
   * @return
   *   The parent component context.
   */
  public ComponentContext getComponent() {
    return component;
  }

}
