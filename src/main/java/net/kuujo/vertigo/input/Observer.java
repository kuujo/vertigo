package net.kuujo.vertigo.input;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * A component observer.
 *
 * @author Jordan Halterman
 */
public interface Observer {

  /**
   * Sets a message handler on the observer.
   *
   * @param handler
   *   A message handler.
   * @return
   *   The called observer instance.
   */
  public Observer messageHandler(Handler<JsonMessage> handler);

  /**
   * Starts the observer.
   *
   * @return
   *   The called observer instance.
   */
  public Observer start();

  /**
   * Starts the observer.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the start is complete.
   * @return
   *   The called observer instance.
   */
  public Observer start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops the observer.
   */
  public void stop();

  /**
   * Stops the observer.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the stop is complete.
   */
  public void stop(Handler<AsyncResult<Void>> doneHandler);

}
