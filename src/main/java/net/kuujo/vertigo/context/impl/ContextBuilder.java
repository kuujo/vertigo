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
package net.kuujo.vertigo.context.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.ConnectionContext;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.InputStreamContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.context.OutputStreamContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.network.Component;
import net.kuujo.vertigo.network.Input;
import net.kuujo.vertigo.network.MalformedNetworkException;
import net.kuujo.vertigo.network.Module;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Verticle;

/**
 * A context builder.
 *
 * @author Jordan Halterman
 */
public final class ContextBuilder {
  private static final String COMPONENT_ADDRESS_PATTERN = System.getProperty("vertigo.component.address", "%1$s.%2$s");

  /**
   * Builds a network context from a network definition.
   *
   * @param network
   *   The network definition.
   * @param cluster
   *   The cluster in which the network is being deployed.
   * @return
   *   A new network context.
   * @throws MalformedNetworkException 
   *   If the network is malformed.
   */
  public static NetworkContext buildContext(Network network, VertigoCluster cluster) {
    NetworkContext.Builder context = NetworkContext.Builder.newBuilder();

    // Set basic network configuration options.
    context.setAddress(network.getAddress());
    context.setStatusAddress(String.format("%s.__status", network.getAddress()));
    context.setAckingEnabled(network.isAckingEnabled());
    context.setMessageTimeout(network.getMessageTimeout());

    // Set up network auditors with unique addresses.
    Set<String> auditors = new HashSet<>();
    for (int i = 1; i <= network.getNumAuditors(); i++) {
      auditors.add(String.format("%s.auditor.%d", network.getAddress(), i));
    }
    context.setAuditors(auditors);

    // Set up network components without inputs. Inputs are stored in a map so
    // that they can be set up after all component instances have been set up.
    Map<String, ComponentContext<?>> components = new HashMap<>();
    for (Component<?> component : network.getComponents()) {
      if (component.isModule()) {
        // Set up basic module configuratin options.
        ModuleContext.Builder module = ModuleContext.Builder.newBuilder();
        module.setName(component.getName());
        String address = component.getAddress();
        if (address == null) {
          address = String.format(COMPONENT_ADDRESS_PATTERN, network.getAddress(), component.getName());
        }
        module.setAddress(address);
        module.setStatusAddress(String.format("%s.__status", address));
        module.setModule(((Module) component).getModule());
        module.setConfig(component.getConfig());
        module.setHooks(component.getHooks());
        module.setDeploymentGroup(component.getDeploymentGroup());

        // Set up module instances.
        List<InstanceContext> instances = new ArrayList<>();
        for (int i = 1; i <= component.getNumInstances(); i++) {
          InstanceContext.Builder instance = InstanceContext.Builder.newBuilder();
          instance.setAddress(String.format("%s-%d", address, i));
          instance.setNumber(i);
          instance.setInput(InputContext.Builder.newBuilder().build());
          instance.setOutput(OutputContext.Builder.newBuilder().build());
          instances.add(instance.build());
        }
        module.setInstances(instances);

        components.put(component.getName(), module.build());
      }
      else {
        // Set up basic verticle configuration options.
        VerticleContext.Builder verticle = VerticleContext.Builder.newBuilder();
        verticle.setName(component.getName());
        String address = component.getAddress();
        if (address == null) {
          address = String.format(COMPONENT_ADDRESS_PATTERN, network.getAddress(), component.getName());
        }
        verticle.setAddress(address);
        verticle.setStatusAddress(String.format("%s.__status", address));
        verticle.setMain(((Verticle) component).getMain());
        verticle.setWorker(((Verticle) component).isWorker());
        verticle.setMultiThreaded(((Verticle) component).isMultiThreaded());
        verticle.setConfig(component.getConfig());
        verticle.setHooks(component.getHooks());
        verticle.setDeploymentGroup(component.getDeploymentGroup());

        // Set up module instances.
        List<InstanceContext> instances = new ArrayList<>();
        for (int i = 1; i <= component.getNumInstances(); i++) {
          InstanceContext.Builder instance = InstanceContext.Builder.newBuilder();
          instance.setAddress(String.format("%s-%d", address, i));
          instance.setStatusAddress(String.format("%s-%d.__status", address, i));
          instance.setNumber(i);
          instance.setInput(InputContext.Builder.newBuilder().build());
          instance.setOutput(OutputContext.Builder.newBuilder().build());
          instances.add(instance.build());
        }
        verticle.setInstances(instances);

        components.put(component.getName(), verticle.build());
      }
    }

    // Iterate through all inputs in the network and set up input/output streams.
    for (Component<?> component : network.getComponents()) {
      for (Input info : component.getInputs()) {

        // Set up the output stream. Each connection between components (represented
        // as an Input instance) will have an associated InputStreamContext and
        // OutputStreamContext. The OutputStreamContext contains a set of connections
        // to each instance to which the stream feeds. The InputStreamContext will
        // contain a single connection on which the instance listens for messages.
        ComponentContext<?> inputComponentContext = components.get(component.getName());
        if (inputComponentContext != null) {
          String streamAddress = String.format("%s.%s.%s.%s", network.getAddress(), info.getAddress(), inputComponentContext.name(), info.getStream());
          for (InstanceContext inputInstanceContext : inputComponentContext.instances()) {
            String address = String.format("%s.%s.%s.%s", network.getAddress(), info.getAddress(), inputInstanceContext.address(), info.getStream());
            InputStreamContext.Builder inputStream = InputStreamContext.Builder.newBuilder();
            inputStream.setName(info.getStream());
            inputStream.setAddress(streamAddress);
            inputStream.setSource(info.getAddress());
            inputStream.setGrouping(info.getGrouping());
            ConnectionContext connection = ConnectionContext.Builder.newBuilder().setAddress(address).build();
            inputStream.setConnection(connection);
            InputContext.Builder.newBuilder(inputInstanceContext.input()).addStream(inputStream.build()).build();
          }

          ComponentContext<?> outputComponentContext = components.get(info.getAddress());
          if (outputComponentContext != null) {
            for (InstanceContext outputInstanceContext : outputComponentContext.instances()) {
              OutputStreamContext.Builder outputStream = OutputStreamContext.Builder.newBuilder();
              outputStream.setName(info.getStream());
              outputStream.setAddress(streamAddress);
              outputStream.setTarget(inputComponentContext.name());
              outputStream.setGrouping(info.getGrouping());
              for (InstanceContext inputInstanceContext : inputComponentContext.instances()) {
                String address = String.format("%s.%s.%s.%s", network.getAddress(), info.getAddress(), inputInstanceContext.address(), info.getStream());
                ConnectionContext connection = ConnectionContext.Builder.newBuilder().setAddress(address).build();
                outputStream.addConnection(connection);
              }
              OutputContext.Builder.newBuilder(outputInstanceContext.output()).addStream(outputStream.build()).build();
            }
          }
        }
      }
    }

    // Set the components on the network context and build the final context.
    context.setComponents(components.values());
    return context.build();
  }

