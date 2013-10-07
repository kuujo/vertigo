package net.kuujo.vertigo.filter;

import net.kuujo.vertigo.messaging.JsonMessage;

public interface Condition {

  /**
   * Indicates whether the given message is valid.
   *
   * @param message
   *   The message to validate.
   * @return
   *   A boolean indicating whether the given message is valid.
   */
  public boolean isValid(JsonMessage message);

}
