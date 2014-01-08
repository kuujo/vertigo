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

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.serializer.Serializer;
import net.kuujo.vertigo.serializer.SerializerFactory;

/**
 * Component input context.
 *
 * @author Jordan Halterman
 */
public class InputContext implements Context {
  private static final String DEFAULT_STREAM = "default";
  private String id;
  private String stream = DEFAULT_STREAM;
  private String address;
  private Grouping grouping;
  private @JsonIgnore ComponentContext<?> component;

  private InputContext() {
  }

  /**
   * Creates a new input context from JSON.
   *
   * @param context
   *   A JSON representation of the input context.
   * @return
   *   A new input context instance.
   * @throws MalformedContextException
   *   If the JSON context is malformed.
   */
  public static InputContext fromJson(JsonObject context) {
    Serializer serializer = SerializerFactory.getSerializer(Context.class);
    InputContext input = serializer.deserialize(context.getObject("input"), InputContext.class);
    ComponentContext<?> component = ComponentContext.fromJson(context);
    return input.setComponentContext(component);
  }

  /**
   * Serializes an input context to JSON.
   *
   * @param context
   *   The input context to serialize.
   * @return
   *   A Json representation of the input context.
   */
  public static JsonObject toJson(InputContext context) {
    Serializer serializer = SerializerFactory.getSerializer(Context.class);
    JsonObject json = ComponentContext.toJson(context.componentContext());
    return json.putObject("input", serializer.serialize(context));
  }

  /**
   * Sets the input's parent context.
   */
  InputContext setComponentContext(ComponentContext<?> component) {
    this.component = component;
    return this;
  }

  /**
   * Returns the input ID.
   *
   * @return
   *   The unique input identifier.
   */
  public String id() {
    return id;
  }

  /**
   * Returns the address to which the input subscribes.
   *
   * @return
   *   The input address.
   */
  public String address() {
    return address;
  }

  /**
   * Returns the stream to which the input subscribes.
   *
   * @return
   *   The input stream.
   */
  public String stream() {
    return stream;
  }

  /**
   * Returns the grouping used to partition input between multiple component instances.
   *
   * @return
   *   The input grouping.
   */
  public Grouping grouping() {
    return grouping;
  }

  /**
   * Returns the total number of input partitions.
   *
   * @return
   *   The input count.
   */
  public int count() {
    return component.numInstances();
  }

  /**
   * Returns the parent component context.
   *
   * @return
   *   The parent component context.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T extends ComponentContext> T componentContext() {
    return (T) component;
  }

  @Override
  public String toString() {
    return address();
  }

}
