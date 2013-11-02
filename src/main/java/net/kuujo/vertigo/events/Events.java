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
package net.kuujo.vertigo.events;

import java.lang.reflect.InvocationTargetException;

import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.network.Networks;
import net.kuujo.vertigo.serializer.Serializer;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/**
 * Vertigo event constants.
 *
 * @author Jordan Halterman
 */
public final class Events {

  private final EventBus eventBus;

  public Events(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  /**
   * Triggers an event.
   *
   * @param eventClass
   *   The event class.
   * @param args
   *   A list of event arguments.
   */
  public void trigger(Class<? extends Event> eventClass, Object... args) {
    try {
      eventClass.getConstructor(new Class<?>[]{EventBus.class}).newInstance(eventBus).trigger(args);;
    }
    catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
        InvocationTargetException | NoSuchMethodException | SecurityException e) {
      // Do nothing.
    }
  }

  /**
   * A Vertigo event.
   */
  private static abstract class Event {
    protected EventBus eventBus;
    protected Event(EventBus eventBus) {
      this.eventBus = eventBus;
    }

    /**
     * Triggers the event.
     *
     * @param args
     *   Event arguments.
     */
    public abstract void trigger(Object... args);
  }

  /**
   * Network events.
   *
   * @author Jordan Halterman
   */
  public static final class Network {

    /**
     * Triggered when a network is deployed.
     *
     * Keys:
     * - network: a JSON representation of the network definition
     */
    public static final String DEPLOY = "vertigo.network.deploy";

    /**
     * A deploy event.
     *
     * Arguments:
     * - Network: the network being deployed
     *
     * @author Jordan Halterman
     */
    public static final class Deploy extends Event {
      protected Deploy(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        net.kuujo.vertigo.network.Network network = (net.kuujo.vertigo.network.Network) args[0];
        eventBus.publish(DEPLOY, new JsonObject().putObject("network", Networks.toJson(network)));
      }
    }

    /**
     * Triggered when a network is started.
     *
     * Keys:
     * - address: the address of the started network
     * - context: a JSON representation of the network context
     */
    public static final String START = "vertigo.network.start";

    /**
     * A start event.
     *
     * Arguments:
     * - string: the network address
     * - context: the network context
     *
     * @author Jordan Halterman
     */
    public static final class Start extends Event {
      protected Start(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        NetworkContext context = (NetworkContext) args[1];
        eventBus.publish(START, new JsonObject().putString("address", address).putObject("context", Serializer.serialize(context)));
      }
    }

    /**
     * Triggered when a network is shutdown.
     *
     * Keys:
     * - context: a JSON representation of the network context
     */
    public static final String SHUTDOWN = "vertigo.network.shutdown";

    /**
     * A shutdown event.
     *
     * Arguments:
     * - NetworkContext: the shutdown network context
     *
     * @author Jordan Halterman
     */
    public static final class Shutdown extends Event {
      protected Shutdown(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        NetworkContext context = (NetworkContext) args[0];
        eventBus.publish(SHUTDOWN, new JsonObject().putObject("context", Serializer.serialize(context)));
      } 
    }

  }

  /**
   * Component events.
   *
   * @author Jordan Halterman
   */
  public static final class Component {

    /**
     * Triggered when a component instance is deployed.
     *
     * Keys:
     * - address: the component address
     * - context: a JSON representation of the component instance context
     */
    public static final String DEPLOY = "vertigo.component.deploy";

    /**
     * A component deploy event.
     *
     * Arguments:
     * - string: the network address
     * - context: the network context
     *
     * @author Jordan Halterman
     */
    public static final class Deploy extends Event {
      protected Deploy(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        NetworkContext context = (NetworkContext) args[1];
        eventBus.publish(DEPLOY, new JsonObject().putString("address", address).putObject("context", Serializer.serialize(context)));
      }
    }

    /**
     * Triggered when a component instance is started.
     *
     * Keys:
     * - address: the component address
     * - context: a JSON representation of the component instance context
     */
    public static final String START = "vertigo.component.start";

    /**
     * A start event.
     *
     * Arguments:
     * - string: the network address
     * - context: the network context
     *
     * @author Jordan Halterman
     */
    public static final class Start extends Event {
      protected Start(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        NetworkContext context = (NetworkContext) args[1];
        eventBus.publish(START, new JsonObject().putString("address", address).putObject("context", Serializer.serialize(context)));
      }
    }

    /**
     * Triggered when a component instance is shut down.
     *
     * Keys:
     * - address: the component address
     * - context: a JSON representation of the component instance context
     */
    public static final String SHUTDOWN = "vertigo.component.shutdown";

    /**
     * A stop event.
     *
     * Arguments:
     * - string: the network address
     * - context: the network context
     *
     * @author Jordan Halterman
     */
    public static final class Shutdown extends Event {
      protected Shutdown(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        NetworkContext context = (NetworkContext) args[1];
        eventBus.publish(SHUTDOWN, new JsonObject().putString("address", address).putObject("context", Serializer.serialize(context)));
      }
    }

  }

}
