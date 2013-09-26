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

import net.kuujo.via.cluster.RemoteCluster;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;

/**
 * A Via-based cluster implementation.
 *
 * @author Jordan Halterman
 */
public class ViaCluster extends AbstractCluster {

  public ViaCluster(String viaAddress, Vertx vertx) {
    cluster = new RemoteCluster(viaAddress, vertx.eventBus());
  }

  public ViaCluster(String viaAddress, EventBus eventBus) {
    cluster = new RemoteCluster(viaAddress, eventBus);
  }

}
