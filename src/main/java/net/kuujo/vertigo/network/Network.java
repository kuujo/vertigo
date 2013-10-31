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
package net.kuujo.vertigo.network;

import java.util.UUID;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A Vertigo network.
 *
 * @author Jordan Halterman
 */
public class Network implements Serializable {
  public static final String ADDRESS = "address";
  public static final String BROADCAST = "broadcast";
  public static final String AUDITORS = "auditors";
  public static final String ACKING = "acking";
  public static final String ACK_EXPIRE = "ack_expire";
  public static final String COMPONENTS = "components";

  public static final long DEFAULT_ACK_EXPIRE = 30000;

  private JsonObject definition;

  public Network() {
    definition = new JsonObject();
    init();
  }

  public Network(String address) {
    definition = new JsonObject().putString(ADDRESS, address);
    init();
  }

  /**
   * Initializes the internal definition.
   */
  private void init() {
    String address = definition.getString(ADDRESS);
    if (address == null) {
      address = UUID.randomUUID().toString();
      definition.putString(ADDRESS, address);
    }

    String broadcast = definition.getString(BROADCAST);
    if (broadcast == null) {
      definition.putString(BROADCAST, String.format("%s.%s", address, BROADCAST));
    }
  }

  /**
   * Returns the network address.
   *
   * @return
   *   The network address.
   */
  public String getAddress() {
    return definition.getString(ADDRESS);
  }

  /**
   * Returns the number of network auditors.
   *
   * @return
   *   The number of network auditors.
   */
  public int getNumAuditors() {
    return definition.getInteger(AUDITORS, 1);
  }

  /**
   * Sets the number of network auditors.
   *
   * @param numAuditors
   *   The number of network auditors.
   * @return
   *   The called network instance.
   */
  public Network setNumAuditors(int numAuditors) {
    definition.putNumber(AUDITORS, numAuditors);
    return this;
  }

  /**
   * Enables acking on the network.
   *
   * @return
   *   The called network instance.
   */
  public Network enableAcking() {
    definition.putBoolean(ACKING, true);
    return this;
  }

  /**
   * Disables acking on the network.
   *
   * @return
   *   The called network instance.
   */
  public Network disableAcking() {
    definition.putBoolean(ACKING, false);
    return this;
  }

  /**
   * Returns a boolean indicating whether acking is enabled.
   *
   * @return
   *   Indicates whether acking is enabled for the network.
   */
  public boolean isAckingEnabled() {
    return definition.getBoolean(ACKING, true);
  }

  /**
   * Sets the network ack expiration.
   *
   * @param expire
   *   An ack expiration.
   * @return
   *   The called network instance.
   */
  public Network setAckExpire(long expire) {
    definition.putNumber(ACK_EXPIRE, expire);
    return this;
  }

  /**
   * Gets the network ack expiration.
   *
   * @return
   *   Ack expiration for the network.
   */
  public long getAckExpire() {
    return definition.getLong(ACK_EXPIRE, DEFAULT_ACK_EXPIRE);
  }

