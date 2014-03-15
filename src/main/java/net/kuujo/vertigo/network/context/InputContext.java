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
package net.kuujo.vertigo.network.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Input context represents output information between a
 * source component and a target component. This information
 * is used to indicate where the component should listen
 * for messages.
 *
 * @author Jordan Halterman
 */
public class InputContext extends IOContext<InputContext> {
  private Collection<InputStreamContext> streams = new ArrayList<>();

  /**
   * Creates a new input context from JSON.
   * 
   * @param context A JSON representation of the input context.
   * @return A new input context instance.
   * @throws MalformedContextException If the JSON context is malformed.
   */
  public static InputContext fromJson(JsonObject context) {
    Serializer serializer = SerializerFactory.getSerializer(InstanceContext.class);
    InputContext input = serializer.deserializeObject(context.getObject("input"), InputContext.class);
    InstanceContext instance = InstanceContext.fromJson(context);
    return input.setInstanceContext(instance);
  }

  /**
   * Serializes an input context to JSON.
   * 
   * @param context The input context to serialize.
   * @return A Json representation of the input context.
   */
  public static JsonObject toJson(InputContext context) {
    Serializer serializer = SerializerFactory.getSerializer(InstanceContext.class);
    JsonObject json = InstanceContext.toJson(context.instance());
    return json.putObject("input", serializer.serializeToObject(context));
  }

  /**
   * Returns the input's stream contexts.
   *
   * @return A collection of input stream contexts.
   */
  public Collection<InputStreamContext> streams() {
    return streams;
  }

  @Override
  public void notify(InputContext update) {
    super.notify(update);
    for (InputStreamContext stream : streams) {
      boolean updated = false;
      for (InputStreamContext s : update.streams()) {
        if (stream.equals(s)) {
          stream.notify(s);
          updated = true;
          break;
        }
      }
      if (!updated) {
        stream.notify(null);
      }
    }
  }

  /**
   * Input context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder {
    private InputContext context;

    private Builder() {
      context = new InputContext();
    }

    private Builder(InputContext context) {
      this.context = context;
    }

    /**
     * Creates a new context builder.
     *
     * @return A new context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting input context.
     * @return A new context builder.
     */
    public static Builder newBuilder(InputContext context) {
      return new Builder(context);
    }

    /**
     * Sets the input streams.
     *
     * @param streams An array of input stream contexts.
     * @return The context builder.
     */
    public Builder setStreams(InputStreamContext... streams) {
      context.streams = Arrays.asList(streams);
      return this;
    }

    /**
     * Sets the input streams.
     *
     * @param streams A collection of input stream contexts.
     * @return The context builder.
     */
    public Builder setStreams(Collection<InputStreamContext> streams) {
      context.streams = streams;
      return this;
    }

    /**
     * Adds a stream to the input.
     *
     * @param stream An input stream context.
     * @return The context builder.
     */
    public Builder addStream(InputStreamContext stream) {
      context.streams.add(stream);
      return this;
    }

    /**
     * Removes a stream from the input.
     *
     * @param stream An input stream context.
     * @return The context builder.
     */
    public Builder removeStream(InputStreamContext stream) {
      context.streams.remove(stream);
      return this;
    }

    /**
     * Builds the input context.
     *
     * @return A new input context.
     */
    public InputContext build() {
      return context;
    }
  }

}
