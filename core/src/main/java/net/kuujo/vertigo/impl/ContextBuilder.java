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
import net.kuujo.vertigo.component.PartitionContext;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.connection.*;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.stream.OutputStreamContext;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.NetworkContext;

import java.util.*;

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
      component.setWorker(componentInfo.isWorker());
      component.setMultiThreaded(componentInfo.isMultiThreaded());
      component.setConfig(componentInfo.getConfig());
      component.setResources(componentInfo.getResources());

      // Set up component partitions.
      List<PartitionContext> partitions = new ArrayList<>(componentInfo.getPartitions());
      for (int i = 1; i <= componentInfo.getPartitions(); i++) {
        PartitionContext.Builder partition = PartitionContext.builder();
        partition.setAddress(String.format("%s-%d", address, i));
        partition.setNumber(i);
        partition.setInput(InputContext.builder().build());
        partition.setOutput(OutputContext.builder().build());
        partitions.add(partition.build());
      }
      component.setPartitions(partitions);

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

        for (PartitionContext sourceContext : source.partitions()) {
          // Check if the port already exists on the source's output.
          OutputPortContext.Builder output = null;
          for (OutputPortContext port : sourceContext.output().ports()) {
            if (port.name().equals(connection.getSource().getPort())) {
              output = OutputPortContext.builder(port);
              break;
            }
          }

          // If the output port doesn't already exist then add it.
          if (output == null) {
            OutputPortContext port = OutputPortContext.builder()
              .setName(connection.getSource().getPort())
              .setType(sourceInfo.getOutput().getPort(connection.getSource().getPort()).getType())
              .build();
            OutputContext.builder(sourceContext.output())
              .addPort(port).build();
            output = OutputPortContext.builder(port);
          }

          // Set up an output stream from the output port.
          OutputStreamContext.Builder outStream = OutputStreamContext.builder()
            .setPartitioner(connection.getPartitioner());

          // For each target instance, add a unique input connection for the output.
          for (PartitionContext targetPartition : target.partitions()) {
            // Check if the port already exists on the target's input.
            InputPortContext.Builder input = null;
            for (InputPortContext port : targetPartition.input().ports()) {
              if (port.name().equals(connection.getTarget().getPort())) {
                input = InputPortContext.builder(port);
                break;
              }
            }

            // If the input port doesn't already exist then add it.
            if (input == null) {
              InputPortContext port = InputPortContext.builder()
                .setName(connection.getTarget().getPort())
                .setType(targetInfo.getInput().getPort(connection.getTarget().getPort()).getType())
                .build();
              InputContext.builder(targetPartition.input())
                .addPort(port).build();
              input = InputPortContext.builder(port);
            }

            // Add an input connection to the input port.
            InputConnectionContext.Builder inConnection = InputConnectionContext.builder();
            inConnection.setSource(SourceContext.builder()
              .setComponent(connection.getSource().getComponent())
              .setPort(connection.getSource().getPort())
              .setPartition(sourceContext.number()).build());
            inConnection.setTarget(TargetContext.builder()
              .setComponent(connection.getTarget().getComponent())
              .setPort(connection.getTarget().getPort())
              .setPartition(targetPartition.number()).build());

            // Add the connection to the target input port.
            input.addConnection(inConnection.build()).build();

            // Add the new output connection to the output stream. This creates a one-to-many
            // relationship between output connections and input connections, and input
            // connections maintain a many-to-one relationship with output connections.
            OutputConnectionContext.Builder outConnection = OutputConnectionContext.builder();
            outConnection.setSource(SourceContext.builder()
              .setComponent(connection.getSource().getComponent())
              .setPort(connection.getSource().getPort()).build());
            outConnection.setTarget(TargetContext.builder()
              .setComponent(connection.getTarget().getComponent())
              .setPort(connection.getTarget().getPort()).build());

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
