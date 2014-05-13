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
package net.kuujo.vertigo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.InstanceContext;
import net.kuujo.vertigo.component.ModuleConfig;
import net.kuujo.vertigo.component.VerticleConfig;
import net.kuujo.vertigo.component.impl.DefaultComponentContext;
import net.kuujo.vertigo.component.impl.DefaultInstanceContext;
import net.kuujo.vertigo.component.impl.DefaultModuleContext;
import net.kuujo.vertigo.component.impl.DefaultVerticleContext;
import net.kuujo.vertigo.hook.IOHook;
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.io.connection.impl.DefaultInputConnectionContext;
import net.kuujo.vertigo.io.connection.impl.DefaultOutputConnectionContext;
import net.kuujo.vertigo.io.impl.DefaultInputContext;
import net.kuujo.vertigo.io.impl.DefaultOutputContext;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.port.impl.DefaultInputPortContext;
import net.kuujo.vertigo.io.port.impl.DefaultOutputPortContext;
import net.kuujo.vertigo.io.stream.impl.DefaultOutputStreamContext;
import net.kuujo.vertigo.network.MalformedNetworkException;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.NetworkContext;
import net.kuujo.vertigo.network.impl.DefaultNetworkContext;

/**
 * A context builder.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class ContextBuilder {
  private static final String COMPONENT_ADDRESS_PATTERN = System.getProperty("vertigo.component.address", "%1$s.%2$s.%3$s");

  /**
   * Builds a network context from a network definition.
   *
   * @param network The network definition.
   * @param cluster The cluster to which the network belongs.
   * @return A new network context.
   * @throws MalformedNetworkException If the network is malformed.
   */
  public static NetworkContext buildContext(NetworkConfig network, String cluster) {
    DefaultNetworkContext.Builder context = DefaultNetworkContext.Builder.newBuilder();

    // Set basic network configuration options.
    context.setName(network.getName());
    context.setVersion(UUID.randomUUID().toString());
    context.setAddress(String.format("%s.%s", cluster, network.getName()));
    context.setConfig(network);
    context.setStatusAddress(String.format("%s.%s.__status", cluster, network.getName()));

    // Set up network components without inputs. Inputs are stored in a map so
    // that they can be set up after all component instances have been set up.
    Map<String, DefaultComponentContext<?>> components = new HashMap<>();
    for (ComponentConfig<?> component : network.getComponents()) {
      if (component.getType().equals(ComponentConfig.Type.MODULE)) {
        // Set up basic module configuration options.
        DefaultModuleContext.Builder module = DefaultModuleContext.Builder.newBuilder();
        module.setName(component.getName());
        String address = String.format(COMPONENT_ADDRESS_PATTERN, cluster, network.getName(), component.getName());
        module.setAddress(address);
        module.setStatusAddress(String.format("%s.__status", address));
        module.setModule(((ModuleConfig) component).getModule());
        module.setConfig(component.getConfig());
        module.setGroup(component.getGroup());
        module.setHooks(component.getHooks());

        // Set up module instances.
        List<DefaultInstanceContext> instances = new ArrayList<>();
        for (int i = 1; i <= component.getInstances(); i++) {
          DefaultInstanceContext.Builder instance = DefaultInstanceContext.Builder.newBuilder();
          instance.setAddress(String.format("%s-%d", address, i));
          instance.setStatusAddress(String.format("%s-%d.__status", address, i));
          instance.setNumber(i);
          instance.setInput(DefaultInputContext.Builder.newBuilder().build());
          instance.setOutput(DefaultOutputContext.Builder.newBuilder().build());
          instances.add(instance.build());
        }
        module.setInstances(instances);

        components.put(component.getName(), module.build());
      } else {
        // Set up basic verticle configuration options.
        DefaultVerticleContext.Builder verticle = DefaultVerticleContext.Builder.newBuilder();
        verticle.setName(component.getName());
        String address = String.format(COMPONENT_ADDRESS_PATTERN, cluster, network.getName(), component.getName());
        verticle.setAddress(address);
        verticle.setStatusAddress(String.format("%s.__status", address));
        verticle.setMain(((VerticleConfig) component).getMain());
        verticle.setWorker(((VerticleConfig) component).isWorker());
        verticle.setMultiThreaded(((VerticleConfig) component).isMultiThreaded());
        verticle.setConfig(component.getConfig());
        verticle.setGroup(component.getGroup());
        verticle.setHooks(component.getHooks());

        // Set up module instances.
        List<DefaultInstanceContext> instances = new ArrayList<>();
        for (int i = 1; i <= component.getInstances(); i++) {
          DefaultInstanceContext.Builder instance = DefaultInstanceContext.Builder.newBuilder();
          instance.setAddress(String.format("%s-%d", address, i));
          instance.setStatusAddress(String.format("%s-%d.__status", address, i));
          instance.setNumber(i);
          instance.setInput(DefaultInputContext.Builder.newBuilder().build());
          instance.setOutput(DefaultOutputContext.Builder.newBuilder().build());
          instances.add(instance.build());
        }
        verticle.setInstances(instances);

        components.put(component.getName(), verticle.build());
      }
    }

    // Iterate through connections and create connection contexts.
    // For each input connection, an internal input connection is created
    // for each instance of the source component. Corresponding output connections
    // are assigned to each output connection. In other words, each internal
    // output connection can send to multiple addresses, but each internal input
    // connection only listens on a single event bus address for messages from a
    // single instance of the source component. This simplifies back pressure and
    // resolving ordering issues in many-to-many component relationships.
    for (ConnectionConfig connection : network.getConnections()) {
      ComponentContext<?> source = components.get(connection.getSource().getComponent());
      ComponentContext<?> target = components.get(connection.getTarget().getComponent());

      // Only add connections if both components are currently in the network configuration.
      // If a component is added to the configuration later then the context will need to
      // be rebuilt.
      if (source != null && target != null) {
        for (InstanceContext sourceInstance : source.instances()) {
          // Check if the port already exists on the source's output.
          DefaultOutputPortContext.Builder output = null;
          for (OutputPortContext port : sourceInstance.output().ports()) {
            if (port.name().equals(connection.getSource().getPort())) {
              output = DefaultOutputPortContext.Builder.newBuilder((DefaultOutputPortContext) port);
              break;
            }
          }

          // If the output port doesn't already exist then add it.
          if (output == null) {
            DefaultOutputPortContext port = DefaultOutputPortContext.Builder.newBuilder()
                .setAddress(String.format("out:%s@%s.%s.%s[%d]", connection.getSource().getPort(), cluster, network.getName(), source.name(), sourceInstance.number()))
                .setName(connection.getSource().getPort())
                .build();
            DefaultOutputContext.Builder.newBuilder((DefaultOutputContext) sourceInstance.output())
                .addPort(port).build();
            output = DefaultOutputPortContext.Builder.newBuilder(port);
          }

          // Set up an output stream from the output port.
          DefaultOutputStreamContext.Builder outStream = DefaultOutputStreamContext.Builder.newBuilder();
          outStream.setAddress(String.format("out:%s@%s.%s.%s[%d]->in:%s@%s.%s.%s[]", connection.getSource().getPort(), cluster, network.getName(), source.name(), sourceInstance.number(), connection.getTarget().getPort(), cluster, network.getName(), target.name()));
          outStream.setSelector(connection.getSelector());

          // For each target instance, add a unique input connection for the output.
          for (InstanceContext targetInstance : target.instances()) {
            // Check if the port already exists on the target's input.
            DefaultInputPortContext.Builder input = null;
            for (InputPortContext port : targetInstance.input().ports()) {
              if (port.name().equals(connection.getTarget().getPort())) {
                input = DefaultInputPortContext.Builder.newBuilder((DefaultInputPortContext) port);
                break;
              }
            }

            // If the input port doesn't already exist then add it.
            if (input == null) {
              DefaultInputPortContext port = DefaultInputPortContext.Builder.newBuilder()
                  .setAddress(String.format("in:%s@%s.%s.%s[%d]", connection.getTarget().getPort(), cluster, network.getName(), target.name(), targetInstance.number()))
                  .setName(connection.getTarget().getPort())
                  .build();
              DefaultInputContext.Builder.newBuilder((DefaultInputContext) targetInstance.input())
                  .addPort(port).build();
              input = DefaultInputPortContext.Builder.newBuilder(port);
            }

            // Add an input connection to the input port.
            DefaultInputConnectionContext.Builder inConnection = DefaultInputConnectionContext.Builder.newBuilder();
            String address = String.format("out:%s@%s.%s.%s[%d]->in:%s@%s.%s.%s[%d]", connection.getSource().getPort(), cluster, network.getName(), source.name(), sourceInstance.number(), connection.getTarget().getPort(), cluster, network.getName(), target.name(), targetInstance.number());
            inConnection.setAddress(address);

            // Add input level hooks to the input.
            inConnection.setHooks(connection.getTarget().getHooks());

            // Add connection level hooks to the input.
            for (IOHook hook : connection.getHooks()) {
              inConnection.addHook(hook);
            }

            // Add the connection to the target input port.
            input.addConnection(inConnection.build()).build();

            // Add the new output connection to the output stream. This creates a one-to-many
            // relationship between output connections and input connections, and input
            // connections maintain a many-to-one relationship with output connections.
            DefaultOutputConnectionContext.Builder outConnection = DefaultOutputConnectionContext.Builder.newBuilder();
            outConnection.setAddress(address);

            // Add output level hooks to the output.
            outConnection.setHooks(connection.getSource().getHooks());

            // Add connection level hooks to the output.
            for (IOHook hook : connection.getHooks()) {
              outConnection.addHook(hook);
            }
            outStream.addConnection(outConnection.build());
          }

          // Add the connection to the source instance's out port.
          output.addStream(outStream.build()).build();
        }
      }
    }

    // Set the components on the network context and build the final context.
    context.setComponents(components.values());
    return context.build();
  }

}
