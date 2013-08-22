package com.blankstyle.vine;

import org.vertx.java.core.json.JsonObject;

/**
 * A seed context builder.
 *
 * @author Jordan Halterman
 */
public class SeedBuilder {

  private JsonObject config;

  public SeedBuilder() {
    this.config = new JsonObject();
  }

  public SeedBuilder(JsonObject config) {
    this.config = config;
  }

  /**
   * Sets the seed name.
   *
   * @param name
   *   The seed name.
   * @return
   *   The called object.
   */
  public SeedBuilder setName(String name) {
    config.putString("name", name);
    return this;
  }

  /**
   * Gets the seed name.
   *
   * @return
   *   The seed name.
   */
  public String getName() {
    return config.getString("name");
  }

  /**
   * Sets the seed address.
   *
   * @param address
   *   The seed address.
   * @return
   *   The called object.
   */
  public SeedBuilder setAddress(String address) {
    config.putString("address", address);
    return this;
  }

  /**
   * Returns the seed address.
   *
   * @return
   *   The seed address.
   */
  public String getAddress() {
    return config.getString("address");
  }

  /**
   * Sets the seed verticle.
   *
   * @param main
   *   The seed main.
   * @return
   *   The called object.
   */
  public SeedBuilder setMain(String main) {
    config.putString("main", main);
    return this;
  }

  /**
   * Gets the seed verticle.
   *
   * @return
   *   The seed main.
   */
  public String getMain() {
    return config.getString("main");
  }

  /**
   * Sets the number of workers on the seed.
   *
   * @param workers
   *   The number of workers.
   * @return
   *   The called object.
   */
  public SeedBuilder setWorkers(int workers) {
    config.putNumber("workers", workers);
    return this;
  }

  /**
   * Gets the number of workers assigned to the seed.
   *
   * @return
   *   The number of workers.
   */
  public int getWorkers() {
    return config.getInteger("workers");
  }

  /**
   * Sets the seed configuration.
   *
   * @param config
   *   The seed configuration.
   * @return
   *   The called object.
   */
  public SeedBuilder setConfig(JsonObject config) {
    config.putObject("config", config);
    return this;
  }

  /**
   * Returns the seed configuration.
   *
   * @return
   *   The seed configuration object.
   */
  public JsonObject getConfig() {
    return config.getObject("config");
  }

  /**
   * Creates a connection between the seed and another seed by name.
   *
   * @param name
   *   The conencted seed's name.
   * @return
   */
  public SeedBuilder connectTo(String name) {
    return null;
  }

  /**
   * Returns a JSON representation of the seed definition.
   *
   * @return
   *   A JSON representation of the seed definition.
   */
  public JsonObject toObject() {
    return config;
  }

}
