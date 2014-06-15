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
package net.kuujo.vertigo.util;

import net.kuujo.vertigo.Context;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.io.IOContext;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.util.serialization.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Context serialization helpers.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class Contexts {

  /**
   * Serializes a context to JSON.
   *
   * @param context The context to serialize.
   * @return The serialized context.
   */
  public static JsonObject serialize(Context<?> context) {
    if (context instanceof NetworkContext) {
      return serialize(NetworkContext.class.cast(context));
    } else if (context instanceof ComponentContext) {
      return serialize(ComponentContext.class.cast(context));
    } else if (context instanceof InstanceContext) {
      return serialize(InstanceContext.class.cast(context));
    } else if (context instanceof IOContext) {
      return serialize(IOContext.class.cast(context));
    } else if (context instanceof InputPortContext) {
      return serialize(InputPortContext.class.cast(context));
    } else if (context instanceof OutputPortContext) {
      return serialize(OutputPortContext.class.cast(context));
    } else {
      throw new UnsupportedOperationException("Cannot serialize " + context.getClass().getCanonicalName() + " type contexts");
    }
  }

  /**
   * Serializes a network context to JSON.
   *
   * @param context The context to serialize.
   * @return The serialized context.
   */
  public static JsonObject serialize(NetworkContext context) {
    return serialize(context.uri(), context);
  }

  /**
   * Serializes a component context to JSON.
   *
   * @param context The context to serialize.
   * @return The serialized context.
   */
  public static JsonObject serialize(ComponentContext<?> context) {
    return serialize(context.uri(), context.network());
  }

  /**
   * Serializes an instance context to JSON.
   *
   * @param context The context to serialize.
   * @return The serialized context.
   */
  public static JsonObject serialize(InstanceContext context) {
    return serialize(context.uri(), context.component().network());
  }

  /**
   * Serializes an I/O context to JSON.
   *
   * @param context The context to serialize.
   * @return The serialized context.
   */
  public static JsonObject serialize(IOContext<?> context) {
    return serialize(context.uri(), context.instance().component().network());
  }

  /**
   * Serializes an input port context to JSON.
   *
   * @param context The context to serialize.
   * @return The serialized context.
   */
  public static JsonObject serialize(InputPortContext context) {
    return serialize(context.uri(), context.input().instance().component().network());
  }

  /**
   * Serializes an output port context to JSON.
   *
   * @param context The context to serialize.
   * @return The serialized context.
   */
  public static JsonObject serialize(OutputPortContext context) {
    return serialize(context.uri(), context.output().instance().component().network());
  }

  private static JsonObject serialize(String uri, NetworkContext context) {
    return new JsonObject()
        .putString("uri", uri)
        .putObject("context", SerializerFactory.getSerializer(Context.class).serializeToObject(context));
  }

  /**
   * Deserializes a context from JSON.
   *
   * @param context The serialized context.
   * @return The deserialized context.
   */
  public static <T extends Context<T>> T deserialize(JsonObject context) {
    return deserialize(context.getString("uri"), context.getObject("context"));
  }

  @SuppressWarnings("unchecked")
  private static <T extends Context<T>> T deserialize(String uri, JsonObject context) {
    ContextUri curi = new ContextUri(uri);
    NetworkContext network = SerializerFactory.getSerializer(Context.class).deserializeObject(context, NetworkContext.class);
    if (!curi.getCluster().equals(network.cluster()) || !curi.getNetwork().equals(network.name())) {
      throw new IllegalArgumentException("The given URI does not match the given context configuration");
    }

    if (curi.hasComponent()) {
      ComponentContext<?> component = network.component(curi.getComponent());
      if (component == null) {
        throw new IllegalArgumentException("The URI component " + curi.getComponent() + " does not exist in the given network configuration");
      }
      if (curi.hasInstance()) {
        InstanceContext instance = component.instance(curi.getInstance());
        if (instance == null) {
          throw new IllegalArgumentException("The URI instance " + curi.getInstance() + " does not exist in the given component configuration");
        }
        if (curi.hasEndpoint()) {
          switch (curi.getEndpoint()) {
            case ContextUri.ENDPOINT_IN:
              InputContext input = instance.input();
              if (curi.hasPort()) {
                InputPortContext inPort = input.port(curi.getPort());
                if (inPort == null) {
                  throw new IllegalArgumentException("The URI port " + curi.getPort() + " does not exist in the given input configuration");
                }
                return (T) inPort;
              }
              return (T) input;
            case ContextUri.ENDPOINT_OUT:
              OutputContext output = instance.output();
              if (curi.hasPort()) {
                OutputPortContext outPort = output.port(curi.getPort());
                if (outPort == null) {
                  throw new IllegalArgumentException("The URI port " + curi.getPort() + " does not exist in the given output configuration");
                }
                return (T) outPort;
              }
              return (T) output;
            default:
              throw new IllegalArgumentException("The URI endpoint " + curi.getEndpoint() + " is not a valid endpoint type");
          }
        }
        return (T) instance;
      }
      return (T) component;
    }
    return (T) network;
  }

}
