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
import net.kuujo.vine.java.SeedVerticle;
import net.kuujo.vine.messaging.JsonMessage;

public class TestConsistentSeed extends SeedVerticle {

  private String received;

  @Override
  public void handle(JsonMessage message) {
    String body = message.body().getString("body");

    // The seed instance should always receive the same string. The test runs
    // with only two strings - one of an odd length and one of an event length.
    // Consistent hashing means when two instances of this seed are running, each
    // instance should receive one of those two strings at all times.
    if (received == null) {
      received = body;
    }
    else {
      assertEquals(received, body);
    }
    emit(message.body().copy());
    ack(message);
  }

}
