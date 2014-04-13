package net.kuujo.vertigo.output;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

public interface OutputBuffer<T extends OutputBuffer<T>> extends Output<T> {

  /**
   * Opens the output.
   *
   * @return The output.
   */
  T open();

  /**
   * Opens the output.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The output.
   */
  T open(Handler<AsyncResult<Void>> doneHandler);

  /**
   * Sets the output send queue max size.
   *
   * @param maxSize The maximum output queue size.
   * @return The output.
   */
  T setSendQueueMaxSize(int maxSize);

  /**
   * Returns the maximum output queue size.
   *
   * @return The maximum output queue size.
   */
  int getSendQueueMaxSize();

  /**
   * Returns a boolean indicating whether the output queue is full.
   *
   * @return Indicates whether the output queue is full.
   */
  boolean sendQueueFull();

  /**
   * Sets a drain handler on the output.
   *
   * @param handler A handler to be called once the output is prepared for new messages.
   * @return The output.
   */
  T drainHandler(Handler<Void> handler);

  /**
   * Closes the output.
   */
  void close();

  /**
   * Closes the output.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   */
  void close(Handler<AsyncResult<Void>> doneHandler);

}
