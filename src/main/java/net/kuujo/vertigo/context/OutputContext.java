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
package net.kuujo.vertigo.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Output context represents output information between a
 * source component and a target component. This information
 * is used to indicate where the component should send messages.
 *
 * @author Jordan Halterman
 */
public class OutputContext extends IOContext<OutputContext> {
  private Collection<OutputStreamContext> streams = new ArrayList<>();

  /**
   * Creates a new output context from JSON.
   * 
   * @param context A JSON representation of the output context.
   * @return A new output context instance.
   * @throws MalformedContextException If the JSON context is malformed.
   */
  public static OutputContext fromJson(JsonObject context) {
    Serializer serializer = SerializerFactory.getSerializer(InstanceContext.class);
    OutputContext output = serializer.deserializeObject(context.getObject("output"), OutputContext.class);
    InstanceContext instance = InstanceContext.fromJson(context);
    return output.setInstanceContext(instance);
  }

  /**
   * Serializes an output context to JSON.
   * 
   * @param context The IO context to serialize.
   * @return A Json representation of the IO context.
   */
  public static JsonObject toJson(OutputContext context) {
    Serializer serializer = SerializerFactory.getSerializer(InstanceContext.class);
    JsonObject json = InstanceContext.toJson(context.instance());
    return json.putObject("output", serializer.serializeToObject(context));
  }

  /**
   * Returns the output's stream contexts.
   *
   * @return A collection of output stream contexts.
   */
  public Collection<OutputStreamContext> streams() {
    return streams;
  }

  @Override
  public void notify(OutputContext update) {
    super.notify(update);
    for (OutputStreamContext stream : streams) {
      boolean updated = false;
      for (OutputStreamContext s : update.streams()) {
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
   * Output context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<OutputContext> {

    private Builder() {
      super(new OutputContext());
    }

    private Builder(OutputContext context) {
      super(context);
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
     * @param context A starting output context.
     * @return A new context builder.
     */
    public static Builder newBuilder(OutputContext context) {
      return new Builder(context);
    }

    /**
     * Sets the output streams.
     *
     * @param streams An array of output stream contexts.
     * @return The context builder.
     */
    public Builder setStreams(OutputStreamContext... streams) {
      context.streams = Arrays.asList(streams);
      return this;
    }

    /**
     * Sets the output streams.
     *
     * @param streams A collection of output stream contexts.
     * @return The context builder.
     */
    public Builder setStreams(Collection<OutputStreamContext> streams) {
      context.streams = streams;
      return this;
    }

    /**
     * Adds a stream to the output.
     *
     * @param stream An output stream context.
     * @return The context builder.
     */
    public Builder addStream(OutputStreamContext stream) {
      context.streams.add(stream);
      return this;
    }

    /**
     * Removes a stream from the output.
     *
     * @param stream An output stream context.
     * @return The context builder.
     */
    public Builder removeStream(OutputStreamContext stream) {
      context.streams.remove(stream);
      return this;
    }
  }

}
