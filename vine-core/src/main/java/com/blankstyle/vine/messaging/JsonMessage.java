package com.blankstyle.vine.messaging;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * A JSON data message.
 *
 * @author Jordan Halterman
 */
public class JsonMessage extends JsonObject {

  private EventBusMessage message;

  public JsonMessage(Message<JsonObject> message) {
    super(message.body().getObject("body").toMap());
    this.message = new EventBusMessage(message).setIdentifier(message.body().getString("id"));
  }

  public JsonMessage(JsonObject data) {
    super(data.getObject("body").toMap());
    this.message = new EventBusMessage().setIdentifier(data.getString("id"));
  }

  /**
   * Returns the current underlying event bus message.
   */
  public EventBusMessage message() {
    return message;
  }

  @Override
  public String encode() {
    if (message != null) {
      return new JsonObject().putObject("body", (JsonObject) this).encode();
    }
    else {
      return new JsonObject().putString("id", message.getIdentifier()).putObject("body", new JsonObject(toMap())).encode();
    }
  }

  @Override
  public JsonObject copy() {
    return new JsonMessage(new JsonObject().putObject("body", super.copy()).putString("id", message.getIdentifier()));
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
    return new JsonMessage(new JsonObject().putObject("body", childData).putString("id", message.getIdentifier()));
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
    return new JsonMessage(new JsonObject().putObject("body", data));
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
  public static JsonMessage create(String id, JsonObject data) {
    return new JsonMessage(new JsonObject().putObject("body", data).putString("id", id));
  }

}
