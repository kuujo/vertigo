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
package net.kuujo.vertigo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.grouping.RoundGrouping;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;


/**
 * A network definition.
 *
 * @author Jordan Halterman
 */
public class Network implements Serializable<JsonObject> {

  private JsonObject definition = new JsonObject();

  private static final long DEFAULT_ACK_EXPIRE = 30000;

  public Network(String address) {
    definition.putString("address", address);
  }

  private Network(JsonObject json) {
    definition = json;
  }

  /**
   * Creates a network from JSON object.
   *
   * @param json
   *   A JSON representation of the network.
   * @return
   *   A new network instance.
   */
  public static Network fromJson(JsonObject json) {
    return new Network(json);
  }

  /**
   * Gets the network address.
   */
  public String address() {
    return definition.getString("address");
  }

  /**
   * Sets the network address.
   *
   * @param address
   *   The network address.
   * @return
   *   The called network definition.
   */
  public Network setAddress(String address) {
    definition.putString("address", address);
    return this;
  }

  /**
   * Indicates whether acking is enabled for the network.
   * Acking is enabled by default.
   *
   * @return
   *   A boolean value indicating whether acking is enabled.
   */
  public boolean isAckingEnabled() {
    return definition.getBoolean("ack", true);
  }

  /**
   * Enables acking for the network.
   * Acking is enabled by default.
   *
   * @return
   *   The called network definition.
   */
  public Network enableAcking() {
    definition.putBoolean("ack", true);
    return this;
  }

  /**
   * Disables acking for the network.
   *
   * @return
   *   The called network definition.
   */
  public Network disableAcking() {
    definition.putBoolean("ack", false);
    return this;
  }

  /**
   * Indicates the number of auditor (acker) verticle instances.
   *
   * @return
   *   The number of acker verticle instances for the network.
   */
  public int getNumAuditors() {
    return definition.getInteger("auditors", 1);
  }

  /**
   * Sets the number of auditor (acker) verticle instances.
   *
   * @param numAuditors
   *   The number of acker verticle instances for the network.
   * @return
   *   The called network definition.
   */
  public Network setNumAuditors(int numAuditors) {
    definition.putNumber("auditors", numAuditors);
    return this;
  }

  /**
   * Indicates the internal auditor ack expiration.
   *
   * @return
   *   The internal network auditor ack expiration.
   */
  public long getAckExpire() {
    return definition.getLong("expire", DEFAULT_ACK_EXPIRE);
  }

  /**
   * Sets the internal auditor ack expiration. This is the amount of time after
   * which the acker verticle will consider all messages to be timed out. Therefore,
   * this expiration should be greater than any feeder timeout in the network.
   *
   * @param expire
   *   The ack expiration.
   * @return
   *   The called network definition.
   */
  public Network setAckExpire(long expire) {
    definition.putNumber("expire", expire);
    return this;
  }

  /**
   * Adds a root definition.
   */
  Component addDefinition(Component definition) {
    // Add the root definition.
    definition.setNetwork(this);
    JsonObject components = this.definition.getObject("components");
    if (components == null) {
      components = new JsonObject();
      this.definition.putObject("components", components);
    }
    if (!components.getFieldNames().contains(definition.name())) {
      components.putObject(definition.name(), definition.serialize());
    }
    return definition;
  }

