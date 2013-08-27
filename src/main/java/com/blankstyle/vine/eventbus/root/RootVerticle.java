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
package com.blankstyle.vine.eventbus.root;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.context.JsonRootContext;
import com.blankstyle.vine.context.RootContext;
import com.blankstyle.vine.eventbus.Action;
import com.blankstyle.vine.eventbus.Argument;
import com.blankstyle.vine.eventbus.ArgumentsDefinition;
import com.blankstyle.vine.eventbus.AsynchronousAction;
import com.blankstyle.vine.eventbus.CommandDispatcher;
import com.blankstyle.vine.eventbus.DefaultCommandDispatcher;
import com.blankstyle.vine.eventbus.JsonCommand;
import com.blankstyle.vine.eventbus.SynchronousAction;
import com.blankstyle.vine.heartbeat.DefaultHeartBeatMonitor;
import com.blankstyle.vine.heartbeat.HeartBeatMonitor;

/**
 * A Vine root verticle.
 *
 * @author Jordan Halterman
 */
public class RootVerticle extends BusModBase implements Handler<Message<JsonObject>> {

  private RootContext context;

  private CommandDispatcher dispatcher = new DefaultCommandDispatcher() {{
    registerAction(Register.NAME, Register.class);
    registerAction(Deploy.NAME, Deploy.class);
    registerAction(Undeploy.NAME, Undeploy.class);
    registerAction(LoadContext.NAME, LoadContext.class);
    registerAction(UpdateContext.NAME, UpdateContext.class);
  }};

  private Map<String, String> heartbeatMap = new HashMap<String, String>();

  private int heartbeatCounter;

  private HeartBeatMonitor heartbeatMonitor;

  private Set<String> stems = new HashSet<String>();

  @Override
  public void start() {
    heartbeatMonitor = new DefaultHeartBeatMonitor().setVertx(vertx).setEventBus(vertx.eventBus());
    context = new JsonRootContext(container.config());
    dispatcher.setVertx(vertx);
    dispatcher.setEventBus(vertx.eventBus());
    dispatcher.setContext(context);
    vertx.eventBus().registerHandler(context.getAddress(), this);
  }

  @Override
  public void handle(final Message<JsonObject> message) {
    dispatcher.dispatch(new JsonCommand(message.body()), new Handler<AsyncResult<Object>>() {
      @Override
      public void handle(AsyncResult<Object> result) {
        if (result.succeeded()) {
          message.reply(result.result());
        }
        else {
          message.reply(result.cause());
        }
      }
    });
  }

  /**
   * Registers a stem address.
   *
   * @param address
   *   The stem address.
   */
  private void registerStem(String address) {
    stems.add(address);
  }

  /**
   * Unregisters a stem address.
   *
   * @param address
   *   The stem address.
   */
  private void unregisterStem(String address) {
    if (stems.contains(address)) {
      stems.remove(address);
    }
  }

  /**
   * Returns the next heartbeat address.
   */
  private String nextHeartBeatAddress() {
    heartbeatCounter++;
    return String.format("%s.heartbeat.%s", context.getAddress(), heartbeatCounter);
  }

  /**
   * A root verticle register action.
   *
   * This action is called when a new stem is started. The stem will call
   * this action and in response get a unique address to which to send heartbeats.
   *
   * @author Jordan Halterman
   */
  public class Register extends Action<RootContext> implements SynchronousAction<String> {

    public static final String NAME = "register";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<String>() {
        @Override
        public String name() {
          return "address";
        }
        @Override
        public boolean isValid(String value) {
          return value instanceof String;
        }
      });
    }};

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public String execute(Object[] args) {
      final String address = (String) args[0];
      String heartbeatAddress = nextHeartBeatAddress();
      heartbeatMap.put(address, heartbeatAddress);
      heartbeatMonitor.monitor(heartbeatAddress, new Handler<String>() {
        @Override
        public void handle(String hbAddress) {
          unregisterStem(address);
        }
      });
      registerStem(address);
      return heartbeatAddress;
    }

  }

  /**
   * A root deploy action.
   *
   * @author Jordan Halterman
   */
  public class Deploy extends Action<RootContext> implements AsynchronousAction<Void> {

    public static final String NAME = "deploy";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<JsonObject>() {
        @Override
        public String name() {
          return "vine";
        }
        @Override
        public boolean isValid(JsonObject value) {
          return value instanceof JsonObject;
        }
      });
    }};

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public void execute(Object[] args, Handler<AsyncResult<Object>> resultHandler) {
      
    }

  }

  /**
   * A root undeploy action.
   *
   * @author Jordan Halterman
   */
  public class Undeploy extends Action<RootContext> implements AsynchronousAction<Void> {

    public static final String NAME = "undeploy";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<JsonObject>() {
        @Override
        public String name() {
          return "vine";
        }
        @Override
        public boolean isValid(JsonObject value) {
          return value instanceof JsonObject;
        }
      });
    }};

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public void execute(Object[] args, Handler<AsyncResult<Object>> resultHandler) {
      
    }

  }

  /**
   * A root load context action.
   *
   * @author Jordan Halterman
   */
  public class LoadContext extends Action<RootContext> implements AsynchronousAction<Void> {

    public static final String NAME = "load";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<String>() {
        @Override
        public String name() {
          return "context";
        }
        @Override
        public boolean isValid(String value) {
          return value instanceof String;
        }
      });
    }};

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public void execute(Object[] args, Handler<AsyncResult<Object>> resultHandler) {
      
    }

  }

  /**
   * A root update context action.
   *
   * @author Jordan Halterman
   */
  public class UpdateContext extends Action<RootContext> implements AsynchronousAction<Void> {

    public static final String NAME = "update";

    private ArgumentsDefinition args = new ArgumentsDefinition() {{
      addArgument(new Argument<JsonObject>() {
        @Override
        public String name() {
          return "context";
        }
        @Override
        public boolean isValid(JsonObject value) {
          return value instanceof JsonObject;
        }
      });
    }};

    @Override
    public ArgumentsDefinition getArgumentsDefinition() {
      return args;
    }

    @Override
    public void execute(Object[] args, Handler<AsyncResult<Object>> resultHandler) {
      
    }

  }

}
