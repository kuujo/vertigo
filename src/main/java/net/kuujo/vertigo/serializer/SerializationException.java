package net.kuujo.vertigo.serializer;

import net.kuujo.vertigo.VertigoException;

/**
 * A serialization exception.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("serial")
public class SerializationException extends VertigoException {

  public SerializationException(String message) {
    super(message);
  }

  public SerializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public SerializationException(Throwable cause) {
    super(cause);
  }

}
