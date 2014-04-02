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
package net.kuujo.vertigo.output.impl;

import net.kuujo.vertigo.cluster.VertigoCluster;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageAcker;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * Ordered output connection for exactly-once processing.
 *
 * @author Jordan Halterman
 */
public class OrderedExactlyOnceOutputConnection extends BaseOutputConnection {

  public OrderedExactlyOnceOutputConnection(Vertx vertx, OutputConnectionContext context, VertigoCluster cluster, MessageAcker acker) {
    super(vertx, context, cluster, acker);
  }

  @Override
  public String send(JsonMessage message) {
    return send(message, (Handler<AsyncResult<Void>>) null);
  }

  @Override
  public String send(JsonMessage message, Handler<AsyncResult<Void>> doneHandler) {
    return message.id();
  }

  @Override
  public String send(JsonMessage message, JsonMessage parent) {
    return send(message, parent, null);
  }

  @Override
  public String send(JsonMessage message, JsonMessage parent, Handler<AsyncResult<Void>> doneHandler) {
    return message.id();
  }

}
