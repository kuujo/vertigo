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
package com.blankstyle.vine.vertx;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * A Vine root verticle.
 *
 * @author Jordan Halterman
 */
public class RootVerticle extends BusModBase implements Handler<Message<JsonObject>> {

  private String address;

  private CommandDispatcher dispatcher = new CommandDispatcher() {{
    registerCommand("deploy", Deploy.class);
    registerCommand("undeploy", Undeploy.class);
    registerCommand("load", LoadContext.class);
    registerCommand("update", UpdateContext.class);
    registerCommand("delete", DeleteContext.class);
  }};

  @Override
  public void start() {
    address = getMandatoryStringConfig("address");
    vertx.eventBus().registerHandler(address, this);
  }

  @Override
  public void handle(Message<JsonObject> message) {
    String command = getMandatoryString("command", message);
    JsonObject args = getMandatoryObject("arguments", message);
    message.reply(dispatcher.dispatch(command, args));
  }

  /**
   * Deploys a vine.
   */
  private class Deploy extends Command<JsonObject> {
    @Override
    public JsonObject execute(JsonObject definition) {
      return null;
    }
  }

  /**
   * Undeploys a vine.
   */
  private class Undeploy extends Command<JsonObject> {
    @Override
    public JsonObject execute(JsonObject definition) {
      return null;
    }
  }

  /**
   * Loads a vine context.
   */
  private class LoadContext extends Command<JsonObject> {
    @Override
    public JsonObject execute(JsonObject args) {
      return null;
    }
  }

  /**
   * Updates a vine context.
   */
  private class UpdateContext extends Command<JsonObject> {
    @Override
    public JsonObject execute(JsonObject context) {
      return null;
    }
  }

  /**
   * Deletes a vine context.
   */
  private class DeleteContext extends Command<Void> {
    @Override
    public Void execute(JsonObject context) {
      return null;
    }
  }

}
