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
package net.kuujo.vertigo.cluster.manager.impl;

import org.vertx.java.core.Handler;

/**
 * Cluster membership listener.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
interface ClusterListener {

  /**
   * Returns the local unique node ID.
   *
   * @return The node ID of the local node.
   */
  String nodeId();

  /**
   * Registers a cluster join handler.
   *
   * @param handler The handler to register.
   */
  void registerJoinHandler(Handler<String> handler);

  /**
   * Unregisters a cluster join handler.
   *
   * @param handler The handler to unregister.
   */
  void unregisterJoinHandler(Handler<String> handler);

  /**
   * Registers a cluster leave handler.
   *
   * @param handler The handler to register.
   */
  void registerLeaveHandler(Handler<String> handler);

  /**
   * Unregisters a cluster leave handler.
   *
   * @param handler The handler to unregister.
   */
  void unregisterLeaveHandler(Handler<String> handler);

}