  /**
   * Merges two network contexts together.
   *
   * @param base The base network context.
   * @param merge The context to merge.
   * @return The merged network context.
   */
  public static NetworkContext mergeContexts(NetworkContext base, NetworkContext merge) {
    if (!base.address().equals(merge.address())) {
      throw new IllegalArgumentException("Network addresses must match for merge.");
    }

    NetworkContext.Builder context = NetworkContext.Builder.newBuilder(base);

    // Add components to the network.
    for (ComponentContext<?> component : merge.components()) {
      context.addComponent(component);
    }

    // Iterate through all components and map any missing connections.
    for (ComponentContext<?> targetComponent : base.components()) {
      for (InstanceContext targetInstance : targetComponent.instances()) {
        for (InputStreamContext input : targetInstance.input().streams()) {
          ComponentContext<?> sourceComponent = base.component(input.source());
          if (sourceComponent != null) {
            for (InstanceContext sourceInstance : sourceComponent.instances()) {
              boolean exists = false;
              for (OutputStreamContext output : sourceInstance.output().streams()) {
                if (output.address().equals(input.address())) {
                  boolean hasConnection = false;
                  for (ConnectionContext connection : output.connections()) {
                    if (connection.address().equals(input.connection().address())) {
                      hasConnection = true;
                      break;
                    }
                  }
                  if (!hasConnection) {
                    OutputStreamContext.Builder.newBuilder(output).addConnection(input.connection()).build();
                  }
                  exists = true;
                  break;
                }
              }
              if (!exists) {
                InstanceContext.Builder.newBuilder(sourceInstance).setOutput(
                    OutputContext.Builder.newBuilder(sourceInstance.output()).addStream(
                        OutputStreamContext.Builder.newBuilder()
                            .setName(input.name())
                            .setAddress(input.address())
                            .setTarget(targetComponent.name())
                            .setGrouping(input.grouping())
                            .addConnection(input.connection()).build()).build());
              }
            }
          }
        }
      }
    }

    return context.build();
  }

  /**
   * Unmerges two network contexts.
   *
   * @param base The base network context.
   * @param merge The context to unmerge.
   * @return The unmerged network context.
   */
  public static NetworkContext unmergeContexts(NetworkContext base, NetworkContext merge) {
    if (!base.address().equals(merge.address())) {
      throw new IllegalArgumentException("Network addresses must match for merge.");
    }

    NetworkContext.Builder context = NetworkContext.Builder.newBuilder(base);

    // Iterate through all the components being removed and remove their connections.
    for (ComponentContext<?> targetComponent : merge.components()) {
      for (InstanceContext targetInstance : targetComponent.instances()) {
        for (InputStreamContext input : targetInstance.input().streams()) {
          ComponentContext<?> sourceComponent = base.component(input.source());
          if (sourceComponent != null) {
            for (InstanceContext sourceInstance : sourceComponent.instances()) {
              for (OutputStreamContext output : sourceInstance.output().streams()) {
                if (output.address().equals(input.address())) {
                  for (ConnectionContext connection : output.connections()) {
                    if (connection.address().equals(input.connection().address())) {
                      OutputStreamContext.Builder.newBuilder(output).removeConnection(connection).build();
                    }
                  }
                  OutputContext.Builder.newBuilder(sourceInstance.output()).removeStream(output).build();
                }
              }
            }
          }
        }
      }
    }

    for (ComponentContext<?> component : merge.components()) {
      context.removeComponent(component);
    }

    return context.build();
  }

  /**
   * Indicates whether the two contexts are equivalent by simply counting components.
   *
   * @param base The base network context.
   * @param merge The context being unmerged.
   * @return Indicates whether the contexts are equivalent.
   */
  public static boolean isCompleteUnmerge(NetworkContext base, Network merge) {
    int count = 0;
    for (Component<?> component : merge.getComponents()) {
      if (base.component(component.getName()) != null) {
        count++;
      }
    }
    return count == base.components().size();
  }

}
