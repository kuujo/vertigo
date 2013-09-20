package net.kuujo.vine.messaging;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * A JSON data message.
 *
 * @author Jordan Halterman
 */
public class JsonMessage extends JsonObject {

  private EventBusMessage message;

  public JsonMessage(Object id, JsonObject body, Message<JsonObject> message) {
    super(body.toMap());
    this.message = new EventBusMessage(message).setIdentifier(id);
  }

  public JsonMessage(JsonObject body, Message<JsonObject> message) {
    super(body.toMap());
    this.message = new EventBusMessage(message);
  }

  public JsonMessage(Object id, JsonObject body) {
    super(body.toMap());
    this.message = new EventBusMessage().setIdentifier(id);
  }

  public JsonMessage(JsonObject body) {
    super(body.toMap());
    this.message = new EventBusMessage();
  }

  /**
   * Returns the current underlying event bus message.
   */
  public EventBusMessage message() {
    return message;
  }

  /**
   * Creates a child message.
   *
   * @param childData
   *   The data to apply to the message child.
   * @return
   *   The called message instance.
   */
  public JsonMessage createChild(JsonObject childData) {
    Object id = message.getIdentifier();
    if (id != null) {
      return new JsonMessage(id, childData);
    }
    else {
      return new JsonMessage(childData);
    }
  }

  /**
   * Creates a message with no identifier.
   *
   * @param data
   *   The data to apply to the new message.
   * @return
   *   The new message instance.
   */
  public static JsonMessage create(JsonObject data) {
    return new JsonMessage(data);
  }

  /**
   * Creates a message with an identifier.
   *
   * @param id
   *   The unique message identifier.
   * @param data
   *   The data to apply to the new message.
   * @return
   *   The new message instance
   */
  public static JsonMessage create(Object id, JsonObject data) {
    return new JsonMessage(id, data);
  }

  /**
   * Creates a message with an identifier.
   *
   * @param id
   *   The unique message identifier.
   * @param data
   *   The data to apply to the new message.
   * @param message
   *   The eventbus message.
   * @return
   *   A new message instance.
   */
  public static JsonMessage create(Object id, JsonObject data, Message<JsonObject> message) {
    return new JsonMessage(id, data, message);
  }

}
