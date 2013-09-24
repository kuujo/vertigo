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
import net.kuujo.vevent.definition.VineDefinition;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

/**
 * A clustered Vevent implementation.
 *
 * @author Jordan Halterman
 */
public class ClusteredVevent implements Vevent {

  @Override
  public Vevent setVertx(Vertx vertx) {
    return null;
  }

  @Override
  public Vertx getVertx() {
    return null;
  }

  @Override
  public void deploy(VineDefinition vine,
      Handler<AsyncResult<NetworkContext>> handler) {
    
  }

  @Override
  public void deploy(VineDefinition vine, long timeout,
      Handler<AsyncResult<NetworkContext>> handler) {
    
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
