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
package net.kuujo.vertigo.test.unit;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.network.MalformedNetworkException;
import net.kuujo.vertigo.network.Network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Definition to context tests.
 *
 * @author Jordan Halterman
 */
public class ContextTest {

  @Test
  public void testNetworkContext() {
    Network network = new Network("test");
    network.setNumAuditors(2);
    network.enableAcking();

    network.addVerticle("test1", "net.kuujo.vertigo.VertigoVerticle");
    network.addVerticle("test2", "net.kuujo.vertigo.VertigoVerticle").addInput("test1");

    NetworkContext context;
    try {
      context = network.createContext();
      assertEquals(2, context.getAuditors().size());
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

}
