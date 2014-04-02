/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.input.impl;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageAcker;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;

/**
 * Basic ordered input connection.
 *
 * @author Jordan Halterman
 */
public class OrderedInputConnection extends BaseInputConnection {

  public OrderedInputConnection(Vertx vertx, InputConnectionContext context, VertigoCluster cluster, MessageAcker acker) {
    super(vertx, context, cluster, acker);
  }

  @Override
  protected void handleMessage(JsonMessage message, final Message<String> sourceMessage) {
    acker.register(message, new Handler<Boolean>() {
      @Override
      public void handle(Boolean success) {
        sourceMessage.reply(true);
      }
    });
  }

}
