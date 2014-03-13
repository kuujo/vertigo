package net.kuujo.vertigo.auditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.message.impl.DefaultMessageId;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
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
  public static final String ADDRESS = "address";
  public static final String TIMEOUT = "timeout";

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
    address = container.config().getString(ADDRESS);
    if (address == null) {
      future.setFailure(new IllegalArgumentException("No auditor address specified."));
      return;
    }

    timeout = container.config().getLong(TIMEOUT, 30000);

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
          log.debug("Message " + tree.id.correlationId() + " timed out.");
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
    JsonObject id = body.getObject("id");
    if (id != null) {
      MessageId messageId = DefaultMessageId.fromJson(id);
      MessageTree tree = new MessageTree(messageId, System.currentTimeMillis() + timeout);
      trees.put(messageId.correlationId(), tree);
      message.reply();
    }
  }

  /**
   * Commits an initial fork to a message tree.
   */
  private void doCommit(final JsonObject message) {
    JsonObject id = message.getObject("id");
    if (id != null) {
      MessageId messageId = DefaultMessageId.fromJson(id);
      MessageTree tree = trees.get(messageId.correlationId());
      if (tree != null) {
        JsonArray children = message.getArray("children");
        if (children != null) {
          for (Object child : children) {
            tree.fork(DefaultMessageId.fromJson((JsonObject) child));
          }
          if (tree.complete()) {
            ack(trees.remove(messageId.correlationId()));
          }
        }
        else {
          ack(trees.remove(messageId.correlationId()));
        }
      }
    }
  }

  /**
   * Acks a message in a tree and creates children.
   */
  private void doAck(final JsonObject message) {
    JsonObject id = message.getObject("id");
    if (id != null) {
      MessageId messageId = DefaultMessageId.fromJson(id);
      MessageTree tree = trees.get(messageId.root());
      JsonArray children = message.getArray("children");
      if (children != null) {
        for (Object child : children) {
          tree.fork(DefaultMessageId.fromJson((JsonObject) child));
        }
      }
      tree.ack(messageId);
      if (tree.complete()) {
        ack(trees.remove(messageId.root()));
      }
    }
  }

  /**
   * Fails a message tree.
   */
  private void doFail(final JsonObject message) {
    JsonObject id = message.getObject("id");
    if (id != null) {
      MessageId messageId = DefaultMessageId.fromJson(id);
      MessageTree tree = trees.remove(messageId.root());
      if (tree != null) {
        fail(tree);
      }
    }
  }

  /**
   * Notifies a message tree owner of an acked message tree.
   */
  private void ack(MessageTree tree) {
    eventBus.send(tree.id.owner(), new JsonObject().putString("action", "ack").putObject("id", tree.id.toJson()));
  }

  /**
   * Notifies a message tree owner of a failed message tree.
   */
  private void fail(MessageTree tree) {
    eventBus.send(tree.id.owner(), new JsonObject().putString("action", "fail").putObject("id", tree.id.toJson()));
  }

  /**
   * Notifies a message tree owner of a timed out message tree.
   */
  private void timeout(MessageTree tree) {
    eventBus.send(tree.id.owner(), new JsonObject().putString("action", "timeout").putObject("id", tree.id.toJson()));
  }

  /**
   * Message tree.
   */
  private static class MessageTree {
    private final MessageId id;
    private long code;
    private long timeout;

    private MessageTree(MessageId messageId, long timeout) {
      this.id = messageId;
      this.timeout = timeout;
    }

    /**
     * Adds a new message to the tree.
     */
    private void fork(MessageId messageId) {
      this.code += messageId.ackCode();
    }

    /**
     * Acks a message in the tree.
     */
    private void ack(MessageId messageId) {
      this.code -= messageId.ackCode();
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
