package net.kuujo.vertigo.serializer;

import org.vertx.java.core.json.JsonObject;

/**
 * A serializable object.
 *
 * @author Jordan Halterman
 */
public interface Serializable {

  /**
   * Gets the object state.
   *
   * @return
   *   A JSON object representation of the serializable object's internal state.
   */
  public JsonObject getState();

  /**
   * Sets the object state.
   *
   * @param state
   *   A JSON object representation of the serializable object's internal state.
   */
  public void setState(JsonObject state);

}
