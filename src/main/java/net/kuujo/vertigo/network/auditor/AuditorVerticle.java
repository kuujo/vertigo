package net.kuujo.vertigo.network.auditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

/**
 * Message auditor verticle.
 *
 * @author Jordan Halterman
 */
public class AuditorVerticle extends Verticle {
  private static final Logger log = LoggerFactory.getLogger(AuditorVerticle.class);
  private String address;
  private EventBus eventBus;
  private long timeout = 30000;
  private long cleanupInterval = 100;
  private final Map<String, MessageTree> trees = new HashMap<>();

  private final Handler<Message<JsonObject>> messageHandler = new Handler<Message<JsonObject>>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      String action = body.getString("action");
      if (action != null) {
        switch (action) {
          case "create":
            doCreate(message);
            break;
          case "commit":
            doCommit(body);
            break;
          case "ack":
            doAck(body);
            break;
          case "fail":
            doFail(body);
            break;
        }
      }
    }
  };

  @Override
  public void start(final Future<Void> future) {
    address = container.config().getString("address");
    if (address == null) {
      future.setFailure(new IllegalArgumentException("No auditor address specified."));
      return;
    }

    timeout = container.config().getLong("timeout", 30000);

    eventBus = vertx.eventBus();
    eventBus.registerHandler(address, messageHandler, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else {
          AuditorVerticle.super.start(future);
        }
      }
    });
  }

  @Override
  public void start() {
    startTimer();
  }

  /**
   * Starts a periodic timer that checks for timed out messages.
   */
  private void startTimer() {
    vertx.setPeriodic(cleanupInterval, new Handler<Long>() {
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
    for (Iterator<Map.Entry<String, MessageTree>> iterator = trees.entrySet().iterator(); iterator.hasNext();) {
      MessageTree tree = iterator.next().getValue();
      if (tree.timeout <= currentTime) {
        iterator.remove();
        if (log.isDebugEnabled()) {
          log.debug("Message " + tree.id + " timed out.");
        }
        timeout(tree);
      }
      else {
        break;
      }
    }
  }

  /**
   * Creates a new message tree.
   */
  private void doCreate(final Message<JsonObject> message) {
    JsonObject body = message.body();
    String treeId = body.getString("tree");
    String source = body.getString("source");
    MessageTree tree = new MessageTree(treeId, source, System.currentTimeMillis() + timeout);
    trees.put(treeId, tree);
    message.reply();
  }

  /**
   * Commits an initial fork to a message tree.
   */
  private void doCommit(final JsonObject message) {
    String treeId = message.getString("tree");
    if (treeId != null) {
      MessageTree tree = trees.get(treeId);
      if (tree != null) {
        long children = message.getLong("children", 0);
        if (children != 0) {
          tree.fork(children);
          if (tree.complete()) {
            trees.remove(treeId);
            ack(tree);
          }
        }
        else {
          ack(trees.remove(treeId));
        }
      }
    }
  }

  /**
   * Acks a message in a tree and creates children.
   */
  private void doAck(final JsonObject message) {
    String treeId = message.getString("tree");
    int parent = message.getInteger("parent", 0);
    if (treeId != null) {
      MessageTree tree = trees.get(treeId);
      if (tree != null) {
        tree.fork(message.getLong("children", 0));
        tree.ack(parent);
        if (tree.complete()) {
          trees.remove(treeId);
          ack(tree);
        }
      }
    }
  }

  /**
   * Fails a message tree.
   */
  private void doFail(final JsonObject message) {
    String treeId = message.getString("tree");
    if (treeId != null) {
      MessageTree tree = trees.remove(treeId);
      if (tree != null) {
        fail(tree);
      }
    }
  }

  /**
   * Notifies a message tree owner of an acked message tree.
   */
  private void ack(MessageTree tree) {
    eventBus.send(tree.source, new JsonObject().putString("action", "ack").putString("id", tree.id));
  }

  /**
   * Notifies a message tree owner of a failed message tree.
   */
  private void fail(MessageTree tree) {
    eventBus.send(tree.source, new JsonObject().putString("action", "fail").putString("id", tree.id));
  }

  /**
   * Notifies a message tree owner of a timed out message tree.
   */
  private void timeout(MessageTree tree) {
    eventBus.send(tree.source, new JsonObject().putString("action", "timeout").putString("id", tree.id));
  }

  /**
   * Message tree.
   */
  private static class MessageTree {
    private final String id;
    private final String source;
    private long code;
    private long timeout;

    private MessageTree(String messageId, String source, long timeout) {
      this.id = messageId;
      this.source = source;
      this.timeout = timeout;
    }

    /**
     * Adds a new message to the tree.
     */
    private void fork(long code) {
      this.code += code;
    }

    /**
     * Acks a message in the tree.
     */
    private void ack(long code) {
      this.code -= code;
    }

    /**
     * Checks whether the tree has been completely processed.
     */
    private boolean complete() {
      return code == 0;
    }

    @Override
    public String toString() {
      return String.valueOf(code);
    }
  }

}
