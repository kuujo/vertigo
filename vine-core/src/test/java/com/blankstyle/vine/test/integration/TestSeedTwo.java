package com.blankstyle.vine.test.integration;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.SeedVerticle;

import static org.vertx.testtools.VertxAssert.assertEquals;

public class TestSeedTwo extends SeedVerticle {

  @Override
  protected void process(JsonObject data) {
    assertEquals("Hello world!", data.getString("body"));
    emit(new JsonObject().putString("body", "Hello world again again!"));
  }

}
