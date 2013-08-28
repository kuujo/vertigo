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
package com.blankstyle.vine.definition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Serializeable;
import com.blankstyle.vine.context.VineContext;


/**
 * A default vine context implementation.
 *
 * @author Jordan Halterman
 */
public class VineDefinition implements Serializeable<JsonObject> {

  private JsonObject definition = new JsonObject();

  private static final long DEFAULT_TIMEOUT = 5000;

  private static final long DEFAULT_EXPIRATION = 15000;

  public VineDefinition() {
  }

  public VineDefinition(JsonObject json) {
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
   */
  public VineDefinition setAddress(String address) {
    definition.putString("address", address);
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
  public VineDefinition setMessageTimeout(long timeout) {
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
  public VineDefinition setMessageExpiration(long expiration) {
    definition.putNumber("expiration", expiration);
    return this;
  }

  /**
   * Adds a connection to a seed definition.
   */
  private SeedDefinition addDefinition(SeedDefinition definition) {
    // Add the seed definition.
    JsonObject seeds = this.definition.getObject("seeds");
    if (seeds == null) {
      seeds = new JsonObject();
      this.definition.putObject("seeds", seeds);
    }
    if (!seeds.getFieldNames().contains(definition.getName())) {
      seeds.putObject(definition.getName(), definition.serialize());
    }

    // Add the seed connection.
    JsonArray connections = this.definition.getArray("connections");
    if (connections == null) {
      connections = new JsonArray();
      this.definition.putArray("connections", connections);
    }
    if (!connections.contains(definition.getName())) {
      connections.add(definition.getName());
    }
    return definition;
  }

  /**
   * Defines a feeder to a seed.
   *
   * @param definition
   *   A seed definition.
   * @return
   *   The passed seed definition.
   */
  public SeedDefinition feed(SeedDefinition definition) {
    return addDefinition(definition);
  }

  /**
   * Defines a feeder to a seed, creating a new seed definition.
   *
   * @param name
   *   The seed name.
   * @return
   *   A new seed definition.
   */
  public SeedDefinition feed(String name) {
    return feed(name, null, 1);
  }

  /**
   * Defines a feeder to a seed, creating a new seed definition.
   *
   * @param name
   *   The seed name.
   * @param main
   *   The seed main.
   * @return
   *   A new seed definition.
   */
  public SeedDefinition feed(String name, String main) {
    return feed(name, main, 1);
  }

  /**
   * Defines a feeder to a seed, creating a new seed definition.
   *
   * @param name
   *   The seed name.
   * @param main
   *   The seed main.
   * @param workers
   *   The number of seed workers.
   * @return
   *   A new seed definition.
   */
  public SeedDefinition feed(String name, String main, int workers) {
    return addDefinition(new SeedDefinition().setName(name).setMain(main).setWorkers(workers));
  }

  @Override
  public JsonObject serialize() {
    return definition;
  }

  /**
   * Creates a seed address.
   */
  protected String createSeedAddress(String vineAddress, String seedName) {
    return String.format("%s.%s", vineAddress, seedName);
  }

  /**
   * Creates an array of worker addresses.
   */
  protected String[] createWorkerAddresses(String seedAddress, int numWorkers) {
    List<String> addresses = new ArrayList<String>();
    for (int i = 0; i < numWorkers; i++) {
      addresses.add(String.format("%s.%d", seedAddress, i+1));
    }
    return (String[]) addresses.toArray();
  }

  /**
   * Returns a vine context representation of the vine.
   *
   * @return
   *   A prepared vine context.
   * @throws MalformedDefinitionException 
   */
  public VineContext toContext() throws MalformedDefinitionException {
    String address = definition.getString("address");
    if (address == null) {
      throw new MalformedDefinitionException("No address specified.");
    }

    JsonObject context = new JsonObject();
    context.putString("address", address);
    context.putObject("definition", definition);

    // First, create all seed contexts and then add connections.
    JsonObject seeds = definition.getObject("seeds");
    Iterator<String> iter = seeds.getFieldNames().iterator();

    // Create seed contexts:
    // {
    //   "name": "seed1",
    //   "workers": [
    //     "foo.seed1.1",
    //     "foo.seed1.2"
    //   ],
    //   "definition": {
    //     ...
    //   }
    // }
    JsonObject seedContexts = new JsonObject();
    while (iter.hasNext()) {
      JsonObject seed = seeds.getObject(iter.next());

      String seedName = seed.getString("name");
      if (seedName == null) {
        throw new MalformedDefinitionException("No seed name specified.");
      }

      // Create the basic seed context object.
      JsonObject seedContext = new JsonObject();
      seedContext.putString("name", seedName);
      seedContext.putString("address", createSeedAddress(definition.getString("address"), seed.getString("name")));
      seedContext.putObject("definition", seed);

      // Create an array of worker addresses.
      seedContext.putArray("workers", new JsonArray(createWorkerAddresses(seedContext.getString("address"), seedContext.getInteger("workers"))));

      // Add the seed context to the seeds object.
      seedContexts.putObject(seedContext.getString("name"), seedContext);
    }

    // Seed contexts are stored in context.seeds.
    context.putObject("seeds", seedContexts);

    JsonArray connections = definition.getArray("connections");
    if (connections == null) {
      connections = new JsonArray();
    }

    JsonObject connectionContexts = new JsonObject();

    // Create an object of connection information:
    // {
    //   "seed1": {
    //     "addresses": [
    //       "foo.seed1.1",
    //       "foo.seed1.2"
    //     ]
    //   }
    // }
    Iterator<Object> iter2 = connections.iterator();
    while (iter2.hasNext()) {
      String name = iter2.next().toString();
      JsonObject seedContext = seedContexts.getObject(name);
      if (seedContext == null) {
        continue;
      }

      JsonObject connection = new JsonObject();
      connection.putString("name", name);
      connection.putString("grouping", seedContext.getObject("definition").getString("grouping", "round"));
      connection.putArray("addresses", seedContext.getArray("workers").copy());

      connectionContexts.putObject(name, connection);
    }

    // Connection information is stored in context.connections.
    context.putObject("connections", connectionContexts);

    // Now iterate through each seed context and add connection information.
    // This needed to be done *after* those contexts are created because
    // we need to be able to get context information from connecting seeds.
    // {
    //   "seed1": {
    //     "addresses": [
    //       "foo.seed1.1",
    //       "foo.seed1.2"
    //     ],
    //     "grouping": "random"
    //   }
    //   ...
    // }
    Iterator<String> seedNames = seedContexts.getFieldNames().iterator();
    while (seedNames.hasNext()) {
      JsonObject seedContext = seedContexts.getObject(seedNames.next());
      JsonObject seedDef = seedContext.getObject("definition");

      // Iterate through each of the seed's connections.
      JsonArray seedCons = seedDef.getArray("connections");
      Iterator<Object> iterCon = seedCons.iterator();

      JsonObject seedConnectionContexts = new JsonObject();
      while (iterCon.hasNext()) {
        // Get the seed name and with it a reference to the seed context.
        String name = iterCon.next().toString();
        JsonObject conContext = seedContexts.getObject(name);
        if (conContext == null) {
          continue;
        }

        // With the context, we can list all of the worker addresses.
        JsonObject connection = new JsonObject();
        connection.putString("name", name);
        connection.putString("grouping", conContext.getString("grouping", "round"));
        connection.putArray("addresses", conContext.getArray("workers").copy());

        seedConnectionContexts.putObject(name, connection);
      }

      // Finally, add the connections to the object.
      seedContext.putObject("connections", seedConnectionContexts);
    }

    return new VineContext(context);
  }

}
