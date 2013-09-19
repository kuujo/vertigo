package net.kuujo.vine.messaging;

import org.vertx.java.core.eventbus.Message;

/**
 * An eventbus message.
 *
 * @author Jordan Halterman
 */
public class EventBusMessage {

  private long id;

  private Message<?> message;

  private boolean ready;

  private boolean acked;

  private boolean ack;

  private boolean responded;

  public EventBusMessage() {
  }

  public EventBusMessage(Message<?> message) {
    this.message = message;
  }

  /**
   * Sets the unique message identifier.
   *
   * @param id
   *   A message identifier.
   * @return
   *   The called message instance.
   */
  public EventBusMessage setIdentifier(long id) {
    this.id = id;
    return this;
  }

  /**
   * Gets the unique message identifier.
   *
   * @return
   *   A unique message identifier.
   */
  public long getIdentifier() {
    return id;
  }

  /**
   * Sets the message to ready state.
   */
  public void ready() {
    ready = true;
    checkAck();
  }

  /**
   * Acknowledges that the message has been processed.
   */
  public void ack() {
    acked = true; ack = true;
    checkAck();
  }

  /**
   * Indicates failure to process the message.
   */
  public void fail() {
    acked = true; ack = false;
    checkAck();
  }

  /**
   * Replies to the message.
   */
  public void reply() {
    message.reply();
  }

  /**
   * Replies to the message.
   */
  public <T> void reply(T reply) {
    message.reply(reply);
  }

  /**
   * Checks whether the message has completed processing and acks or fails
   * if necessary.
   */
  private void checkAck() {
    if (!isLocked()) {
      if (ready && acked && message != null) {
        message.reply(ack);
        lock();
      }
      else if (acked && !ack) {
        message.reply(false);
        lock();
      }
    }
  }

  /**
   * Locks the message, preventing multiple ack responses.
   */
  private void lock() {
    responded = true;
  }

  /**
   * Indicates whether the message has been responded to.
   */
  private boolean isLocked() {
    return responded;
  }

}
