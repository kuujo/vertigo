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

import net.kuujo.vertigo.cluster.ClusterClient;
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
  public static NetworkContext buildContext(Network network, ClusterClient cluster) {
    NetworkContext.Builder context = NetworkContext.Builder.newBuilder();

    // Set the network cluster.
    context.setCluster(cluster);

    // Set basic network configuration options.
    context.setAddress(network.getAddress());
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
    Map<String, List<Input>> inputs = new HashMap<>();
    for (Component<?> component : network.getComponents()) {
      // Store the component inputs for later setup.
      inputs.put(component.getName(), component.getInputs());

      if (component.isModule()) {
        // Set up basic module configuratin options.
        ModuleContext.Builder module = ModuleContext.Builder.newBuilder();
        module.setCluster(cluster);
        module.setName(component.getName());
        String address = component.getAddress();
        if (address == null) {
          address = String.format(COMPONENT_ADDRESS_PATTERN, network.getAddress(), component.getName());
        }
        module.setAddress(address);
        module.setType(component.getType());
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
        verticle.setCluster(cluster);
        verticle.setName(component.getName());
        String address = component.getAddress();
        if (address == null) {
          address = String.format(COMPONENT_ADDRESS_PATTERN, network.getAddress(), component.getName());
        }
        verticle.setAddress(address);
        verticle.setType(component.getType());
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
          instance.setCluster(cluster);
          instance.setAddress(String.format("%s-%d", address, i));
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
        OutputStreamContext.Builder outputStream = OutputStreamContext.Builder.newBuilder();
        outputStream.setCluster(cluster);
        outputStream.setName(info.getStream());
        outputStream.setGrouping(info.getGrouping());

        // The input component is the entry key.
        ComponentContext<?> inputComponentContext = components.get(component.getAddress());

        // The output component is referenced by the Input instance.
        ComponentContext<?> outputComponentContext = components.get(info.getAddress());

        if (inputComponentContext != null && outputComponentContext != null) {
          outputStream.setAddress(String.format("%s.%s.%s", outputComponentContext.address(), inputComponentContext.address(), info.getStream()));
          // Iterate through input instances and add unique addresses to the output stream.
          for (InstanceContext inputInstanceContext : inputComponentContext.instances()) {
            String address = String.format("%s.%s.%s", outputComponentContext.address(), inputInstanceContext.address(), info.getStream());
            InputStreamContext.Builder inputStream = InputStreamContext.Builder.newBuilder();
            inputStream.setCluster(cluster);
            inputStream.setName(info.getStream());
            inputStream.setAddress(address);
            ConnectionContext connection = ConnectionContext.Builder.newBuilder().setAddress(address).build();
            inputStream.setConnection(connection); // Set the input stream connection.
            outputStream.addConnection(connection); // Add the connection to the output stream.
            InputContext.Builder.newBuilder(inputInstanceContext.input()).addStream(inputStream.build()).build();
          }

          // Iterate through output instances and add the output stream.
          for (InstanceContext outputInstanceContext : outputComponentContext.instances()) {
            OutputContext.Builder.newBuilder(outputInstanceContext.output()).addStream(outputStream.build()).build();
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
  public static NetworkContext mergeContexts(NetworkContext base, Network merge) {
    if (!base.address().equals(merge.getAddress())) {
      throw new IllegalArgumentException("Network addresses must match for merge.");
    }

    NetworkContext.Builder context = NetworkContext.Builder.newBuilder(base);

    // Set up network components without inputs. Inputs are stored in a map so
    // that they can be set up after all component instances have been set up.
    Map<String, ComponentContext<?>> components = new HashMap<>();
    Map<String, List<Input>> inputs = new HashMap<>();
    for (Component<?> component : merge.getComponents()) {
      if (base.component(component.getName()) != null) {
        throw new MalformedNetworkException("Component " + component.getName() + " already exists in the network.");
      }

      // Store the component inputs for later setup.
      inputs.put(component.getName(), component.getInputs());

      if (component.isModule()) {
        // Set up basic module configuratin options.
        ModuleContext.Builder module = ModuleContext.Builder.newBuilder();
        module.setCluster(base.cluster());
        module.setName(component.getName());
        String address = component.getAddress();
        if (address == null) {
          address = String.format(COMPONENT_ADDRESS_PATTERN, merge.getAddress(), component.getName());
        }
        module.setAddress(address);
        module.setType(component.getType());
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
        verticle.setCluster(base.cluster());
        verticle.setName(component.getName());
        String address = component.getAddress();
        if (address == null) {
          address = String.format(COMPONENT_ADDRESS_PATTERN, merge.getAddress(), component.getName());
        }
        verticle.setAddress(address);
        verticle.setType(component.getType());
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
          instance.setCluster(base.cluster());
          instance.setAddress(String.format("%s-%d", address, i));
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
    for (Component<?> component : merge.getComponents()) {
      for (Input info : component.getInputs()) {

        // Set up the output stream. Each connection between components (represented
        // as an Input instance) will have an associated InputStreamContext and
        // OutputStreamContext. The OutputStreamContext contains a set of connections
        // to each instance to which the stream feeds. The InputStreamContext will
        // contain a single connection on which the instance listens for messages.
        OutputStreamContext.Builder outputStream = OutputStreamContext.Builder.newBuilder();
        outputStream.setCluster(base.cluster());
        outputStream.setName(info.getStream());
        outputStream.setGrouping(info.getGrouping());

        // The input component is the entry key.
        ComponentContext<?> inputComponentContext = components.get(component.getName());

        // The output component is referenced by the Input instance.
        ComponentContext<?> outputComponentContext = components.get(info.getAddress());

        // If the output component wasn't found then check for it in existing components.
        if (outputComponentContext == null) {
          outputComponentContext = base.component(info.getAddress());
        }

        if (inputComponentContext != null && outputComponentContext != null) {
          outputStream.setTarget(inputComponentContext.name());
          outputStream.setAddress(String.format("%s.%s.%s", outputComponentContext.address(), inputComponentContext.address(), info.getStream()));

          // Iterate through input instances and add unique addresses to the output stream.
          for (InstanceContext inputInstanceContext : inputComponentContext.instances()) {
            String address = String.format("%s.%s.%s", outputComponentContext.address(), inputInstanceContext.address(), info.getStream());
            InputStreamContext.Builder inputStream = InputStreamContext.Builder.newBuilder();
            inputStream.setSource(outputComponentContext.name());
            inputStream.setCluster(base.cluster());
            inputStream.setName(info.getStream());
            inputStream.setAddress(address);
            ConnectionContext connection = ConnectionContext.Builder.newBuilder().setAddress(address).build();
            inputStream.setConnection(connection); // Set the input stream connection.
            outputStream.addConnection(connection); // Add the connection to the output stream.
            InputContext.Builder.newBuilder(inputInstanceContext.input()).addStream(inputStream.build()).build();
          }

          // Iterate through output instances and add the output stream.
          for (InstanceContext outputInstanceContext : outputComponentContext.instances()) {
            OutputContext.Builder.newBuilder(outputInstanceContext.output()).addStream(outputStream.build()).build();
          }
        }
      }
    }

    // Set the components on the network context and build the final context.
    for (ComponentContext<?> component : components.values()) {
      context.addComponent(component);
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
  public static NetworkContext unmergeContexts(NetworkContext base, Network merge) {
    if (!base.address().equals(merge.getAddress())) {
      throw new IllegalArgumentException("Network addresses must match for merge.");
    }

    NetworkContext.Builder context = NetworkContext.Builder.newBuilder(base);

    // Iterate through components and remove input/output streams.
    for (Component<?> component : merge.getComponents()) {
      for (ComponentContext<?> componentContext : base.components()) {
        // Search through instances for streams related to the component.
        for (InstanceContext instanceContext : componentContext.instances()) {

          // Remove any input stream from the component.
          InputContext.Builder input = InputContext.Builder.newBuilder(instanceContext.input());
          for (InputStreamContext stream : instanceContext.input().streams()) {
            if (stream.source().equals(component.getName())) {
              input.removeStream(stream);
            }
          }
          input.build();

          // Remove any output stream to the component.
          OutputContext.Builder output = OutputContext.Builder.newBuilder(instanceContext.output());
          for (OutputStreamContext stream : instanceContext.output().streams()) {
            if (stream.target().equals(component.getName())) {
              output.removeStream(stream);
            }
          }
          output.build();
        }

        // Remove the component from the network context.
        if (componentContext.name().equals(component.getName())) {
          context.removeComponent(componentContext);
        }
      }
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
