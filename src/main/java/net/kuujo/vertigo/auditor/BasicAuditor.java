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
package net.kuujo.vertigo.auditor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.message.MessageId;

/**
 * A basic auditor implementation.
 *
 * This auditor tracks message trees by utilizing unique numbers assigned to
 * each message ID. Each time a message is created in the tree, its unique number
 * is added to the tree count. Each time a message is acked in the tree, its
 * number is subtracted from the tree count. Once the tree count returns to zero,
 * the tree is considered fully processed and the message source is notified.
 *
 * This auditor protects against race conditions by allowing creations, acks,
 * and failures to arrive in *any* order. When a creation, ack, or failure arrives
 * for a not-previously-known message tree, the tree will be automatically created.
 * Because of the mathematics involved, the tree will not be considered fully
 * processed until all create/ack/fail messages have been received.
 *
 * @author Jordan Halterman
 */
public class BasicAuditor implements Auditor {
  private Vertx vertx;
  private AuditorVerticle acker;
  private long timeout = 30000;
  private long cleanupInterval = 100;
  private long timeoutTimer;
  private final Map<String, Root> roots = new LinkedHashMap<String, Root>();

  @Override
  public void setVertx(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void setContainer(Container container) {
  }

  @Override
  public void setAcker(AuditorVerticle auditorVerticle) {
    this.acker = auditorVerticle;
  }

  @Override
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public void start() {
    // Only start the timeouts timer if timeouts are enabled (greater than 0).
    if (timeout > 0) startTimer();
  }

  /**
   * Starts a periodic timer that checks for timed out messages.
   */
  private void startTimer() {
    timeoutTimer = vertx.setPeriodic(cleanupInterval, new Handler<Long>() {
      @Override
      public void handle(Long timerId) {
        checkTimeout();
      }
    });
  }

  /**
   * Checks messages for timeouts.
   */
  private void checkTimeout() {
    // If timeout == 0 then timeouts are disabled for the network. Skip the check.
    if (timeout == 0) return;

    // Iterate over nodes and fail any nodes whose expiration time has passed.
    // Nodes are stored in a LinkedHashMap in the order in which they're created,
    // so we can iterate up to the oldest node which has not yet timed out and stop.
    long currentTime = System.currentTimeMillis();
    for (Iterator<Map.Entry<String, Root>> iterator = roots.entrySet().iterator(); iterator.hasNext();) {
      Root root = iterator.next().getValue();
      if (root.timeout <= currentTime) {
        iterator.remove();
        acker.timeout(root.id);
      }
      else {
        break;
      }
    }
  }

  @Override
  public void create(MessageId messageId) {
    Root root = getRoot(messageId.correlationId());
    root.id = messageId;
  }

  @Override
  public void fork(MessageId messageId) {
    String rootId = messageId.root();
    if (rootId != null) {
      Root root = getRoot(rootId);
      root.fork(messageId.ackCode());

      // The root may have already been created and the child ID acked. To guard
      // against race conditions, we account for this and assume the tree could
      // have been completely processed with the addition of this ID.
      if (root.complete()) {
        finish(roots.remove(rootId));
      }
    }
  }

  @Override
  public void ack(MessageId messageId) {
    String rootId = messageId.root();
    if (rootId == null) {
      rootId = messageId.correlationId();
    }

    Root root = getRoot(rootId);
    root.ack(messageId.ackCode());
    if (root.complete()) {
      finish(roots.remove(rootId));
    }
  }

  @Override
  public void fail(MessageId messageId) {
    String rootId = messageId.root();
    if (rootId == null) {
      rootId = messageId.correlationId();
    }

    Root root = getRoot(rootId);
    root.fail(messageId.ackCode());
    if (root.complete()) {
      finish(roots.remove(rootId));
    }
  }

  /**
   * Finishes acking of a tree.
   */
  private void finish(Root root) {
    if (root.failed) {
      acker.fail(root.id);
    }
    else {
      acker.complete(root.id);
    }
  }

  /**
   * Gets a root. If the root does not yet exist then it will be created. This
   * protected against race conditions by allowing roots to be created even
   * before the "create" message is received by the auditor, resulting in creation
   * simply having the effect of appending forks to the root.
   */
  private Root getRoot(String rootId) {
    if (roots.containsKey(rootId)) {
      return roots.get(rootId);
    }
    else {
      Root root = new Root(null, System.currentTimeMillis() + timeout);
      roots.put(rootId, root);
      return root;
    }
  }

  /**
   * A tree root.
   */
  private static final class Root {
    private MessageId id;
    private final long timeout;
    private long count;
    private boolean failed;

    private Root(MessageId id, long timeout) {
      this.id = id;
      this.timeout = timeout;
    }

    /**
     * Adds a new message code to the tree.
     */
    private void fork(long code) {
      count += code;
    }

    /**
     * Acks a message code in the tree.
     */
    private void ack(long code) {
      count -= code;
    }

    /**
     * Fails a message code in the tree.
     */
    private void fail(long code) {
      count -= code;
      failed = true;
    }

    /**
     * Indicates whether the tree has been completely processed.
     */
    private boolean complete() {
      return id != null && count == 0;
    }
  }

  @Override
  public void stop() {
    vertx.cancelTimer(timeoutTimer);
  }

}
