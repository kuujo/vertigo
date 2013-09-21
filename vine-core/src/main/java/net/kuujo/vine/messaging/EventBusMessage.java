package net.kuujo.vine.messaging;

import org.vertx.java.core.eventbus.Message;

/**
 * An eventbus message.
 *
 * @author Jordan Halterman
 */
public class EventBusMessage {

  private Object id;

  private Message<?> message;

  private boolean ready;

  private boolean acked;

  private boolean ack;

  private boolean locked;

  public EventBusMessage() {
  }

  public EventBusMessage(Object id, Message<?> message) {
    this.id = id;
    this.message = message;
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
  public EventBusMessage setIdentifier(Object id) {
    this.id = id;
    return this;
  }

  /**
   * Gets the unique message identifier.
   *
   * @return
   *   A unique message identifier.
   */
  @SuppressWarnings("unchecked")
  public <T> T getIdentifier() {
    return (T) id;
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
    if (message != null) {
      message.reply();
    }
  }

  /**
   * Replies to the message.
   */
  public <T> void reply(T reply) {
    if (message != null) {
      message.reply(reply);
    }
  }

  /**
   * Checks whether the message has completed processing and acks or fails
   * if necessary.
   */
  private void checkAck() {
    if (!locked && acked && message != null) {
      if (ready) {
        message.reply(ack);
        locked = true;
      }
      else if (!ack) {
        message.reply(false);
        locked = true;
      }
    }
  }

}
