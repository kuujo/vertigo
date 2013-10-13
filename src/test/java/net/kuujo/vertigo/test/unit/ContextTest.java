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

import net.kuujo.vertigo.MalformedNetworkException;
import net.kuujo.vertigo.Network;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.ConnectionContext;
import net.kuujo.vertigo.context.FilterContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.WorkerContext;
import net.kuujo.vertigo.dispatcher.AllDispatcher;
import net.kuujo.vertigo.dispatcher.Dispatcher;
import net.kuujo.vertigo.dispatcher.FieldsDispatcher;
import net.kuujo.vertigo.dispatcher.RandomDispatcher;
import net.kuujo.vertigo.dispatcher.RoundRobinDispatcher;
import net.kuujo.vertigo.filter.Condition;
import net.kuujo.vertigo.filter.FieldFilter;
import net.kuujo.vertigo.filter.SourceFilter;
import net.kuujo.vertigo.filter.TagsFilter;
import net.kuujo.vertigo.grouping.AllGrouping;
import net.kuujo.vertigo.grouping.FieldsGrouping;
import net.kuujo.vertigo.grouping.RandomGrouping;
import net.kuujo.vertigo.grouping.RoundGrouping;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertTrue;
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
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new RoundGrouping());

    try {
      NetworkContext context = network.createContext();
      assertEquals(2, context.getAuditors().size());
      assertEquals(2, context.getNumAuditors());
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testComponentContext() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new RoundGrouping());

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test2");
      assertEquals("test.test2", component.address());
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testWorkerContext() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 1).setConfig(new JsonObject().putString("foo", "bar")).groupBy(new RoundGrouping());

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test2");
      WorkerContext worker = component.getWorkerContexts().iterator().next();
      assertEquals("test.test2.1", worker.address());
      assertEquals("bar", worker.config().getString("foo"));
      assertEquals("test2", worker.getComponentContext().getDefinition().name());
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testRandomGrouping() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new RandomGrouping());

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test1");
      ConnectionContext connectionContext = component.getConnectionContext("test2");
      assertEquals(2, connectionContext.getAddresses().size());
      try {
        Dispatcher dispatcher = connectionContext.getGrouping().createDispatcher();
        assertTrue(dispatcher instanceof RandomDispatcher);
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testRoundGrouping() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new RoundGrouping());

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test1");
      ConnectionContext connectionContext = component.getConnectionContext("test2");
      assertEquals(2, connectionContext.getAddresses().size());
      try {
        Dispatcher dispatcher = connectionContext.getGrouping().createDispatcher();
        assertTrue(dispatcher instanceof RoundRobinDispatcher);
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testFieldsGrouping() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new FieldsGrouping("test"));

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test1");
      ConnectionContext connectionContext = component.getConnectionContext("test2");
      assertEquals(2, connectionContext.getAddresses().size());
      try {
        Dispatcher dispatcher = connectionContext.getGrouping().createDispatcher();
        assertTrue(dispatcher instanceof FieldsDispatcher);
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testAllGrouping() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new AllGrouping());

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test1");
      ConnectionContext connectionContext = component.getConnectionContext("test2");
      assertEquals(2, connectionContext.getAddresses().size());
      try {
        Dispatcher dispatcher = connectionContext.getGrouping().createDispatcher();
        assertTrue(dispatcher instanceof AllDispatcher);
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTagsFilter() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new RoundGrouping()).filterBy(new TagsFilter("foo"));

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test1");
      ConnectionContext connectionContext = component.getConnectionContext("test2");
      assertEquals(2, connectionContext.getAddresses().size());
      try {
        FilterContext filterContext = connectionContext.getFilters().iterator().next();
        Condition filter = filterContext.createCondition();
        assertTrue(filter instanceof TagsFilter.TagsCondition);
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testFieldFilter() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new RoundGrouping()).filterBy(new FieldFilter("foo", "bar"));

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test1");
      ConnectionContext connectionContext = component.getConnectionContext("test2");
      assertEquals(2, connectionContext.getAddresses().size());
      try {
        FilterContext filterContext = connectionContext.getFilters().iterator().next();
        Condition filter = filterContext.createCondition();
        assertTrue(filter instanceof FieldFilter.FieldCondition);
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testSourceFilter() {
    Network network = new Network("test");
    network.fromVerticle("test1", "net.kuujo.vertigo.java.VertigoVerticle")
      .toVerticle("test2", "net.kuujo.vertigo.java.VertigoVerticle", 2).groupBy(new RoundGrouping()).filterBy(new SourceFilter("foo"));

    try {
      NetworkContext context = network.createContext();
      ComponentContext component = context.getComponentContext("test1");
      ConnectionContext connectionContext = component.getConnectionContext("test2");
      assertEquals(2, connectionContext.getAddresses().size());
      try {
        FilterContext filterContext = connectionContext.getFilters().iterator().next();
        Condition filter = filterContext.createCondition();
        assertTrue(filter instanceof SourceFilter.SourceCondition);
      }
      catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
    catch (MalformedNetworkException e) {
      fail(e.getMessage());
    }
  }

}
