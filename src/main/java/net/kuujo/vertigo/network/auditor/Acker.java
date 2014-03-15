package net.kuujo.vertigo.network.auditor;

import java.util.List;

import net.kuujo.vertigo.message.MessageId;

import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;

/**
 * A message acker.
 *
 * @author Jordan Halterman
 */
public interface Acker {

  /**
   * Starts the acker.
   *
   * @param doneHandler An asynchronous handler to be called once the acker is started.
   * @return The called acker instance.
   */
  Acker start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sets an ack handler on the acker.
   *
   * @param ackHandler An ack handler.
   * @return The called acker instance.
   */
  Acker ackHandler(Handler<String> ackHandler);

  /**
   * Sets a fail handler on the acker.
   *
   * @param failHandler A fail handler.
   * @return The called acker instance.
   */
  Acker failHandler(Handler<String> failHandler);

  /**
   * Sets a timeout handler on the acker.
   *
   * @param timeoutHandler A timeout handler.
   * @return The called acker instance.
   */
  Acker timeoutHandler(Handler<String> timeoutHandler);

  /**
   * Creates a new message.
   *
   * @param messageId The message ID.
   * @param doneHandler An asynchronous handler to be called once the tree has been created.
   * @return The called acker instance.
   */
  Acker create(MessageId messageId, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Forks a message, creating children.
   *
   * @param messageId The parent message ID.
   * @param children The children message IDs.
   * @return The called acker instance.
   */
  Acker fork(MessageId messageId, List<MessageId> children);

  /**
   * Commits a message update to the auditor.
   *
   * @param messageId The message ID.
   * @return The called acker instance.
   */
  Acker commit(MessageId messageId);

  /**
   * Acks a message.
   *
   * @param messageId The acked message ID.
   * @return The called acker instance.
   */
  Acker ack(MessageId messageId);

  /**
   * Fails a message.
   *
   * @param messageId The failed message ID.
   * @return The called acker instance.
   */
  Acker fail(MessageId messageId);

}
