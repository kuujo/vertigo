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

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.hooks.ComponentHook;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/**
 * Vertigo event constants.
 *
 * Vertigo publishes events on the default Vert.x event bus when certain events
 * occur. These classes contain constants for event addresses and methods for
 * triggering specific events. Events are triggered by Vertigo. Users should
 * register handlers on the event bus to receive event notifications. For
 * messaging related events, see {@link ComponentHook} and other hook related
 * classes.
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
      eventClass.getConstructor(new Class<?>[]{EventBus.class}).newInstance(eventBus).trigger(args);
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
     * - address: the network address
     * - network: a JSON representation of the network context
     */
    public static final String DEPLOY = "vertigo.network.deploy";

    /**
     * A deploy event.
     *
     * Arguments:
     * - address: the network address
     * - context: the network context
     *
     * @author Jordan Halterman
     */
    public static final class Deploy extends Event {
      public Deploy(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        NetworkContext context = (NetworkContext) args[1];
        eventBus.publish(DEPLOY, new JsonObject().putString("address", address).putObject("context", context.getState()));
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
      public Start(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        NetworkContext context = (NetworkContext) args[1];
        eventBus.publish(START, new JsonObject().putString("address", address).putObject("context", context.getState()));
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
      public Shutdown(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        NetworkContext context = (NetworkContext) args[1];
        eventBus.publish(SHUTDOWN, new JsonObject().putString("address", address).putObject("context", context.getState()));
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
     * - string: the component address
     * - context: the instance context
     *
     * @author Jordan Halterman
     */
    public static final class Deploy extends Event {
      public Deploy(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        InstanceContext context = (InstanceContext) args[1];
        eventBus.publish(DEPLOY, new JsonObject().putString("address", address).putObject("context", context.getState()));
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
     * - string: the component address
     * - context: the instance context
     *
     * @author Jordan Halterman
     */
    public static final class Start extends Event {
      public Start(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        InstanceContext context = (InstanceContext) args[1];
        eventBus.publish(START, new JsonObject().putString("address", address).putObject("context", context.getState()));
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
     * - string: the component address
     * - context: the instance context
     *
     * @author Jordan Halterman
     */
    public static final class Shutdown extends Event {
      public Shutdown(EventBus eventBus) {
        super(eventBus);
      }

      @Override
      public void trigger(Object... args) {
        String address = (String) args[0];
        InstanceContext context = (InstanceContext) args[1];
        eventBus.publish(SHUTDOWN, new JsonObject().putString("address", address).putObject("context", context.getState()));
      }
    }

  }

}
