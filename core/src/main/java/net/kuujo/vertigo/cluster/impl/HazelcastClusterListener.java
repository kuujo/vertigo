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
package net.kuujo.vertigo.cluster.impl;

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
  private final Vertx vertx;
  private final String nodeID;
  private Handler<String> joinHandler;
  private Handler<String> leaveHandler;

  public HazelcastClusterListener(HazelcastInstance hazelcast, Vertx vertx) {
    this.vertx = vertx;
    this.nodeID = hazelcast.getCluster().getLocalMember().getUuid();
    hazelcast.getCluster().addMembershipListener(this);
  }

  @Override
  public String nodeId() {
    return nodeID;
  }

  @Override
  public void joinHandler(Handler<String> handler) {
    joinHandler = handler;
  }

  @Override
  public void leaveHandler(Handler<String> handler) {
    leaveHandler = handler;
  }

  @Override
  public void memberAdded(final MembershipEvent event) {
    if (joinHandler != null) {
      // This method will be called by Hazelcast so we have to make sure
      // we call the Vert.x handler on the proper context.
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          joinHandler.handle(event.getMember().getUuid());
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
    if (leaveHandler != null) {
      // This method will be called by Hazelcast so we have to make sure
      // we call the Vert.x handler on the proper context.
      vertx.runOnContext(new Handler<Void>() {
        @Override
        public void handle(Void _) {
          leaveHandler.handle(event.getMember().getUuid());
        }
      });
    }
  }

}
