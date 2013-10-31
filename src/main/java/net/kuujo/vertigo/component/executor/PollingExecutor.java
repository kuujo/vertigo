package net.kuujo.vertigo.component.executor;

import org.vertx.java.core.Handler;

public interface PollingExecutor extends Executor<PollingExecutor> {

  /**
   * Sets the execute delay.
   *
   * @param delay
   *   The empty execute delay.
   * @return
   *   The called executor instance.
   */
  PollingExecutor setExecuteDelay(long delay);

  /**
   * Gets the execute delay.
   *
   * @return
   *   The empty executor delay.
   */
  long getExecuteDelay();

  /**
   * Sets an execute handler.
   *
   * @param handler
   *   A handler to be invoked for executing the network.
   * @return
   *   The called executor instance.
   */
  PollingExecutor executeHandler(Handler<PollingExecutor> handler);

}
