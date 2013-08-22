package com.blankstyle.vine.impl;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.SeedContext;

public class JsonSeedContext implements SeedContext {

  protected JsonObject json;

  public JsonSeedContext() {
    this.json = new JsonObject();
  }

  public JsonSeedContext(JsonObject json) {
    this.json = json;
  }

  public static JsonSeedContext fromJson(JsonObject json) {
    return new JsonSeedContext(json);
  }

  @Override
  public String getAddress() {
    return json.getString("address");
  }

  public JsonObject toJson() {
    return json;
  }

}
