package com.blankstyle.vine.impl;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.VineContext;

public class JsonVineContext implements VineContext {

  protected JsonObject json;

  public JsonVineContext() {
    this.json = new JsonObject();
  }

  public JsonVineContext(JsonObject json) {
    this.json = json;
  }

  public static JsonVineContext fromJson(JsonObject json) {
    return new JsonVineContext(json);
  }

  @Override
  public String getAddress() {
    // TODO Auto-generated method stub
    return null;
  }

  public JsonObject toJson() {
    return json;
  }

}
