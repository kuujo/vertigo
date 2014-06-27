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

import java.util.HashSet;
import java.util.Set;

import net.kuujo.vertigo.util.ContextManager;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

/**
 * Hazelcast cluster membership listener.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
class HazelcastClusterListener implements ClusterListener, MembershipListener {
  private final String nodeID;
  private final ContextManager context;
  private final Set<Handler<String>> joinHandlers = new HashSet<>();
  private final Set<Handler<String>> leaveHandlers = new HashSet<>();

  public HazelcastClusterListener(HazelcastInstance hazelcast, Vertx vertx) {
    this.nodeID = hazelcast.getCluster().getLocalMember().getUuid();
    this.context = new ContextManager(vertx);
    hazelcast.getCluster().addMembershipListener(this);
  }

  @Override
  public String nodeId() {
    return nodeID;
  }

  @Override
  public void registerJoinHandler(Handler<String> handler) {
    joinHandlers.add(handler);
  }

  @Override
  public void unregisterJoinHandler(Handler<String> handler) {
    joinHandlers.remove(handler);
  }

  @Override
  public void registerLeaveHandler(Handler<String> handler) {
    leaveHandlers.add(handler);
  }

  @Override
  public void unregisterLeaveHandler(Handler<String> handler) {
    leaveHandlers.remove(handler);
  }

  @Override
  public void memberAdded(final MembershipEvent event) {
    // This method will be called from the Hazelcast context so we have
    // to make sure we call Vert.x handlers on the proper context.
    for (final Handler<String> handler : joinHandlers) {
      context.run(new Runnable() {
        @Override
        public void run() {
          handler.handle(event.getMember().getUuid());
        }
      });
    }
  }

  @Override
  public void memberAttributeChanged(MemberAttributeEvent event) {
    // Do nothing.
  }

  @Override
  public void memberRemoved(final MembershipEvent event) {
    // This method will be called from the Hazelcast context so we have
    // to make sure we call Vert.x handlers on the proper context.
    for (final Handler<String> handler : leaveHandlers) {
      context.run(new Runnable() {
        @Override
        public void run() {
          handler.handle(event.getMember().getUuid());
        }
      });
    }
  }

}
