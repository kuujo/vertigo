package net.kuujo.vertigo.input;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.Handler;

/**
 * An input collector.
 *
 * @author Jordan Halterman
 */
public interface InputCollector {

  /**
   * Sets a message handler on the collector.
   *
   * @param handler
   *   The message handler.
   * @return
   *   The called collector instance.
   */
  public InputCollector messageHandler(Handler<JsonMessage> handler);

  /**
   * Acknowledges a message.
   *
   * @param message
   *   The message to ack.
   * @return
   *   The called input collector instance.
   */
  public InputCollector ack(JsonMessage message);

  /**
   * Fails a message.
   *
   * @param message
   *   The message to fail.
   * @return
   *   The called input collector instance.
   */
  public InputCollector fail(JsonMessage message);

  /**
   * Fails a message with a failure message.
   *
   * @param message
   *   The message to fail.
   * @param failMessage
   *   A failure message.
   * @return
   *   The called input collector instance.
   */
  public InputCollector fail(JsonMessage message, String failMessage);

  /**
   * Closes the input collector.
   */
  public void close();

}
