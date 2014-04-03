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

import java.util.List;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageAcker;
import net.kuujo.vertigo.message.impl.ReliableJsonMessage;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;

/**
 * Basic input connection implementation.
 *
 * @author Jordan Halterman
 */
public class BasicInputConnection extends BaseInputConnection {
  private final MessageAcker emptyAcker = new MessageAcker() {
    @Override
    public void anchor(JsonMessage child) {
      
    }
    @Override
    public void anchor(List<JsonMessage> children) {
      
    }
    @Override
    public void ack() {
      
    }
    @Override
    public void timeout() {
      
    }
  };

  public BasicInputConnection(Vertx vertx, InputConnectionContext context, VertigoCluster cluster) {
    super(vertx, context, cluster);
  }

  @Override
  protected void handleMessage(ReliableJsonMessage message, final Message<String> sourceMessage) {
    if (messageHandler != null) {
      message.setAcker(emptyAcker);
      messageHandler.handle(message);
    }
  }

}
