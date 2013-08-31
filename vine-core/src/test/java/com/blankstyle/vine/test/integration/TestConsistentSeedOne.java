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
package com.blankstyle.vine.test.integration;

import static org.vertx.testtools.VertxAssert.assertEquals;

import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.SeedVerticle;

public class TestConsistentSeedOne extends SeedVerticle {

  private static final String VALUE = "a";

  @Override
  protected void process(JsonObject data) {
    String body = data.getString("body");
    assertEquals(VALUE, body);
    emit(data);
  }

}
