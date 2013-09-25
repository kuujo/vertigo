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
package net.kuujo.vevent.definition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.kuujo.vevent.Serializeable;
import net.kuujo.vevent.context.NetworkContext;
import net.kuujo.vevent.grouping.RoundGrouping;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;


/**
 * A default vine context implementation.
 *
 * @author Jordan Halterman
 */
public class NetworkDefinition implements Serializeable<JsonObject> {

  private JsonObject definition = new JsonObject();

  private static final int DEFAULT_QUEUE_SIZE = 1000;

  private static final long DEFAULT_TIMEOUT = 5000;

  private static final long DEFAULT_EXPIRATION = 15000;

  public NetworkDefinition() {
  }

  public NetworkDefinition(JsonObject json) {
    definition = json;
  }

  /**
   * Gets the vine address.
   */
  public String getAddress() {
    return definition.getString("address");
  }

  /**
   * Sets the vine address.
   *
   * @param address
   *   The vine address.
   * @return
   *   The called vine definition.
   */
  public NetworkDefinition setAddress(String address) {
    definition.putString("address", address);
    return this;
  }

  /**
   * Gets a vine option.
   *
   * @param option
   *   The option to get.
   * @return
   *   The option value.
   */
  public String getOption(String option) {
    return definition.getString(option);
  }

  /**
   * Sets a vine option.
   *
   * @param option
   *   The option to set.
   * @param value
   *   The option value.
   * @return
   *   The called vine definition.
   */
  public NetworkDefinition setOption(String option, String value) {
    switch (option) {
      case "address":
        return setAddress(value);
      default:
        definition.putString(option, value);
        break;
    }
    return this;
  }

  /**
   * Gets the maximum vine queue size.
   *
   * @return
   *   The maximum vine queue size.
   */
  public int getMaxQueueSize() {
    return definition.getInteger("queue_size", DEFAULT_QUEUE_SIZE);
  }

  /**
   * Sets the maximum vine queue size.
   *
   * @param queueSize
   *   The maximum vine process queue size.
   * @return
   *   The called vine definition.
   */
  public NetworkDefinition setMaxQueueSize(int queueSize) {
    definition.putNumber("queue_size", queueSize);
    return this;
  }

  /**
   * Gets the vine message timeout.
   */
  public long getMessageTimeout() {
    return definition.getLong("timeout", DEFAULT_TIMEOUT);
  }

  /**
   * Sets the vine message timeout.
   *
   * @param timeout
   *   The vine message timeout.
   */
  public NetworkDefinition setMessageTimeout(long timeout) {
    definition.putNumber("timeout", timeout);
    return this;
  }

  /**
   * Gets the message expiration.
   */
  public long getMessageExpiration() {
    return definition.getLong("expiration", DEFAULT_EXPIRATION);
  }

  /**
   * Sets the message expiration.
   *
   * @param expiration
   *   The vine message expiration.
   */
  public NetworkDefinition setMessageExpiration(long expiration) {
    definition.putNumber("expiration", expiration);
    return this;
  }

  /**
   * Adds a root definition.
   */
  private ComponentDefinition addDefinition(ComponentDefinition definition) {
    // Add the root definition.
    JsonObject roots = this.definition.getObject("roots");
    if (roots == null) {
      roots = new JsonObject();
      this.definition.putObject("roots", roots);
    }
    if (!roots.getFieldNames().contains(definition.getName())) {
      roots.putObject(definition.getName(), definition.serialize());
    }
    return definition;
  }

  /**
   * Adds a root to the network.
   *
   * @param definition
   *   A root definition.
   * @return
   *   The given root definition.
   */
  public ComponentDefinition from(ComponentDefinition definition) {
    return addDefinition(definition);
  }

  /**
   * Adds a root to the network.
   *
   * @param name
   *   The root name.
   * @return
   *   A new root definition.
   */
  public ComponentDefinition from(String name) {
    return from(name, null, 1);
  }

  /**
   * Adds a root to the network.
   *
   * @param name
   *   The root name.
   * @param main
   *   The root main.
   * @return
   *   A new root definition.
   */
  public ComponentDefinition from(String name, String main) {
    return from(name, main, 1);
  }