  /**
   * Gets a component by address.
   *
   * @param address
   *   The component address.
   * @return
   *   A component instance, or null if the component does not exist in the network.
   */
  public Component<?> getComponent(String address) {
    JsonObject components = definition.getObject(COMPONENTS);
    if (components == null) {
      components = new JsonObject();
      definition.putObject(COMPONENTS, components);
    }

    if (components.getFieldNames().contains(address)) {
      try {
        return Serializer.deserialize(components.getObject(address));
      }
      catch (SerializationException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Adds a component to the network.
   *
   * @param component
   *   The component to add.
   * @return
   *   The added component instance.
   */
  public <T extends Component<?>> T addComponent(T component) {
    JsonObject components = definition.getObject(COMPONENTS);
    if (components == null) {
      components = new JsonObject();
      definition.putObject(COMPONENTS, components);
    }
    components.putObject(component.getAddress(), Serializer.serialize(component));
    return component;
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address) {
    return addComponent(new Verticle(address));
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address.
   * @param main
   *   The verticle main.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address, String main) {
    return addComponent(new Verticle(address).setMain(main));
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address.
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle component configuration.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address, String main, JsonObject config) {
    return addComponent(new Verticle(address).setMain(main).setConfig(config));
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address.
   * @param main
   *   The verticle main.
   * @param instances
   *   The number of component instances.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address, String main, int instances) {
    return addComponent(new Verticle(address).setMain(main).setInstances(instances));
  }

  /**
   * Adds a verticle component to the network.
   *
   * @param address
   *   The component address.
   * @param main
   *   The verticle main.
   * @param config
   *   The verticle component configuration.
   * @param instances
   *   The number of component instances.
   * @return
   *   The new verticle component instance.
   */
  public Verticle addVerticle(String address, String main, JsonObject config, int instances) {
    return addComponent(new Verticle(address).setMain(main).setConfig(config).setInstances(instances));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address) {
    return addComponent(new Module(address));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleName
   *   The module name.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address, String moduleName) {
    return addComponent(new Module(address).setModule(moduleName));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleName
   *   The module name.
   * @param config
   *   The module component configuration.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address, String moduleName, JsonObject config) {
    return addComponent(new Module(address).setModule(moduleName).setConfig(config));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleName
   *   The module name.
   * @param instances
   *   The number of component instances.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address, String moduleName, int instances) {
    return addComponent(new Module(address).setModule(moduleName).setInstances(instances));
  }

  /**
   * Adds a module component to the network.
   *
   * @param address
   *   The component address.
   * @param moduleName
   *   The module name.
   * @param config
   *   The module component configuration.
   * @param instances
   *   The number of component instances.
   * @return
   *   The new module component instance.
   */
  public Module addModule(String address, String moduleName, JsonObject config, int instances) {
    return addComponent(new Module(address).setModule(moduleName).setConfig(config).setInstances(instances));
  }

  /**
   * Creates a network context from network definition.
   *
   * @return
   *   A network context.
   * @throws MalformedNetworkException
   *   If the network definition is invalid.
   */
  public NetworkContext createContext() throws MalformedNetworkException {
    JsonObject context = definition.copy();

    String address = context.getString(ADDRESS);
    if (address == null) {
      address = UUID.randomUUID().toString();
      context.putString(ADDRESS, address);
    }

    context.putString(BROADCAST, context.getString(BROADCAST, String.format("%s.%s", address, BROADCAST)));
    context.putBoolean(ACKING, context.getBoolean(ACKING, true));
    context.putNumber(ACK_EXPIRE, context.getLong(ACK_EXPIRE, DEFAULT_ACK_EXPIRE));

    int numAuditors = context.getInteger(AUDITORS, 1);
    JsonArray auditors = new JsonArray();
    for (int i = 0; i < numAuditors; i++) {
      auditors.add(String.format("%s.auditor.%d", getAddress(), i+1));
    }
    context.putArray(AUDITORS, auditors);

    JsonObject componentDefs = definition.getObject(COMPONENTS);
    if (componentDefs == null) {
      componentDefs = new JsonObject();
    }

    JsonObject componentContexts = new JsonObject();
    for (String componentAddress : componentDefs.getFieldNames()) {
      JsonObject componentContext = Serializer.serialize(getComponent(componentAddress).createContext());
      if (componentContext != null) {
        // Adding the parent context here causes a circular reference in serialization.
        // componentContexts.putObject(componentAddress, componentContext.putObject("parent", context));
        componentContexts.putObject(componentAddress, componentContext);
      }
    }
    context.putObject(COMPONENTS, componentContexts);
    return NetworkContext.fromJson(context);
  }

  @Override
  public JsonObject getState() {
    return definition;
  }

  @Override
  public void setState(JsonObject state) {
    definition = state;
  }

}
