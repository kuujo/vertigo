package net.kuujo.vertigo.output;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.json.JsonObject;

/**
 * A component output collector.
 *
 * @author Jordan Halterman
 */
public interface OutputCollector {

  public String getAddress();

  public OutputCollector emit(JsonObject data);

  public OutputCollector emit(JsonObject data, String tag);

  public OutputCollector emit(JsonObject data, JsonMessage parent);

  public OutputCollector emit(JsonObject data, String tag, JsonMessage parent);

  public void start();

}