  /**
   * Adds a root to the network.
   *
   * @param name
   *   The root name.
   * @param main
   *   The root main.
   * @param workers
   *   The number of root workers.
   * @return
   *   A new root definition.
   */
  public ComponentDefinition from(String name, String main, int workers) {
    return addDefinition(new ComponentDefinition().setName(name).setMain(main).setWorkers(workers));
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
  protected String[] createWorkerAddresses(String componentAddress, int numWorkers) {
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
   * @throws MalformedDefinitionException 
   */
  public NetworkContext createContext() throws MalformedDefinitionException {
    String address = definition.getString("address");
    if (address == null) {
      throw new MalformedDefinitionException("No address specified.");
    }

    JsonObject context = new JsonObject();
    context.putString("address", address);
    context.putObject("definition", definition);

    // First, create all component contexts and then add connections.
    JsonObject roots = definition.getObject("roots");
    Iterator<String> iter = roots.getFieldNames().iterator();

    // Create component contexts:
    // {
    //   "name": "component1",
    //   "workers": [
    //     "foo.component1.1",
    //     "foo.component1.2"
    //   ],
    //   "definition": {
    //     ...
    //   }
    // }
    JsonObject componentContexts = new JsonObject();
    while (iter.hasNext()) {
      JsonObject root = roots.getObject(iter.next());
      JsonObject componentDefinitions = buildComponentsRecursive(root);
      Iterator<String> iterComponents = componentDefinitions.getFieldNames().iterator();
      while (iterComponents.hasNext()) {
        JsonObject componentDef = componentDefinitions.getObject(iterComponents.next());
        JsonObject componentContext = new JsonObject();
        String componentName = componentDef.getString("name");
        if (componentName == null) {
          throw new MalformedDefinitionException("No component name specified.");
        }
        componentContext.putString("name", componentName);
        componentContext.putString("address", createComponentAddress(definition.getString("address"), componentDef.getString("name")));
        componentContext.putObject("definition", componentDef);
        componentContext.putArray("workers", new JsonArray(createWorkerAddresses(componentContext.getString("address"), componentContext.getObject("definition").getInteger("workers"))));
        componentContexts.putObject(componentContext.getString("name"), componentContext);
      }
    }

    // Worker contexts are stored in context.workers.
    context.putObject("workers", componentContexts);
    

    JsonArray connections = definition.getArray("connections");
    if (connections == null) {
      connections = new JsonArray();
    }

    JsonObject connectionContexts = new JsonObject();

    // Create an object of connection information:
    // {
    //   "component1": {
    //     "addresses": [
    //       "foo.component1.1",
    //       "foo.component1.2"
    //     ]
    //   }
    // }
    Iterator<Object> iter2 = connections.iterator();
    while (iter2.hasNext()) {
      String name = iter2.next().toString();
      JsonObject componentContext = componentContexts.getObject(name);
      if (componentContext == null) {
        continue;
      }

      JsonObject connection = new JsonObject();
      connection.putString("name", name);

      JsonObject grouping = componentContext.getObject("definition").getObject("grouping");
      if (grouping == null) {
        grouping = new RoundGrouping().serialize();
      }

      connection.putObject("grouping", grouping);
      connection.putArray("addresses", componentContext.getArray("workers").copy());

      connectionContexts.putObject(name, connection);
    }

    // Connection information is stored in context.connections.
    context.putObject("connections", connectionContexts);

    // Now iterate through each component context and add connection information.
    // This needed to be done *after* those contexts are created because
    // we need to be able to get context information from connecting components.
    // {
    //   "component1": {
    //     "addresses": [
    //       "foo.component1.1",
    //       "foo.component1.2"
    //     ],
    //     "grouping": "random"
    //   }
    //   ...
    // }
    Iterator<String> componentNames = componentContexts.getFieldNames().iterator();
    while (componentNames.hasNext()) {
      JsonObject componentContext = componentContexts.getObject(componentNames.next());
      JsonObject componentDef = componentContext.getObject("definition");

      // Iterate through each of the component's connections.
      JsonObject componentCons = componentDef.getObject("connections");
      JsonObject componentConnectionContexts = new JsonObject();

      if (componentCons != null) {
        Set<String> conKeys = componentCons.getFieldNames();
        Iterator<String> iterCon = conKeys.iterator();
  
        while (iterCon.hasNext()) {
          // Get the component name and with it a reference to the component context.
          String name = iterCon.next().toString();
          JsonObject conContext = componentContexts.getObject(name);
          if (conContext == null) {
            continue;
          }
  
          // With the context, we can list all of the worker addresses.
          JsonObject connection = new JsonObject();
          connection.putString("name", name);

          // If the connection doesn't define a grouping, use a round grouping.
          JsonObject grouping = conContext.getObject("grouping");
          if (grouping == null) {
            grouping = new RoundGrouping().serialize();
          }

          connection.putObject("grouping", grouping);
          connection.putArray("addresses", conContext.getArray("workers").copy());
  
          componentConnectionContexts.putObject(name, connection);
        }
      }

      // Finally, add the connections to the object.
      componentContext.putObject("connections", componentConnectionContexts);
    }

    return new NetworkContext(context);
  }

  private JsonObject buildComponentsRecursive(JsonObject componentDefinition) {
    return buildComponentsRecursive(componentDefinition, new JsonObject());
  }

  private JsonObject buildComponentsRecursive(JsonObject componentDefinition, JsonObject components) {
    components.putObject(componentDefinition.getString("name"), componentDefinition);
    JsonObject connections = componentDefinition.getObject("connections");
    if (connections != null) {
      Iterator<String> iter = connections.getFieldNames().iterator();
      while (iter.hasNext()) {
        buildComponentsRecursive(connections.getObject(iter.next()), components);
      }
    }
    return components;
  }

}
