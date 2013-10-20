package net.kuujo.vertigo.input;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * An input collector.
 *
 * @author Jordan Halterman
 */
public interface InputCollector {

  /**
   * Adds an input to the collector.
   *
   * @param input
   *   The input to add.
   * @return
   *   The called collector instance.
   */
  public InputCollector addInput(Input input);

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
   * Starts the input collector.
   *
   * @return
   *   The called collector instance.
   */
  public InputCollector start();

  /**
   * Starts the input collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the start is complete.
   * @return
   *   The called collector instance.
   */
  public InputCollector start(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Stops the input collector.
   */
  public void stop();

  /**
   * Stops the input collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the stop is complete.
   */
  public void stop(Handler<AsyncResult<Void>> doneHandler);

}
