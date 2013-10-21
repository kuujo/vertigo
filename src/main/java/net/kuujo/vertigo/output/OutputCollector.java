package net.kuujo.vertigo.output;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A component output collector.
 *
 * @author Jordan Halterman
 */
public interface OutputCollector {

  /**
   * Returns the output address.
   *
   * @return
   *   The output address.
   */
  public String getAddress();

  /**
   * Emits a message to all output channels.
   *
   * @param data
   *   The data to emit.
   * @return
   *   The called output collector instance.
   */
  public OutputCollector emit(JsonObject data);

  /**
   * Emits a message to all output channels.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to output data.
   * @return
   *   The called output collector instance.
   */
  public OutputCollector emit(JsonObject data, String tag);

  /**
   * Emits a message to all output channels.
   *
   * @param data
   *   The data to emit.
   * @param parent
   *   The parent message of the data.
   * @return
   *   The called output collector instance.
   */
  public OutputCollector emit(JsonObject data, JsonMessage parent);

  /**
   * Emits a message to all output channels.
   *
   * @param data
   *   The data to emit.
   * @param tag
   *   A tag to apply to output data.
   * @param parent
   *   The parent message of the data.
   * @return
   *   The called output collector instance.
   */
  public OutputCollector emit(JsonObject data, String tag, JsonMessage parent);

  /**
   * Starts the output collector.
   */
  public void start();

  /**
   * Starts the output collector.
   *
   * @param doneHandler
   *   An asynchronous handler to be invoked once the collector is started.
   */
  public void start(Handler<AsyncResult<Void>> doneHandler);

}
