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

import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.connection.*;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.InputPortInfo;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.NetworkContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Context builder.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class ContextBuilder {
  private static final String COMPONENT_ADDRESS_PATTERN = System.getProperty("vertigo.component.address", "%1$s.%2$s");

  /**
   * Builds a network context from a network definition.
   *
   * @param network The network definition.
   * @return A new network context.
   */
  public static NetworkContext buildContext(Network network) {
    NetworkContext.Builder context = NetworkContext.builder();

    // Set basic network configuration options.
    context.setName(network.getName());
    context.setAddress(network.getName());
    context.setVersion(UUID.randomUUID().toString());
    context.setConfig(network);

    // Set up network components without inputs. Inputs are stored in a map so
    // that they can be set up after all component partitions have been set up.
    Map<String, ComponentContext> components = new HashMap<>(network.getComponents().size());
    for (ComponentInfo componentInfo : network.getComponents()) {
      // Set up basic component configuration options.
      ComponentContext.Builder component = ComponentContext.builder();
      component.setName(componentInfo.getName());
      String address = String.format(COMPONENT_ADDRESS_PATTERN, network.getName(), componentInfo.getName());
      component.setAddress(address);
      component.setIdentifier(componentInfo.getIdentifier());
      component.setConfig(componentInfo.getConfig());
      component.setWorker(componentInfo.isWorker());
      component.setMultiThreaded(componentInfo.isMultiThreaded());
      component.setStateful(componentInfo.isStateful());
      component.setReplicas(componentInfo.getReplicas());
      component.setResources(componentInfo.getResources());

      // Set up component input ports.
      InputContext.Builder input = InputContext.builder();
      for (InputPortInfo port : componentInfo.getInput().getPorts()) {
        input.addPort(InputPortContext.builder()
          .setName(port.getName())
          .setType(port.getType())
          .setCodec(port.getCodec())
          .setPersistent(port.isPersistent())
          .setInput(input.build())
          .build());
      }
      component.setInput(input.build());

      // Set up component output ports.
      OutputContext.Builder output = OutputContext.builder();
      for (OutputPortInfo port : componentInfo.getOutput().getPorts()) {
        output.addPort(OutputPortContext.builder()
          .setName(port.getName())
          .setType(port.getType())
          .setCodec(port.getCodec())
          .setPersistent(port.isPersistent())
          .setOutput(output.build())
          .build());
      }
      component.setOutput(output.build());

      components.put(componentInfo.getName(), component.build());
    }

    // Iterate through connections and create connection contexts.
    // For each input connection, an internal input connection is created
    // for each instance of the source component. Corresponding output connections
    // are assigned to each output connection. In other words, each internal
    // output connection can send to multiple addresses, but each internal input
    // connection only listens on a single event bus address for messages from a
    // single instance of the source component. This simplifies back pressure and
    // resolving ordering issues in many-to-many component relationships.
    for (ConnectionInfo connection : network.getConnections()) {
      ComponentContext source = components.get(connection.getSource().getComponent());
      ComponentContext target = components.get(connection.getTarget().getComponent());

      // Only add connections if both components are currently in the network configuration.
      // If a component is added to the configuration later then the context will need to
      // be rebuilt.
      if (source != null && target != null) {
        ComponentInfo sourceInfo = network.getComponent(source.name());
        ComponentInfo targetInfo = network.getComponent(target.name());

        // Add the connection to the source's output port context.
        OutputPortContext.Builder output = OutputPortContext.builder(source.output().port(connection.getSource().getPort()))
          .setName(connection.getSource().getPort())
          .setType(sourceInfo.getOutput().getPort(connection.getSource().getPort()).getType());

        output.addConnection(OutputConnectionContext.builder()
          .setSource(SourceContext.builder()
            .setComponent(connection.getSource().getComponent())
            .setPort(connection.getSource().getPort())
            .setAddress(source.address())
            .build())
          .setTarget(TargetContext.builder()
            .setComponent(connection.getTarget().getComponent())
            .setPort(connection.getTarget().getPort())
            .setAddress(target.address())
            .build())
          .setPort(output.build()).build());

        // Add the connection to the target's input port context.
        InputPortContext.Builder input = InputPortContext.builder(target.input().port(connection.getTarget().getPort()))
          .setName(connection.getTarget().getPort())
          .setType(targetInfo.getInput().getPort(connection.getTarget().getPort()).getType());

        input.addConnection(InputConnectionContext.builder()
          .setSource(SourceContext.builder()
            .setComponent(connection.getSource().getComponent())
            .setPort(connection.getSource().getPort())
            .setAddress(source.address())
            .build())
          .setTarget(TargetContext.builder()
            .setComponent(connection.getTarget().getComponent())
            .setPort(connection.getTarget().getPort())
            .setAddress(target.address())
            .build())
          .build());
      }
    }

    // Set the components on the network context and build the final context.
    context.setComponents(components.values());
    return context.build();
  }

}
