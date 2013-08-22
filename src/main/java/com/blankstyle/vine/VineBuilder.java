package com.blankstyle.vine;

import org.vertx.java.core.json.JsonObject;

public class VineBuilder {

  private JsonObject config;

  public VineBuilder() {
    this.config = new JsonObject();
  }

  public VineBuilder(String name) {
    this();
    setName(name);
  }

  public VineBuilder(JsonObject config) {
    this.config = config;
  }

  public VineBuilder(String name, JsonObject config) {
    this(config);
    setName(name);
  }

  /**
   * Sets the vine name.
   *
   * @param name
   *   The vine name.
   * @return
   *   The called object.
   */
  public VineBuilder setName(String name) {
    config.putString("name", name);
    return this;
  }

  /**
   * Gets the vine name.
   *
   * @return
   *   The vine name.
   */
  public String getName() {
    return config.getString("name");
  }

  /**
   * Sets a new seed on the vine.
   *
   * @param name
   *   The seed name.
   * @param main
   *   The seed verticle.
   * @return
   *   A new seed definition.
   */
  public SeedBuilder setSeed(String name, String main) {
    JsonObject seeds = config.getObject("seeds");
    if (seeds == null) {
      seeds = new JsonObject();
      config.putObject("seeds", seeds);
    }
    SeedBuilder seed = new SeedBuilder();
    seed.setName(name);
    seed.setMain(main);
    seeds.putObject(seed.getName(), seed.toObject());
    return seed;
  }

  /**
   * Returns a seed by name.
   *
   * @param name
   *   The seed name.
   * @return
   *   The seed definition.
   */
  public SeedBuilder getSeed(String name) {
    return new SeedBuilder(config.getObject("seeds").getObject(name));
  }

  /**
   * Creates a vine context.
   *
   * @return
   *   A vine context as a JSON object.
   */
  public JsonObject createContext() {
    JsonObject context = config.copy();
    String address = context.getString("address");
    if (address == null) {
      context.putString("address", String.format("vine.%s", getName()));
    }
    return context;
  }

}