  public Component from(Component definition) {
    return addDefinition(definition);
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @return
   *   A new component definition instance.
   */
  public Component fromVerticle(String name) {
    return addDefinition(new Component(name).setType(Component.VERTICLE));
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @return
   *   A new component definition instance.
   */
  public Component fromVerticle(String name, String main) {
    return fromVerticle(name, main, new JsonObject(), 1);
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @return
   *   A new component definition instance.
   */
  public Component fromVerticle(String name, String main, JsonObject config) {
    return fromVerticle(name, main, config, 1);
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public Component fromVerticle(String name, String main, int workers) {
    return fromVerticle(name, main, new JsonObject(), workers);
  }

  /**
   * Adds an initial verticle component.
   *
   * @param name
   *   The component name.
   * @param main
   *   The verticle main.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public Component fromVerticle(String name, String main, JsonObject config, int workers) {
    return addDefinition(new Component(name).setType(Component.VERTICLE).setMain(main).setConfig(config).setWorkers(workers));
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @return
   *   A new component definition instance.
   */
  public Component fromModule(String name) {
    return addDefinition(new Component(name).setType(Component.MODULE));
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @return
   *   A new component definition instance.
   */
  public Component fromModule(String name, String moduleName) {
    return fromModule(name, moduleName, new JsonObject(), 1);
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @return
   *   A new component definition instance.
   */
  public Component fromModule(String name, String moduleName, JsonObject config) {
    return fromModule(name, moduleName, config, 1);
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public Component fromModule(String name, String moduleName, int workers) {
    return fromModule(name, moduleName, new JsonObject(), workers);
  }

  /**
   * Adds an initial module component.
   *
   * @param name
   *   The component name.
   * @param moduleName
   *   The module name.
   * @param config
   *   A verticle configuration. This will be accessable via the worker's
   *   WorkerContext instance.
   * @param workers
   *   The number of worker verticles to deploy.
   * @return
   *   A new component definition instance.
   */
  public Component fromModule(String name, String moduleName, JsonObject config, int workers) {
    return addDefinition(new Component(name).setType(Component.MODULE).setModule(moduleName).setConfig(config).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

  /**
   * Creates a component address.
   */
  protected String createComponentAddress(String networkAddress, String componentName) {
    return String.format("%s.%s", networkAddress, componentName);
  }

  /**
   * Creates an array of worker addresses.
   */
  protected String[] createAddresses(String componentAddress, int numWorkers) {
    List<String> addresses = new ArrayList<String>();
    for (int i = 0; i < numWorkers; i++) {
      addresses.add(String.format("%s.%d", componentAddress, i+1));
    }
    return addresses.toArray(new String[addresses.size()]);
  }

  /**
   * Returns a network context representation of the network.
   *
   * @return
   *   A prepared network context.
   * @throws MalformedNetworkException 
   */
  public NetworkContext createContext() throws MalformedNetworkException {
    String address = definition.getString("address");
    if (address == null) {
      throw new MalformedNetworkException("No address specified.");
    }

    JsonObject context = new JsonObject();
    context.putString("address", address);
    context.putArray("auditors", new JsonArray(createAddresses(String.format("%s.audit", address), getNumAuditors())));
    context.putString("broadcast", String.format("%s.broadcast", address));
    context.putObject("definition", definition);

    // First, create all component contexts and then add connections.
    JsonObject components = definition.getObject("components");
    Iterator<String> iter = components.getFieldNames().iterator();

    // Create component contexts.
    JsonObject componentContexts = new JsonObject();
    while (iter.hasNext()) {
      JsonObject component = components.getObject(iter.next());
      JsonObject componentContext = new JsonObject();
      String componentName = component.getString("name");
      if (componentName == null) {
        throw new MalformedNetworkException("No component name specified.");
      }
      componentContext.putString("name", componentName);
      componentContext.putString("address", createComponentAddress(definition.getString("address"), component.getString("name")));
      componentContext.putObject("definition", component);
      componentContext.putArray("workers", new JsonArray(createAddresses(componentContext.getString("address"), componentContext.getObject("definition").getInteger("workers", 1))));
      componentContexts.putObject(componentContext.getString("name"), componentContext);
    }

    Iterator<String> iter2 = components.getFieldNames().iterator();
    while (iter2.hasNext()) {
      JsonObject component = components.getObject(iter2.next());
      JsonObject componentContext = componentContexts.getObject(component.getString("name"));

      JsonObject componentDef = componentContext.getObject("definition");

      // Iterate through each of the component's connections.
      JsonArray componentCons = componentDef.getArray("connections");
      JsonObject componentConnectionContexts = new JsonObject();

      if (componentCons != null) {
        Iterator<Object> iterCon = componentCons.iterator();
  
        while (iterCon.hasNext()) {
          // Get the component name and with it a reference to the component context.
          String name = iterCon.next().toString();
          JsonObject conContext = componentContexts.getObject(name);
          if (conContext == null) {
            continue;
          }
          JsonObject conDef = conContext.getObject("definition");
  
          // With the context, we can list all of the worker addresses.
          JsonObject connection = new JsonObject();
          connection.putString("name", name);

          // If the connection doesn't define a grouping, use a round grouping.
          JsonObject grouping = conDef.getObject("grouping");
          if (grouping == null) {
            grouping = new JsonObject().putString("grouping", RoundGrouping.class.getName()).putObject("definition", new JsonObject());
          }
          connection.putObject("grouping", grouping);

          // Add filter definitions to the connection.
          JsonArray filters = conDef.getArray("filters");
          if (filters == null) {
            filters = new JsonArray();
          }
          connection.putArray("filters", filters);

          connection.putArray("addresses", conContext.getArray("workers").copy());
  
          componentConnectionContexts.putObject(name, connection);
        }
      }

      // Finally, add the connections to the object.
      componentContext.putObject("connections", componentConnectionContexts);
    }

    // Component contexts are stored in context.workers.
    context.putObject("components", componentContexts);

    return NetworkContext.fromJson(context);
  }

}
