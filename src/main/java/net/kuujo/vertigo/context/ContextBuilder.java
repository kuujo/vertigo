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

import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.hooks.ComponentHook;
import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.MalformedNetworkException;
import net.kuujo.vertigo.network.Module;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Verticle;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A context builder.
 *
 * @author Jordan Halterman
 */
public final class ContextBuilder {

  /**
   * Builds a network context from a network definition.
   *
   * @param network
   *   The network definition.
   * @return
   *   A new network context.
   * @throws MalformedNetworkException 
   *   If the network is malformed.
   */
  public static NetworkContext buildContext(Network network) throws MalformedNetworkException {
    JsonObject context = new JsonObject();

    // Add basic configuration items.
    context.putString(NetworkContext.ADDRESS, network.getAddress());
    context.putBoolean(NetworkContext.ACKING, network.isAckingEnabled());
    context.putNumber(NetworkContext.ACK_TIMEOUT, network.getAckTimeout());

    // Create an array of auditor addresses.
    JsonArray auditors = new JsonArray();
    for (int i = 1; i < network.getNumAuditors()+1; i++) {
      auditors.add(String.format("%s.auditor.%d", network.getAddress(), i));
    }
    context.putArray(NetworkContext.AUDITORS, auditors);

    // Create an object of component contexts, keyed by component addresses.
    List<Component<?>> components = network.getComponents();
    JsonObject componentContexts = new JsonObject();
    for (Component<?> component : components) {
      ComponentContext componentContext = buildContext(component);
      componentContexts.putObject(componentContext.getAddress(), componentContext.getState());
    }
    context.putObject(NetworkContext.COMPONENTS, componentContexts);

    try {
      return NetworkContext.fromJson(context);
    }
    catch (MalformedContextException e) {
      throw new MalformedNetworkException(e);
    }
  }

  /**
   * Builds a component context from a component definition.
   *
   * @param component
   *   The component definition.
   * @return
   *   A new component context.
   * @throws MalformedNetworkException
   *   If the compnent is malformed.
   */
  public static ComponentContext buildContext(Component<?> component) throws MalformedNetworkException {
    JsonObject context = new JsonObject();

    // Add basic component configurations.
    context.putString(ComponentContext.ADDRESS, component.getAddress());
    context.putObject(ComponentContext.CONFIG, component.getConfig());
    context.putNumber(ComponentContext.HEARTBEAT_INTERVAL, component.getHeartbeatInterval());

    // Handle module/verticle type components.
    if (component instanceof Module) {
      context.putString(ComponentContext.TYPE, ComponentContext.MODULE);
      context.putString(ModuleContext.MODULE, ((Module) component).getModule());
    }
    else if (component instanceof Verticle) {
      context.putString(ComponentContext.TYPE, ComponentContext.VERTICLE);
      context.putString(VerticleContext.MAIN, ((Verticle) component).getMain());
    }
    else {
      throw new MalformedNetworkException("Invalid component type.");
    }

    // Create an array of hook info.
    JsonObject hooks = new JsonObject();
    for (ComponentHook hook : component.getHooks()) {
      if (hook instanceof Serializable) {
        JsonArray serializable = hooks.getArray(ComponentContext.SERIALIZABLE_HOOKS);
        if (serializable == null) {
          serializable = new JsonArray();
          hooks.putArray(ComponentContext.SERIALIZABLE_HOOKS, serializable);
        }
        serializable.add(Serializer.serialize((Serializable) hook));
      }
      else {
        JsonArray bare = hooks.getArray(ComponentContext.BARE_HOOKS);
        if (bare == null) {
          bare = new JsonArray();
          hooks.putArray(ComponentContext.BARE_HOOKS, bare);
        }
        bare.add(new JsonObject().putString("type", hook.getClass().getName()));
      }
    }
    context.putObject(ComponentContext.HOOKS, hooks);

    // Create an array of serialized inputs.
    JsonArray inputs = new JsonArray();
    for (Input input : component.getInputs()) {
      inputs.add(Serializer.serialize(input));
    }
    context.putArray(ComponentContext.INPUTS, inputs);

    // Create an array of instance contexts.
    JsonArray instances = new JsonArray();
    int numInstances = component.getInstances();
    for (int i = 1; i < numInstances+1; i++) {
      instances.add(buildContext(component, i).getState());
    }
    context.putArray(ComponentContext.INSTANCES, instances);

    try {
      return ComponentContext.fromJson(context);
    }
    catch (MalformedContextException e) {
      throw new MalformedNetworkException(e);
    }
  }

  /**
   * Builds an instance context from a component definition.
   *
   * @param component
   *   The parent component definition.
   * @param instance
   *   The instance number.
   * @return
   *   A new instance context.
   */
  public static InstanceContext buildContext(Component<?> component, int instance) {
    String id = new StringBuilder().append(component.getAddress()).append(".").append(instance).toString();
    return InstanceContext.fromJson(new JsonObject().putString(InstanceContext.ID, id));
  }

}
