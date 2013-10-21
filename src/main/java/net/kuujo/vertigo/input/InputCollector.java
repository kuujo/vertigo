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
   * Sets a message handler on the collector.
   *
   * @param handler
   *   The message handler.
   * @return
   *   The called collector instance.
   */
  public InputCollector messageHandler(Handler<JsonMessage> handler);

  /**
   * Starts listening on the given input.
   *
   * @param input
   *   An input on which to listen.
   * @return
   *   The called collector instance.
   */
  public InputCollector listen(Input input);

  /**
   * Starts listening on the given input.
   *
   * @param input
   *   An input on which to listen.
   * @param doneHandler
   *   An asynchronous handler to be invoked once listening has been started.
   * @return
   *   The called collector instance.
   */
  public InputCollector listen(Input input, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Closes the input collector.
   */
  public void close();

}
