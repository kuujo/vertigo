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
package net.kuujo.vevent;

import net.kuujo.vevent.context.NetworkContext;
import net.kuujo.vevent.definition.NetworkDefinition;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * A Via-based cluster implementation.
 *
 * @author Jordan Halterman
 */
public class ViaCluster implements Cluster {

  private String address;

  private Vertx vertx;

  private EventBus eventBus;

  public ViaCluster(String viaAddress) {
    this.address = viaAddress;
  }

  public ViaCluster(String viaAddress, Vertx vertx) {
    this.address = viaAddress;
    this.vertx = vertx;
  }

  @Override
  public Cluster setVertx(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    return this;
  }

  @Override
  public void deploy(NetworkDefinition network,
      Handler<AsyncResult<NetworkContext>> doneHandler) {
    
  }

  @Override
  public void deploy(NetworkDefinition network, long timeout,
      Handler<AsyncResult<NetworkContext>> doneHandler) {
    
  }

  @Override
  public void shutdown(NetworkContext context) {
    
  }

  @Override
  public void shutdown(NetworkContext context,
      Handler<AsyncResult<Void>> doneHandler) {
    
  }

  @Override
  public void shutdown(NetworkContext context, long timeout,
      Handler<AsyncResult<Void>> doneHandler) {
    
  }

}
