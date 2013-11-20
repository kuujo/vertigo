package net.kuujo.vertigo.acker;

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
   * @param doneHandler
   *   An asynchronous handler to be called once the acker is started.
   * @return
   *   The called acker instance.
   */
  public Acker start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sets an ack handler on the acker.
   *
   * @param ackHandler
   *   An ack handler.
   * @return
   *   The called acker instance.
   */
  public Acker ackHandler(Handler<MessageId> ackHandler);

  /**
   * Sets a fail handler on the acker.
   *
   * @param failHandler
   *   A fail handler.
   * @return
   *   The called acker instance.
   */
  public Acker failHandler(Handler<MessageId> failHandler);

  /**
   * Sets a timeout handler on the acker.
   *
   * @param timeoutHandler
   *   A timeout handler.
   * @return
   *   The called acker instance.
   */
  public Acker timeoutHandler(Handler<MessageId> timeoutHandler);

  /**
   * Creates a new message.
   *
   * @param messageId
   *   The message ID.
   * @return
   *   The called acker instance.
   */
  public Acker create(MessageId messageId);

  /**
   * Forks a message, creating children.
   *
   * @param messageId
   *   The parent message ID.
   * @param children
   *   The children message IDs.
   * @return
   *   The called acker instance.
   */
  public Acker fork(MessageId messageId, List<MessageId> children);

  /**
   * Acks a message.
   *
   * @param messageId
   *   The acked message ID.
   * @return
   *   The called acker instance.
   */
  public Acker ack(MessageId messageId);

  /**
   * Fails a message.
   *
   * @param messageId
   *   The failed message ID.
   * @return
   *   The called acker instance.
   */
  public Acker fail(MessageId messageId);

}
